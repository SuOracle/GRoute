package net.gozar.app

import android.util.Base64
import org.json.JSONObject
import java.net.URLDecoder

object ConfigParser {

    fun parse(uri: String, source: ConfigSource = ConfigSource.PERSONAL): ProxyConfig? = when {
        uri.startsWith("vless://") -> parseVless(uri, source)
        uri.startsWith("vmess://") -> parseVmess(uri, source)
        uri.startsWith("trojan://") -> parseTrojan(uri, source)
        uri.startsWith("ss://") -> parseShadowsocks(uri, source)
        else -> null
    }

    private fun parseVless(raw: String, source: ConfigSource): ProxyConfig? = try {
        val (name, userHostPort, p) = splitUserUri(raw.removePrefix("vless://"), "VLESS")
        val (uuid, address, port) = splitUserHostPort(userHostPort)
        ProxyConfig(
            name = name, protocol = "vless", address = address, port = port, uuid = uuid,
            encryption = p["encryption"] ?: "none", flow = p["flow"] ?: "",
            network = p["type"] ?: "tcp", security = p["security"] ?: "none",
            sni = p["sni"] ?: "", publicKey = p["pbk"] ?: "", shortId = p["sid"] ?: "",
            fingerprint = p["fp"] ?: "chrome", path = p["path"] ?: "", host = p["host"] ?: "",
            serviceName = p["serviceName"] ?: "", mode = p["mode"] ?: "", alpn = p["alpn"] ?: "",
            source = source
        )
    } catch (e: Exception) { null }

    private fun parseTrojan(raw: String, source: ConfigSource): ProxyConfig? = try {
        val (name, userHostPort, p) = splitUserUri(raw.removePrefix("trojan://"), "Trojan")
        val (password, address, port) = splitUserHostPort(userHostPort)
        ProxyConfig(
            name = name, protocol = "trojan", address = address, port = port,
            password = URLDecoder.decode(password, "UTF-8"), flow = p["flow"] ?: "",
            network = p["type"] ?: "tcp", security = p["security"] ?: "tls",
            sni = p["sni"] ?: "", publicKey = p["pbk"] ?: "", shortId = p["sid"] ?: "",
            fingerprint = p["fp"] ?: "chrome", path = p["path"] ?: "", host = p["host"] ?: "",
            serviceName = p["serviceName"] ?: "", mode = p["mode"] ?: "", alpn = p["alpn"] ?: "",
            source = source
        )
    } catch (e: Exception) { null }

    private fun parseVmess(raw: String, source: ConfigSource): ProxyConfig? = try {
        val o = JSONObject(String(Base64.decode(raw.removePrefix("vmess://").trim(), Base64.DEFAULT)))
        val tls = o.optString("tls")
        ProxyConfig(
            name = o.optString("ps", "VMess"), protocol = "vmess",
            address = o.optString("add"), port = o.optString("port").toIntOrNull() ?: 0,
            uuid = o.optString("id"), alterId = o.optString("aid").toIntOrNull() ?: 0,
            encryption = o.optString("scy", "auto").ifEmpty { "auto" },
            network = o.optString("net", "tcp"),
            security = if (tls.isNotEmpty() && tls != "none") "tls" else "none",
            sni = o.optString("sni"), fingerprint = o.optString("fp", "chrome").ifEmpty { "chrome" },
            path = o.optString("path"), host = o.optString("host"),
            serviceName = if (o.optString("net") == "grpc") o.optString("path") else "",
            mode = o.optString("mode"), alpn = o.optString("alpn"), source = source
        )
    } catch (e: Exception) { null }

    private fun parseShadowsocks(raw: String, source: ConfigSource): ProxyConfig? = try {
        val body = raw.removePrefix("ss://")
        val hash = body.indexOf('#')
        val name = if (hash >= 0) URLDecoder.decode(body.substring(hash + 1), "UTF-8") else "Shadowsocks"
        var main = if (hash >= 0) body.substring(0, hash) else body
        val q = main.indexOf('?'); if (q >= 0) main = main.substring(0, q)

        val method: String; val password: String; val address: String; val port: Int
        if (main.contains('@')) {
            val at = main.lastIndexOf('@')
            val info = String(Base64.decode(main.substring(0, at), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val hp = main.substring(at + 1); val colon = hp.lastIndexOf(':')
            address = hp.substring(0, colon); port = hp.substring(colon + 1).toInt()
            val mc = info.indexOf(':'); method = info.substring(0, mc); password = info.substring(mc + 1)
        } else {
            val dec = String(Base64.decode(main, Base64.DEFAULT))
            val at = dec.lastIndexOf('@'); val mp = dec.substring(0, at)
            val hp = dec.substring(at + 1); val colon = hp.lastIndexOf(':')
            address = hp.substring(0, colon); port = hp.substring(colon + 1).toInt()
            val mc = mp.indexOf(':'); method = mp.substring(0, mc); password = mp.substring(mc + 1)
        }
        ProxyConfig(name = name, protocol = "shadowsocks", address = address, port = port,
            method = method, password = password, source = source)
    } catch (e: Exception) { null }

    private fun splitUserUri(body: String, default: String): Triple<String, String, Map<String, String>> {
        val hash = body.indexOf('#')
        val name = if (hash >= 0) URLDecoder.decode(body.substring(hash + 1), "UTF-8") else default
        val main = if (hash >= 0) body.substring(0, hash) else body
        val q = main.indexOf('?')
        val uhp = if (q >= 0) main.substring(0, q) else main
        return Triple(name, uhp, parseQuery(if (q >= 0) main.substring(q + 1) else ""))
    }

    private fun splitUserHostPort(uhp: String): Triple<String, String, Int> {
        val at = uhp.indexOf('@')
        val hostPort = uhp.substring(at + 1); val colon = hostPort.lastIndexOf(':')
        return Triple(uhp.substring(0, at), hostPort.substring(0, colon), hostPort.substring(colon + 1).toInt())
    }

    private fun parseQuery(query: String): Map<String, String> =
        if (query.isEmpty()) emptyMap()
        else query.split('&').mapNotNull {
            val eq = it.indexOf('=')
            if (eq < 0) null else it.substring(0, eq) to URLDecoder.decode(it.substring(eq + 1), "UTF-8")
        }.toMap()
}