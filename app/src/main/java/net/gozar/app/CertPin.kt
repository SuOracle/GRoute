package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.security.MessageDigest
import java.security.cert.X509Certificate
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object CertPin {

    private fun viaTunnel(): Boolean =
        VpnState.state.value == Connection.CONNECTED && !IkeController.active

    private val HEX64 = Regex("^[0-9a-fA-F]{64}$")

    fun isValid(pin: String): Boolean {
        val parts = pin.split(',').map { it.trim().replace(":", "") }.filter { it.isNotEmpty() }
        return parts.isNotEmpty() && parts.all { HEX64.matches(it) }
    }

    private val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    })

    suspend fun fetch(address: String, port: Int, sni: String): String? =
        withContext(Dispatchers.IO) {
            val host = address.trim()
            if (host.isEmpty() || port !in 1..65535) return@withContext null
            val name = sni.trim().ifEmpty { host }
            var plain: Socket? = null
            var ssl: SSLSocket? = null
            try {
                val ctx = SSLContext.getInstance("TLS")
                ctx.init(null, trustAll, java.security.SecureRandom())
                plain = if (viaTunnel())
                    Socket(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MixedPort.value)))
                else Socket()
                plain.connect(InetSocketAddress(host, port), 8000)
                plain.soTimeout = 8000
                val factory: SSLSocketFactory = ctx.socketFactory
                ssl = factory.createSocket(plain, name, port, true) as SSLSocket
                val params = ssl.sslParameters
                runCatching { params.serverNames = listOf(SNIHostName(name)) }
                ssl.sslParameters = params
                ssl.startHandshake()
                val chain = ssl.session.peerCertificates
                val leaf = chain.firstOrNull() as? X509Certificate ?: return@withContext null
                val digest = MessageDigest.getInstance("SHA-256").digest(leaf.encoded)
                digest.joinToString("") { b -> "%02x".format(b) }
            } catch (e: Throwable) {
                android.util.Log.w(TAG, "pin fetch failed for $host:$port", e)
                null
            } finally {
                runCatching { ssl?.close() }
                runCatching { plain?.close() }
            }
        }

    private const val TAG = "GRoutePin"
}