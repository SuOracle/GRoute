package net.gozar.app

import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Registers a free Cloudflare WARP account and returns it as a WireGuard ProxyConfig
 * that Xray-core can run directly.
 *
 * NOTE: the registration endpoint version and client headers below mirror the WARP
 * Android client and occasionally change; if registration starts returning HTTP
 * 4xx these three constants are what to refresh. The endpoint may also be filtered
 * in some regions — registering while already connected through another server works.
 */
object Warp {

    private const val API_VERSION = "v0a2158"
    private const val CLIENT_VERSION = "a-6.10-2158"
    private const val USER_AGENT = "okhttp/3.12.1"
    const val WARP_ENDPOINT_HOST = "162.159.192.1"
    const val WARP_ENDPOINT_PORT = 2408
    private val ENDPOINTS = listOf(
        "162.159.192.1" to 2408,
        "188.114.96.1" to 2408,
        "188.114.97.1" to 2408,
        "188.114.98.1" to 2408,
        "188.114.99.1" to 2408,
        "188.114.96.1" to 1701,
        "188.114.97.1" to 500,
        "188.114.98.1" to 4500
    )

    sealed class Result {
        data class Success(val configs: List<ProxyConfig>) : Result()
        data class Failure(val message: String) : Result()
    }

    fun register(): Result {
        return try {
            val (privateKey, publicKey) = Curve25519.generateKeyPair()

            val tos = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val body = JSONObject()
                .put("key", publicKey)
                .put("install_id", "")
                .put("fcm_token", "")
                .put("tos", tos)
                .put("model", "PC")
                .put("serial_number", "")
                .put("locale", "en_US")
                .toString()

            val url = URL("https://api.cloudflareclient.com/$API_VERSION/reg")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("CF-Client-Version", CLIENT_VERSION)
            }
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()

            if (code !in 200..299) {
                return Result.Failure("HTTP $code: ${resp.take(200)}")
            }

            val cfg = JSONObject(resp).getJSONObject("config")
            val peer = cfg.getJSONArray("peers").getJSONObject(0)
            val peerPub = peer.getString("public_key")

            val addrObj = cfg.getJSONObject("interface").getJSONObject("addresses")
            val v4 = addrObj.optString("v4", "")
            val v6 = addrObj.optString("v6", "")
            val addrs = mutableListOf<String>()
            if (v4.isNotEmpty()) addrs.add("$v4/32")
            if (v6.isNotEmpty()) addrs.add("$v6/128")
            val localAddress = addrs.joinToString(",")
            val reserved = decodeReserved(cfg.optString("client_id", ""))
            val configs = ENDPOINTS.mapIndexed { i, (host, port) ->
                ProxyConfig(
                    name = "WARP ${i + 1}",
                    protocol = "wireguard",
                    address = host,
                    port = port,
                    privateKey = privateKey,
                    publicKey = peerPub,
                    localAddress = localAddress,
                    mtu = 1280,
                    reserved = reserved,
                    source = ConfigSource.PERSONAL
                )
            }
            Result.Success(configs)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "registration error")
        }
    }
    private fun decodeReserved(clientId: String): String {
        return try {
            val b = Base64.decode(clientId, Base64.DEFAULT)
            if (b.size >= 3) "${b[0].toInt() and 0xff},${b[1].toInt() and 0xff},${b[2].toInt() and 0xff}"
            else ""
        } catch (e: Exception) {
            ""
        }
    }
}