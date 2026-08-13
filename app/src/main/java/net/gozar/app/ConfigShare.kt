package net.gozar.app

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject
import java.net.URLEncoder

object ConfigShare {

    fun toLink(c: ProxyConfig): String = when (c.protocol) {
        "vless" -> userLink("vless", c.uuid, c, includeEncryption = true)
        "trojan" -> userLink("trojan", c.password, c, includeEncryption = false)
        "vmess" -> vmessLink(c)
        "shadowsocks" -> ssLink(c)
        "hysteria2" -> hysteria2Link(c)
        else -> ""
    }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun hysteria2Link(c: ProxyConfig): String {
        val params = ArrayList<Pair<String, String>>()
        if (c.sni.isNotEmpty()) params.add("sni" to c.sni)
        if (c.alpn.isNotEmpty()) params.add("alpn" to c.alpn)
        if (c.allowInsecure) params.add("insecure" to "1")
        if (c.hyObfsPassword.isNotEmpty()) {
            params.add("obfs" to c.hyObfs.ifEmpty { "salamander" })
            params.add("obfs-password" to c.hyObfsPassword)
        }
        if (c.hyUpMbps > 0) params.add("upmbps" to c.hyUpMbps.toString())
        if (c.hyDownMbps > 0) params.add("downmbps" to c.hyDownMbps.toString())
        val query = params.joinToString("&") { it.first + "=" + enc(it.second) }
        val host = if (c.address.contains(':')) "[" + c.address + "]" else c.address
        return "hysteria2://" + enc(c.password) + "@" + host + ":" + c.port +
                (if (query.isEmpty()) "" else "?" + query) + "#" + enc(c.name)
    }

    private fun userLink(
        scheme: String,
        userInfo: String,
        c: ProxyConfig,
        includeEncryption: Boolean
    ): String {
        val params = ArrayList<Pair<String, String>>()
        params.add("type" to c.network)
        params.add("security" to c.security)
        if (includeEncryption && c.encryption.isNotEmpty()) params.add("encryption" to c.encryption)
        if (c.flow.isNotEmpty()) params.add("flow" to c.flow)
        if (c.sni.isNotEmpty()) params.add("sni" to c.sni)
        if (c.publicKey.isNotEmpty()) params.add("pbk" to c.publicKey)
        if (c.shortId.isNotEmpty()) params.add("sid" to c.shortId)
        if (c.fingerprint.isNotEmpty()) params.add("fp" to c.fingerprint)
        if (c.path.isNotEmpty()) params.add("path" to c.path)
        if (c.host.isNotEmpty()) params.add("host" to c.host)
        if (c.serviceName.isNotEmpty()) params.add("serviceName" to c.serviceName)
        if (c.mode.isNotEmpty()) params.add("mode" to c.mode)
        if (c.alpn.isNotEmpty()) params.add("alpn" to c.alpn)
        if (c.headerType.isNotEmpty()) params.add("headerType" to c.headerType)
        val query = params.joinToString("&") { "${it.first}=${enc(it.second)}" }
        return "$scheme://${enc(userInfo)}@${c.address}:${c.port}?$query#${enc(c.name)}"
    }

    private fun vmessLink(c: ProxyConfig): String {
        val o = JSONObject()
        o.put("v", "2")
        o.put("ps", c.name)
        o.put("add", c.address)
        o.put("port", c.port.toString())
        o.put("id", c.uuid)
        o.put("aid", c.alterId.toString())
        o.put("scy", c.encryption.ifEmpty { "auto" })
        o.put("net", c.network)
        o.put("type", c.headerType.ifEmpty { "none" })
        o.put("host", c.host)
        o.put("path", c.path)
        o.put("tls", if (c.security == "tls") "tls" else "")
        o.put("sni", c.sni)
        o.put("fp", c.fingerprint)
        val b64 = Base64.encodeToString(o.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "vmess://$b64"
    }

    private fun ssLink(c: ProxyConfig): String {
        val userInfo = Base64.encodeToString(
            "${c.method}:${c.password}".toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        return "ss://$userInfo@${c.address}:${c.port}#${enc(c.name)}"
    }

    const val QR_MAX_CHARS = 2300

    fun qrBitmap(
        text: String,
        size: Int = 720,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE
    ): Bitmap? {
        if (text.isEmpty() || text.length > QR_MAX_CHARS) return null
        return runCatching {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
                EncodeHintType.MARGIN to 1
            )
            val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val w = matrix.width
            val h = matrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val row = y * w
                for (x in 0 until w) {
                    pixels[row + x] = if (matrix.get(x, y)) darkColor else lightColor
                }
            }
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, w, 0, 0, w, h)
            }
        }.getOrNull()
    }
}