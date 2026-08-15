package net.gozar.app

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

object ConfigParser {

    fun parseBundle(text: String, source: ConfigSource = ConfigSource.PERSONAL): List<ProxyConfig> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()
        val lower = trimmed.lowercase()
        if (lower.contains("[interface]") && lower.contains("[peer]")) {
            val single = parseWireguardConf(trimmed, source)
            if (single != null) return listOf(single)
        }
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val fromJson = parseJsonOutbounds(trimmed, source)
            if (fromJson.isNotEmpty()) return fromJson
        }
        return trimmed.split('\n', '\r')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { parse(it, source) }
    }

    fun parseJsonOutbounds(text: String, source: ConfigSource = ConfigSource.PERSONAL): List<ProxyConfig> {
        val nodes = mutableListOf<Pair<JSONObject, String>>()

        fun collect(o: JSONObject) {
            val label = listOf("remarks", "remark", "name", "ps", "tag")
                .firstNotNullOfOrNull { k -> o.optString(k).takeIf { it.isNotEmpty() } }
                .orEmpty()
            val arr = o.optJSONArray("outbounds")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { nodes.add(it to label) }
                }
            } else {
                nodes.add(o to "")
            }
        }

        runCatching {
            val t = text.trim()
            if (t.startsWith("[")) {
                val arr = JSONArray(t)
                for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { collect(it) }
            } else {
                collect(JSONObject(t))
            }
        }.getOrElse { return emptyList() }

        return nodes.mapNotNull { (node, label) -> outboundToConfig(node, source, label) }
    }

    private fun outboundToConfig(
        o: JSONObject,
        source: ConfigSource,
        parentLabel: String = ""
    ): ProxyConfig? {
        val protocol = o.optString("protocol").lowercase()
        if (protocol.isEmpty()) return null
        if (protocol in setOf("freedom", "blackhole", "dns", "tun", "loopback")) return null

        val settings = o.optJSONObject("settings") ?: JSONObject()
        val stream = o.optJSONObject("streamSettings") ?: JSONObject()
        val tag = o.optString("tag")

        var address = ""
        var port = 0
        var uuid = ""
        var password = ""
        var method = ""
        var encryption = ""
        var flow = ""
        var alterId = 0

        when (protocol) {
            "vless", "vmess" -> {
                val v = settings.optJSONArray("vnext")?.optJSONObject(0) ?: return null
                address = v.optString("address")
                port = v.optInt("port")
                val u = v.optJSONArray("users")?.optJSONObject(0) ?: JSONObject()
                uuid = u.optString("id")
                flow = u.optString("flow")
                alterId = u.optInt("alterId", 0)
                encryption = if (protocol == "vless")
                    u.optString("encryption").ifEmpty { "none" }
                else u.optString("security").ifEmpty { "auto" }
            }
            "trojan", "shadowsocks", "socks", "http" -> {
                val v = settings.optJSONArray("servers")?.optJSONObject(0) ?: return null
                address = v.optString("address")
                port = v.optInt("port")
                password = v.optString("password")
                method = v.optString("method")
                flow = v.optString("flow")
                val u = v.optJSONArray("users")?.optJSONObject(0)
                if (u != null) {
                    uuid = u.optString("user")
                    if (password.isEmpty()) password = u.optString("pass")
                }
            }
            else -> return null
        }
        if (address.isEmpty() || port <= 0) return null

        val network = normalizeNetwork(stream.optString("network").ifEmpty { "tcp" })
        val security = stream.optString("security").ifEmpty { "none" }
        val tls = stream.optJSONObject("tlsSettings")
        val reality = stream.optJSONObject("realitySettings")
        val sec = tls ?: reality

        var host = ""
        var path = ""
        var headerType = ""
        var serviceName = ""
        when (network) {
            "tcp" -> {
                val header = stream.optJSONObject("tcpSettings")?.optJSONObject("header")
                headerType = normalizeHeaderType(header?.optString("type"))
                val req = header?.optJSONObject("request")
                path = req?.optJSONArray("path")?.optString(0).orEmpty()
                host = req?.optJSONObject("headers")?.optJSONArray("Host")?.optString(0).orEmpty()
            }
            "kcp" -> {
                val kcp = stream.optJSONObject("kcpSettings")
                headerType = normalizeHeaderType(kcp?.optJSONObject("header")?.optString("type"))
                path = kcp?.optString("seed").orEmpty()
            }
            "ws" -> {
                val ws = stream.optJSONObject("wsSettings")
                path = ws?.optString("path").orEmpty()
                host = ws?.optJSONObject("headers")?.optString("Host").orEmpty()
                if (host.isEmpty()) host = ws?.optString("host").orEmpty()
            }
            "httpupgrade" -> {
                val hu = stream.optJSONObject("httpupgradeSettings")
                path = hu?.optString("path").orEmpty()
                host = hu?.optString("host").orEmpty()
            }
            "xhttp" -> {
                val xh = stream.optJSONObject("xhttpSettings")
                path = xh?.optString("path").orEmpty()
                host = xh?.optString("host").orEmpty()
            }
            "grpc" -> {
                val g = stream.optJSONObject("grpcSettings")
                serviceName = g?.optString("serviceName").orEmpty()
                host = g?.optString("authority").orEmpty()
            }
            "http" -> {
                val h = stream.optJSONObject("httpSettings")
                path = h?.optString("path").orEmpty()
                host = h?.optJSONArray("host")?.optString(0).orEmpty()
            }
        }

        val sni = sec?.optString("serverName").orEmpty()
        val alpnArr = sec?.optJSONArray("alpn")
        val alpn = if (alpnArr == null) "" else
            (0 until alpnArr.length()).joinToString(",") { alpnArr.optString(it) }

        val name = parentLabel.takeIf { it.isNotEmpty() }
            ?: tag.takeIf { it.isNotEmpty() && it != "proxy" }
            ?: (if (sni.isNotEmpty()) sni else "$address:$port")

        return ProxyConfig(
            name = name,
            protocol = protocol,
            address = address,
            port = port,
            uuid = uuid,
            password = password,
            method = method,
            encryption = encryption,
            flow = flow,
            alterId = alterId,
            network = network,
            security = if (reality != null) "reality" else security,
            sni = sni,
            alpn = alpn,
            host = host,
            path = path,
            headerType = headerType,
            serviceName = serviceName,
            fingerprint = sec?.optString("fingerprint").orEmpty().ifEmpty { "chrome" },
            publicKey = reality?.optString("publicKey").orEmpty(),
            shortId = reality?.optString("shortId").orEmpty(),
            allowInsecure = sec?.optBoolean("allowInsecure", false) ?: false,
            source = source
        )
    }

    fun parse(uri: String, source: ConfigSource = ConfigSource.PERSONAL): ProxyConfig? {
        val trimmed = uri.trim()
        val lower = trimmed.lowercase()
        return when {
            lower.startsWith("vless://") -> parseVless(trimmed.substring(8), source)
            lower.startsWith("vmess://") -> parseVmess(trimmed.substring(8), source)
            lower.startsWith("trojan://") -> parseTrojan(trimmed.substring(9), source)
            lower.startsWith("ss://") -> parseShadowsocks(trimmed.substring(5), source)
            lower.startsWith("socks5://") -> parseProxyUrl(trimmed.substring(9), "socks", source)
            lower.startsWith("socks://") -> parseProxyUrl(trimmed.substring(8), "socks", source)
            lower.startsWith("http://") -> parseProxyUrl(trimmed.substring(7), "http", source)
            lower.startsWith("hysteria2://") -> parseHysteria2(trimmed.substring(12), source)
            lower.startsWith("hy2://") -> parseHysteria2(trimmed.substring(6), source)
            lower.startsWith("ikev2://") -> parseIkev2(trimmed.substring(8), source)
            lower.startsWith("wireguard://") -> parseWireguardUri(trimmed.substring(12), source)
            lower.startsWith("wg://") -> parseWireguardUri(trimmed.substring(5), source)
            lower.contains("[interface]") && lower.contains("[peer]") ->
                parseWireguardConf(trimmed, source)
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

    private fun parseHysteria2(body: String, source: ConfigSource): ProxyConfig? = try {
        val (name, userHostPort, p) = splitUserUri(body, "Hysteria2")
        val (password, address, port) = splitUserHostPort(userHostPort)
        ProxyConfig(
            name = name, protocol = "hysteria2", address = address, port = port,
            password = pctDecode(password),
            sni = p["sni"].orEmpty().ifEmpty { p["peer"].orEmpty() },
            host = p["host"] ?: "",
            alpn = p["alpn"] ?: "",
            security = "tls",
            hyObfs = p["obfs"] ?: "",
            hyObfsPassword = p["obfs-password"].orEmpty().ifEmpty { p["obfsParam"].orEmpty() },
            hyUpMbps = (p["upmbps"] ?: p["up"] ?: "").toIntOrNull() ?: 0,
            hyDownMbps = (p["downmbps"] ?: p["down"] ?: "").toIntOrNull() ?: 0,
            allowInsecure = (p["insecure"] ?: p["allowInsecure"] ?: "") in setOf("1", "true"),
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

    private fun dec(s: String): String = try {
        java.net.URLDecoder.decode(s, "UTF-8")
    } catch (e: Exception) {
        s
    }

    private fun parseProxyUrl(body: String, proto: String, source: ConfigSource): ProxyConfig? {
        return try {
            val hash = body.indexOf('#')
            val name = if (hash >= 0) dec(body.substring(hash + 1)) else ""
            val main = if (hash >= 0) body.substring(0, hash) else body
            val at = main.lastIndexOf('@')
            var user = ""
            var pass = ""
            val hostPart: String
            if (at >= 0) {
                val cred = main.substring(0, at)
                hostPart = main.substring(at + 1)
                val colon = cred.indexOf(':')
                if (colon >= 0) {
                    user = dec(cred.substring(0, colon))
                    pass = dec(cred.substring(colon + 1))
                } else user = dec(cred)
            } else hostPart = main
            val clean = hostPart.substringBefore('/').substringBefore('?')
            val colon = clean.lastIndexOf(':')
            if (colon <= 0) return null
            val host = clean.substring(0, colon)
            val port = clean.substring(colon + 1).toIntOrNull() ?: return null
            ProxyConfig(
                name = if (name.isNotEmpty()) name else "$host:$port",
                protocol = proto,
                address = host,
                port = port,
                uuid = user,
                password = pass,
                source = source
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIkev2(body: String, source: ConfigSource): ProxyConfig? {
        val hash = body.indexOf('#')
        val label = if (hash >= 0) formDecode(body.substring(hash + 1)) else ""
        val core = if (hash >= 0) body.substring(0, hash) else body
        val at = core.lastIndexOf('@')
        if (at <= 0) return null
        val creds = core.substring(0, at)
        val host = core.substring(at + 1).substringBefore('/').trim()
        if (host.isEmpty()) return null
        val colon = creds.indexOf(':')
        if (colon <= 0) return null
        return ProxyConfig(
            name = label.ifBlank { host },
            protocol = "ikev2",
            address = host,
            port = 500,
            sni = host,
            uuid = formDecode(creds.substring(0, colon)),
            password = formDecode(creds.substring(colon + 1)),
            network = "ikev2",
            security = "none",
            source = source
        )
    }

    fun parseWireguardConf(text: String, source: ConfigSource = ConfigSource.PERSONAL): ProxyConfig? {
        return try {
            var section = ""
            var privateKey = ""
            var address = ""
            var mtu = 0
            var publicKey = ""
            var preShared = ""
            var endpoint = ""
            var reserved = ""
            var label = ""
            for (raw in text.lines()) {
                val line = raw.substringBefore('#').trim()
                if (line.isEmpty()) continue
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.lowercase()
                    continue
                }
                val eq = line.indexOf('=')
                if (eq <= 0) continue
                val key = line.substring(0, eq).trim().lowercase()
                val value = line.substring(eq + 1).trim()
                if (section == "[interface]") {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "address" -> address = value
                        "mtu" -> mtu = value.toIntOrNull() ?: 0
                        "reserved" -> reserved = value
                        "name" -> label = value
                    }
                } else if (section == "[peer]") {
                    when (key) {
                        "publickey" -> publicKey = value
                        "presharedkey" -> preShared = value
                        "endpoint" -> endpoint = value
                    }
                }
            }
            if (privateKey.isEmpty() || publicKey.isEmpty() || endpoint.isEmpty()) return null
            val colon = endpoint.lastIndexOf(':')
            if (colon <= 0) return null
            val host = endpoint.substring(0, colon).trim('[', ']')
            val port = endpoint.substring(colon + 1).toIntOrNull() ?: return null
            ProxyConfig(
                name = if (label.isNotEmpty()) label else "WireGuard $host",
                protocol = "wireguard",
                address = host,
                port = port,
                privateKey = privateKey,
                publicKey = publicKey,
                password = preShared,
                localAddress = address,
                mtu = mtu,
                reserved = reserved,
                source = source
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWireguardUri(body: String, source: ConfigSource): ProxyConfig? {
        return try {
            val hash = body.indexOf('#')
            val name = if (hash >= 0) dec(body.substring(hash + 1)) else ""
            val main = if (hash >= 0) body.substring(0, hash) else body
            val q = main.indexOf('?')
            val core = if (q >= 0) main.substring(0, q) else main
            val params = HashMap<String, String>()
            if (q >= 0) {
                for (part in main.substring(q + 1).split("&")) {
                    val i = part.indexOf('=')
                    if (i > 0) params[dec(part.substring(0, i)).lowercase()] = dec(part.substring(i + 1))
                }
            }
            val at = core.lastIndexOf('@')
            if (at <= 0) return null
            val key = dec(core.substring(0, at))
            val hostPart = core.substring(at + 1)
            val colon = hostPart.lastIndexOf(':')
            if (colon <= 0) return null
            val host = hostPart.substring(0, colon).trim('[', ']')
            val port = hostPart.substring(colon + 1).toIntOrNull() ?: return null
            ProxyConfig(
                name = if (name.isNotEmpty()) name else "WireGuard $host",
                protocol = "wireguard",
                address = host,
                port = port,
                privateKey = key,
                publicKey = params["publickey"] ?: params["pubkey"] ?: "",
                password = params["presharedkey"] ?: "",
                localAddress = params["address"] ?: params["ip"] ?: "",
                mtu = params["mtu"]?.toIntOrNull() ?: 0,
                reserved = params["reserved"] ?: "",
                source = source
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseShadowsocks(body: String, source: ConfigSource): ProxyConfig? = try {
        val hash = body.indexOf('#')
        val name = (if (hash >= 0) formDecode(body.substring(hash + 1)).trim() else "").ifEmpty { "Shadowsocks" }
        var main = if (hash >= 0) body.substring(0, hash) else body
        val q = main.indexOf('?')
        val p = parseQuery(if (q >= 0) main.substring(q + 1) else "")
        if (q >= 0) main = main.substring(0, q)
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
        val plugin = p["plugin"].orEmpty()
        val pluginOpts = java.util.TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
        if (plugin.isNotEmpty()) {
            plugin.split(';').forEach { part ->
                val eq = part.indexOf('=')
                if (eq > 0) pluginOpts[part.substring(0, eq).trim()] = part.substring(eq + 1).trim()
                else if (part.isNotBlank() && pluginOpts.isEmpty()) pluginOpts["name"] = part.trim()
            }
        }
        val pluginName = pluginOpts["name"].orEmpty().lowercase()
        val isSimpleObfs = pluginName.startsWith("obfs-local") ||
                pluginName.startsWith("simple-obfs")
        val isV2rayPlugin = pluginName.startsWith("v2ray-plugin")
        val obfsMode = pluginOpts["obfs"].orEmpty().lowercase()

        var network = normalizeNetwork(p["type"])
        var headerType = normalizeHeaderType(p["headerType"])
        var host = p["host"].orEmpty()
        var path = p["path"].orEmpty()
        var security = p["security"].orEmpty().ifEmpty { "none" }

        if (isSimpleObfs && obfsMode == "http") {
            network = "tcp"
            headerType = "http"
            if (host.isEmpty()) host = pluginOpts["obfs-host"].orEmpty()
            if (path.isEmpty()) path = pluginOpts["obfs-uri"].orEmpty()
        } else if (isV2rayPlugin) {
            network = if (pluginOpts.containsKey("quic")) "quic" else "ws"
            if (host.isEmpty()) host = pluginOpts["host"].orEmpty()
            if (path.isEmpty()) path = pluginOpts["path"].orEmpty()
            if (pluginOpts.containsKey("tls")) security = "tls"
        }

        ProxyConfig(name = name, protocol = "shadowsocks", address = address, port = port,
            method = method, password = password,
            network = network.ifEmpty { "tcp" }, headerType = headerType,
            host = host, path = path, security = security,
            sni = p["sni"].orEmpty().ifEmpty { if (security == "tls") host else "" },
            fingerprint = p["fp"].orEmpty().ifEmpty { "chrome" },
            alpn = p["alpn"].orEmpty(),
            source = source)
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

    private fun parseQuery(query: String): Map<String, String> {
        val map = java.util.TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
        if (query.isEmpty()) return map
        query.split('&').forEach {
            val eq = it.indexOf('=')
            if (eq > 0) {
                val key = it.substring(0, eq).trim()
                if (key.isNotEmpty()) map[key] = formDecode(it.substring(eq + 1))
            }
        }
        return map
    }

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