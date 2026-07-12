package net.gozar.app

import android.util.Base64
import org.json.JSONObject
import java.net.URLDecoder

object ConfigParser {

    fun parse(uri: String, source: ConfigSource = ConfigSource.PERSONAL): ProxyConfig? {
        val trimmed = uri.trim()
        val lower = trimmed.lowercase()
        return when {
            lower.startsWith("vless://") -> parseVless(trimmed.substring(8), source)
            lower.startsWith("vmess://") -> parseVmess(trimmed.substring(8), source)
            lower.startsWith("trojan://") -> parseTrojan(trimmed.substring(9), source)
            lower.startsWith("ss://") -> parseShadowsocks(trimmed.substring(5), source)
            else -> null
        }
    }

    private fun normalizeNetwork(t: String?): String = when (val v = t.orEmpty().trim().lowercase()) {
        "", "raw" -> "tcp"
        "mkcp" -> "kcp"
        "websocket" -> "ws"
        "h2", "http2" -> "http"
        "splithttp" -> "xhttp"
        else -> v
    }

    private fun normalizeHeaderType(t: String?): String {
        val v = t.orEmpty().trim().lowercase()
        return if (v == "none") "" else v
    }

    private fun parseVless(body: String, source: ConfigSource): ProxyConfig? = try {
        val (name, userHostPort, p) = splitUserUri(body, "VLESS")
        val (uuid, address, port) = splitUserHostPort(userHostPort)
        val network = normalizeNetwork(p["type"])
        ProxyConfig(
            name = name, protocol = "vless", address = address, port = port,
            uuid = pctDecode(uuid),
            encryption = p["encryption"].orEmpty().ifEmpty { "none" }, flow = p["flow"] ?: "",
            network = network, security = p["security"].orEmpty().ifEmpty { "none" },
            sni = p["sni"] ?: "", publicKey = p["pbk"] ?: "", shortId = p["sid"] ?: "",
            fingerprint = p["fp"].orEmpty().ifEmpty { "chrome" },
            path = p["path"].orEmpty().ifEmpty { p["seed"].orEmpty() }, host = p["host"] ?: "",
            serviceName = p["serviceName"].orEmpty().ifEmpty { if (network == "grpc") p["path"].orEmpty() else "" },
            mode = p["mode"] ?: "", alpn = p["alpn"] ?: "",
            headerType = normalizeHeaderType(p["headerType"]),
            source = source
        )
    } catch (e: Exception) { null }

    private fun parseTrojan(body: String, source: ConfigSource): ProxyConfig? = try {
        val (name, userHostPort, p) = splitUserUri(body, "Trojan")
        val (password, address, port) = splitUserHostPort(userHostPort)
        val network = normalizeNetwork(p["type"])
        ProxyConfig(
            name = name, protocol = "trojan", address = address, port = port,
            password = pctDecode(password), flow = p["flow"] ?: "",
            network = network, security = p["security"].orEmpty().ifEmpty { "tls" },
            sni = p["sni"] ?: "", publicKey = p["pbk"] ?: "", shortId = p["sid"] ?: "",
            fingerprint = p["fp"].orEmpty().ifEmpty { "chrome" },
            path = p["path"].orEmpty().ifEmpty { p["seed"].orEmpty() }, host = p["host"] ?: "",
            serviceName = p["serviceName"].orEmpty().ifEmpty { if (network == "grpc") p["path"].orEmpty() else "" },
            mode = p["mode"] ?: "", alpn = p["alpn"] ?: "",
            headerType = normalizeHeaderType(p["headerType"]),
            source = source
        )
    } catch (e: Exception) { null }

    private fun parseVmess(body: String, source: ConfigSource): ProxyConfig? = try {
        var b = body
        val h = b.indexOf('#'); if (h >= 0) b = b.substring(0, h)
        val q = b.indexOf('?'); if (q >= 0) b = b.substring(0, q)
        val json = decodeB64Text(b) ?: throw IllegalArgumentException("bad vmess body")
        val o = JSONObject(json)
        val tls = o.optString("tls")
        val net = normalizeNetwork(o.optString("net", "tcp"))
        ProxyConfig(
            name = o.optString("ps").ifEmpty { "VMess" }, protocol = "vmess",
            address = o.optString("add"), port = o.optString("port").toIntOrNull() ?: 0,
            uuid = o.optString("id"), alterId = o.optString("aid").toIntOrNull() ?: 0,
            encryption = o.optString("scy", "auto").ifEmpty { "auto" },
            network = net,
            security = if (tls.isNotEmpty() && tls != "none") "tls" else "none",
            sni = o.optString("sni"), fingerprint = o.optString("fp", "chrome").ifEmpty { "chrome" },
            path = o.optString("path"), host = o.optString("host"),
            serviceName = if (net == "grpc") o.optString("path") else "",
            mode = o.optString("mode"), alpn = o.optString("alpn"),
            headerType = normalizeHeaderType(o.optString("type")),
            source = source
        )
    } catch (e: Exception) { null }

    private fun parseShadowsocks(body: String, source: ConfigSource): ProxyConfig? = try {
        val hash = body.indexOf('#')
        val name = (if (hash >= 0) formDecode(body.substring(hash + 1)).trim() else "").ifEmpty { "Shadowsocks" }
        var main = if (hash >= 0) body.substring(0, hash) else body
        val q = main.indexOf('?'); if (q >= 0) main = main.substring(0, q)
        main = main.trim()

        val method: String; val password: String; val address: String; val port: Int
        val at = main.lastIndexOf('@')
        if (at >= 0) {
            val rawUser = pctDecode(main.substring(0, at))
            val decoded = decodeB64Text(rawUser)
            val info = if (decoded != null && decoded.contains(':')) decoded else rawUser
            val hp = splitHostPort(main.substring(at + 1))
            address = hp.first; port = hp.second
            val mc = info.indexOf(':')
            method = info.substring(0, mc); password = info.substring(mc + 1)
        } else {
            val dec = decodeB64Text(pctDecode(main)) ?: throw IllegalArgumentException("bad ss body")
            val da = dec.lastIndexOf('@')
            val mp = dec.substring(0, da)
            val hp = splitHostPort(dec.substring(da + 1))
            address = hp.first; port = hp.second
            val mc = mp.indexOf(':')
            method = mp.substring(0, mc); password = mp.substring(mc + 1)
        }
        ProxyConfig(name = name, protocol = "shadowsocks", address = address, port = port,
            method = method, password = password, source = source)
    } catch (e: Exception) { null }

    private fun splitUserUri(body: String, default: String): Triple<String, String, Map<String, String>> {
        val hash = body.indexOf('#')
        val name = (if (hash >= 0) formDecode(body.substring(hash + 1)).trim() else "").ifEmpty { default }
        val main = if (hash >= 0) body.substring(0, hash) else body
        val q = main.indexOf('?')
        val uhp = if (q >= 0) main.substring(0, q) else main
        return Triple(name, uhp.trim(), parseQuery(if (q >= 0) main.substring(q + 1) else ""))
    }

    private fun splitUserHostPort(uhp: String): Triple<String, String, Int> {
        val at = uhp.lastIndexOf('@')
        val hp = splitHostPort(uhp.substring(at + 1))
        return Triple(uhp.substring(0, at), hp.first, hp.second)
    }

    private fun splitHostPort(raw: String): Pair<String, Int> {
        var s = raw.trim()
        val slash = s.indexOf('/')
        if (slash >= 0) s = s.substring(0, slash)
        if (s.startsWith("[")) {
            val end = s.indexOf(']')
            return s.substring(1, end) to s.substring(end + 2).trim().toInt()
        }
        val colon = s.lastIndexOf(':')
        return s.substring(0, colon) to s.substring(colon + 1).trim().toInt()
    }

    private fun parseQuery(query: String): Map<String, String> =
        if (query.isEmpty()) emptyMap()
        else query.split('&').mapNotNull {
            val eq = it.indexOf('=')
            if (eq < 0) null else it.substring(0, eq).trim() to formDecode(it.substring(eq + 1))
        }.toMap()

    private fun formDecode(s: String): String =
        runCatching { URLDecoder.decode(s, "UTF-8") }.getOrDefault(s)

    private fun pctDecode(s: String): String =
        runCatching { URLDecoder.decode(s.replace("+", "%2B"), "UTF-8") }.getOrDefault(s)

    private fun decodeB64(input: String): ByteArray? {
        val s = buildString {
            for (c in input.trim()) if (c != '\n' && c != '\r' && c != ' ' && c != '\t') append(c)
        }
        if (s.isEmpty()) return null
        val bare = s.trimEnd('=')
        val padded = if (bare.length % 4 == 0) bare else bare + "=".repeat(4 - bare.length % 4)
        for (candidate in arrayOf(padded, bare, s)) {
            for (flags in intArrayOf(Base64.DEFAULT, Base64.URL_SAFE)) {
                val r = runCatching { Base64.decode(candidate, flags) }.getOrNull()
                if (r != null && r.isNotEmpty()) return r
            }
        }
        return null
    }

    private fun decodeB64Text(input: String): String? =
        decodeB64(input)?.let { runCatching { String(it, Charsets.UTF_8) }.getOrNull() }
}