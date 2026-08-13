package net.gozar.app

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class SubUserInfo(
    val upload: Long = 0,
    val download: Long = 0,
    val total: Long = 0,
    val expire: Long = 0
) {
    val used: Long get() = upload + download
    val hasData: Boolean get() = total > 0 || expire > 0
}

data class FetchResult(
    val configs: List<ProxyConfig>,
    val userInfo: SubUserInfo?
)

class SubscriptionError(val kind: Kind, val code: Int = 0) : Exception() {
    enum class Kind { HTTP, EMPTY, NOT_CONFIG, CLASH }
}

object SubscriptionFetcher {

    suspend fun fetch(url: String, source: ConfigSource = ConfigSource.PERSONAL): List<ProxyConfig> =
        fetchFull(url, source).configs

    suspend fun fetchFull(url: String, source: ConfigSource = ConfigSource.PERSONAL): FetchResult =
        withContext(Dispatchers.IO) {
            val conn = openFollowingRedirects(url)
            try {
                val code = conn.responseCode
                if (code !in 200..299) {
                    runCatching { conn.errorStream?.use { it.readBytes() } }
                    throw SubscriptionError(SubscriptionError.Kind.HTTP, code)
                }
                val body = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                val userInfo = parseUserInfo(conn.getHeaderField("subscription-userinfo"))
                val text = decodeMaybeBase64(body)
                val configs = text.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .mapNotNull { ConfigParser.parse(it, source) }
                    .toList()
                if (configs.isEmpty()) throw classify(text)
                FetchResult(configs, userInfo)
            } finally {
                conn.disconnect()
            }
        }

    private fun openFollowingRedirects(startUrl: String): HttpURLConnection {
        var current = startUrl
        var hops = 0
        while (true) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                if (this is HttpsURLConnection) {
                    sslSocketFactory = insecureSocketFactory()
                    hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
                connectTimeout = 12000
                readTimeout = 12000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "v2rayNG/1.9.16 GozarNet")
                setRequestProperty("Accept", "*/*")
                instanceFollowRedirects = false
            }
            val code = conn.responseCode
            if (code in 300..399 && hops < 5) {
                val loc = conn.getHeaderField("Location")
                conn.disconnect()
                if (loc.isNullOrBlank()) {
                    throw SubscriptionError(SubscriptionError.Kind.HTTP, code)
                }
                current = URL(URL(current), loc).toString()
                hops++
                continue
            }
            return conn
        }
    }

    private fun insecureSocketFactory(): SSLSocketFactory {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, trustAll, SecureRandom())
        return ctx.socketFactory
    }
    private fun parseUserInfo(header: String?): SubUserInfo? {
        if (header.isNullOrBlank()) return null
        val map = header.split(';').mapNotNull {
            val eq = it.indexOf('=')
            if (eq < 0) null
            else it.substring(0, eq).trim() to (it.substring(eq + 1).trim().toLongOrNull() ?: 0L)
        }.toMap()
        return SubUserInfo(
            upload = map["upload"] ?: 0,
            download = map["download"] ?: 0,
            total = map["total"] ?: 0,
            expire = map["expire"] ?: 0
        )
    }

    private val SCHEMES = listOf(
        "vless://", "vmess://", "trojan://", "ss://", "socks5://", "socks://",
        "hysteria2://", "hy2://", "ikev2://", "wireguard://", "wg://"
    )

    private fun hasConfig(text: String): Boolean {
        val lower = text.lowercase()
        return SCHEMES.any { lower.contains(it) }
    }

    private fun classify(text: String): SubscriptionError {
        val lower = text.trim().lowercase()
        return when {
            lower.isEmpty() -> SubscriptionError(SubscriptionError.Kind.EMPTY)
            lower.contains("proxies:") || lower.contains("proxy-groups:") ||
                    lower.contains("\"outbounds\"") ->
                SubscriptionError(SubscriptionError.Kind.CLASH)
            else -> SubscriptionError(SubscriptionError.Kind.NOT_CONFIG)
        }
    }

    private fun strip(raw: String): String =
        raw.filterNot { it == '\uFEFF' || it == '\u200B' || it == '\u200E' || it == '\u200F' }
            .trim()

    private fun decodeMaybeBase64(body: String): String {
        val trimmed = strip(body)
        if (hasConfig(trimmed)) return trimmed
        val packed = trimmed.filterNot { it.isWhitespace() }
        val flags = listOf(
            Base64.DEFAULT or Base64.NO_WRAP,
            Base64.URL_SAFE or Base64.NO_WRAP,
            Base64.DEFAULT or Base64.NO_WRAP or Base64.NO_PADDING,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        for (f in flags) {
            val out = runCatching { String(Base64.decode(packed, f), Charsets.UTF_8) }.getOrNull()
            if (out != null && hasConfig(out)) return out
        }
        val padded = packed + "=".repeat((4 - packed.length % 4) % 4)
        for (f in flags) {
            val out = runCatching { String(Base64.decode(padded, f), Charsets.UTF_8) }.getOrNull()
            if (out != null && hasConfig(out)) return out
        }
        return trimmed
    }
}