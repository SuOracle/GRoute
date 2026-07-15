package net.gozar.app

import org.json.JSONArray
import org.json.JSONObject

object ConfigBuilder {

    fun build(
        config: ProxyConfig,
        fragment: Boolean = false,
        splitRouting: Boolean = false,
        sniffing: Boolean = false,
        sniffTypes: Set<String> = setOf("http", "tls", "quic"),
        fragmentPackets: String = "tlshello",
        fragmentLength: String = "10-20",
        fragmentInterval: String = "10-20",
        mux: Boolean = false,
        muxConcurrency: Int = 8
    ): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))

        root.put("stats", JSONObject())
        root.put("policy", JSONObject().put("system", JSONObject()
            .put("statsOutboundUplink", true)
            .put("statsOutboundDownlink", true)))

        val tunIn = JSONObject().put("tag", "tun-in").put("port", 0).put("protocol", "tun")
            .put("settings", JSONObject().put("name", "xray0").put("MTU", 1500))
        if (splitRouting || sniffing) {
            val types = if (sniffing) expandSniffTypes(sniffTypes) else listOf("http", "tls", "quic")
            val destOverride = JSONArray()
            types.forEach { destOverride.put(it) }
            tunIn.put("sniffing", JSONObject()
                .put("enabled", true)
                .put("destOverride", destOverride)
                .put("routeOnly", splitRouting && !sniffing))
        }

        val socksIn = JSONObject().put("tag", "socks-in")
            .put("port", 10626).put("listen", "127.0.0.1").put("protocol", "socks")
            .put("settings", JSONObject().put("udp", true))

        root.put("inbounds", JSONArray().put(tunIn).put(socksIn))

        val proxyOut = buildOutbound(config)
        if (fragment) {
            proxyOut.optJSONObject("streamSettings")
                ?.put("sockopt", JSONObject().put("dialerProxy", "fragment"))
        }
        if (mux) {
            proxyOut.put("mux", JSONObject()
                .put("enabled", true)
                .put("concurrency", muxConcurrency.coerceIn(1, 128)))
        }

        val outbounds = JSONArray().put(proxyOut)
        if (fragment) {
            outbounds.put(JSONObject()
                .put("tag", "fragment")
                .put("protocol", "freedom")
                .put("settings", JSONObject().put("fragment", JSONObject()
                    .put("packets", fragmentPackets.ifBlank { "tlshello" })
                    .put("length", fragmentLength.ifBlank { "10-20" })
                    .put("interval", fragmentInterval.ifBlank { "10-20" }))))
        }
        outbounds.put(JSONObject().put("tag", "direct").put("protocol", "freedom"))
        root.put("outbounds", outbounds)

        val rules = JSONArray()
        if (splitRouting) {
            rules.put(JSONObject().put("type", "field")
                .put("ip", JSONArray().put("geoip:private").put("geoip:ir"))
                .put("outboundTag", "direct"))
            rules.put(JSONObject().put("type", "field")
                .put("domain", JSONArray().put("geosite:category-ir"))
                .put("outboundTag", "direct"))
        }
        rules.put(JSONObject().put("type", "field")
            .put("inboundTag", JSONArray().put("tun-in").put("socks-in"))
            .put("outboundTag", "proxy"))
        root.put("routing", JSONObject().put("domainStrategy", "AsIs").put("rules", rules))

        return root.toString()
    }

    fun buildForTest(config: ProxyConfig): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "none"))
        root.put("outbounds", JSONArray().put(buildOutbound(config)))
        return root.toString()
    }

    private fun expandSniffTypes(types: Set<String>): List<String> {
        val out = LinkedHashSet<String>()
        for (t in types) {
            if (t == "fakedns+others") out.addAll(listOf("fakedns", "http", "tls", "quic"))
            else out.add(t)
        }
        if (out.isEmpty()) { out.add("http"); out.add("tls") }
        return out.toList()
    }

    private fun buildOutbound(config: ProxyConfig): JSONObject {
        if (config.protocol == "wireguard") return buildWireguard(config)
        val settings = when (config.protocol) {
            "vless", "vmess" -> {
                val user = JSONObject().put("id", config.uuid)
                if (config.protocol == "vless") {
                    user.put("encryption", config.encryption)
                    if (config.flow.isNotEmpty()) user.put("flow", config.flow)
                } else {
                    user.put("alterId", config.alterId)
                    user.put("security", config.encryption.ifEmpty { "auto" })
                }
                val vnext = JSONObject().put("address", config.address).put("port", config.port)
                    .put("users", JSONArray().put(user))
                JSONObject().put("vnext", JSONArray().put(vnext))
            }
            "trojan" -> {
                val server = JSONObject().put("address", config.address)
                    .put("port", config.port).put("password", config.password)
                if (config.flow.isNotEmpty()) server.put("flow", config.flow)
                JSONObject().put("servers", JSONArray().put(server))
            }
            "shadowsocks" -> {
                val server = JSONObject().put("address", config.address).put("port", config.port)
                    .put("method", config.method).put("password", config.password)
                JSONObject().put("servers", JSONArray().put(server))
            }
            else -> JSONObject()
        }
        return JSONObject().put("tag", "proxy").put("protocol", config.protocol)
            .put("settings", settings).put("streamSettings", buildStream(config))
    }


    private fun buildWireguard(config: ProxyConfig): JSONObject {
        val isWarpHost = config.address.equals("engage.cloudflareclient.com", ignoreCase = true)
        val epAddress = if (isWarpHost) Warp.WARP_ENDPOINT_HOST else config.address
        val epPort = if (isWarpHost) Warp.WARP_ENDPOINT_PORT else config.port

        val peer = JSONObject()
            .put("publicKey", config.publicKey)
            .put("endpoint", "$epAddress:$epPort")
            .put("allowedIPs", JSONArray().put("0.0.0.0/0").put("::/0"))

        val addrs = JSONArray()
        config.localAddress.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .forEach { addrs.put(it) }

        val settings = JSONObject()
            .put("secretKey", config.privateKey)
            .put("address", addrs)
            .put("peers", JSONArray().put(peer))
        if (config.mtu > 0) settings.put("mtu", config.mtu)

        val reserved = config.reserved.split(",").map { it.trim() }.mapNotNull { it.toIntOrNull() }
        if (reserved.size == 3) {
            settings.put("reserved", JSONArray().apply { reserved.forEach { put(it) } })
        }

        return JSONObject().put("tag", "proxy").put("protocol", "wireguard").put("settings", settings)
    }

    private fun normalizeNetwork(n: String): String = when (val v = n.trim().lowercase()) {
        "", "raw" -> "tcp"
        "mkcp" -> "kcp"
        "websocket" -> "ws"
        "h2", "http2" -> "http"
        "splithttp" -> "xhttp"
        else -> v
    }

    private fun csvArray(s: String): JSONArray {
        val arr = JSONArray()
        s.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { arr.put(it) }
        return arr
    }

    private fun buildStream(config: ProxyConfig): JSONObject {
        val net = normalizeNetwork(config.network)
        val stream = JSONObject().put("network", net)

        when (net) {
            "tcp" -> {
                if (config.headerType.equals("http", ignoreCase = true)) {
                    val request = JSONObject()
                        .put("version", "1.1")
                        .put("method", "GET")
                        .put("path", csvArray(config.path.ifEmpty { "/" }))
                    val headers = JSONObject()
                    if (config.host.isNotEmpty()) headers.put("Host", csvArray(config.host))
                    headers.put("User-Agent", JSONArray()
                        .put("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"))
                    headers.put("Accept-Encoding", JSONArray().put("gzip, deflate"))
                    headers.put("Connection", JSONArray().put("keep-alive"))
                    headers.put("Pragma", "no-cache")
                    request.put("headers", headers)
                    stream.put("tcpSettings", JSONObject().put("header",
                        JSONObject().put("type", "http").put("request", request)))
                }
            }
            "kcp" -> {
                val kcp = JSONObject().put("header",
                    JSONObject().put("type", config.headerType.ifEmpty { "none" }))
                if (config.path.isNotEmpty()) kcp.put("seed", config.path)
                stream.put("kcpSettings", kcp)
            }
            "ws" -> {
                val ws = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) ws.put("headers", JSONObject().put("Host", config.host))
                stream.put("wsSettings", ws)
            }
            "httpupgrade" -> {
                val hu = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) hu.put("host", config.host)
                stream.put("httpupgradeSettings", hu)
            }
            "xhttp" -> {
                val xh = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) xh.put("host", config.host)
                if (config.mode.isNotEmpty()) xh.put("mode", config.mode)
                stream.put("xhttpSettings", xh)
            }
            "grpc" -> {
                stream.put("grpcSettings", JSONObject()
                    .put("serviceName", config.serviceName)
                    .put("multiMode", config.mode == "multi"))
            }
            "http" -> {
                val h = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) h.put("host", csvArray(config.host))
                stream.put("httpSettings", h)
            }
        }

        when (config.security) {
            "reality" -> stream.put("security", "reality").put("realitySettings", JSONObject()
                .put("serverName", config.sni).put("publicKey", config.publicKey)
                .put("shortId", config.shortId).put("fingerprint", config.fingerprint).put("spiderX", "/"))
            "tls" -> {
                val tls = JSONObject()
                    .put("serverName", config.sni.ifEmpty {
                        config.host.substringBefore(",").trim().ifEmpty { config.address }
                    })
                    .put("fingerprint", config.fingerprint)
                if (config.alpn.isNotEmpty()) {
                    val arr = csvArray(config.alpn)
                    if (arr.length() > 0) tls.put("alpn", arr)
                }
                stream.put("security", "tls").put("tlsSettings", tls)
            }
        }
        return stream
    }
}