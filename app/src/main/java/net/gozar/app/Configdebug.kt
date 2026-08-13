package net.gozar.app

enum class DebugLevel { OK, WARN, BAD }

enum class DebugState { HEALTHY, TIMEOUT, BLOCKED, OFFLINE, BROKEN }

data class DebugCheck(
    val partKey: String,
    val level: DebugLevel,
    val noteKey: String = "",
    val value: String = ""
)

object ConfigDebug {

    private val KNOWN_PROTOCOLS = setOf(
        "vless", "vmess", "trojan", "shadowsocks", "hysteria2",
        "wireguard", "ikev2", "socks", "http", "aether", "tor"
    )
    private val KNOWN_NETWORKS = setOf("tcp", "kcp", "ws", "httpupgrade", "xhttp", "grpc", "http")
    private val KNOWN_SECURITY = setOf("none", "tls", "reality")
    private val KNOWN_FINGERPRINTS = setOf(
        "chrome", "firefox", "safari", "ios", "android", "edge",
        "random", "randomized", "360", "qq"
    )
    private val KNOWN_METHODS = setOf(
        "aes-256-gcm", "aes-128-gcm",
        "chacha20-ietf-poly1305", "xchacha20-ietf-poly1305",
        "2022-blake3-aes-256-gcm", "2022-blake3-aes-128-gcm",
        "2022-blake3-chacha20-poly1305",
        "none", "plain"
    )
    private val KNOWN_ALPN = setOf("h3", "h2", "http/1.1")
    private val KNOWN_XHTTP_MODES = setOf("auto", "packet-up", "stream-up", "stream-one")
    private val KNOWN_GRPC_MODES = setOf("gun", "multi")
    private val KNOWN_AETHER_MODES = setOf("masque", "wg", "gool")
    private val VISION_FLOWS = setOf("xtls-rprx-vision", "xtls-rprx-vision-udp443")

    private val UUID_RE = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private val HEX_RE = Regex("^[0-9a-fA-F]+$")
    private val B64_RE = Regex("^[A-Za-z0-9+/_-]+=*$")

    fun usesTcpProbe(c: ProxyConfig): Boolean = when (c.protocol.trim().lowercase()) {
        "hysteria2", "wireguard", "aether", "tor", "ikev2" -> false
        else -> true
    }

    private fun ok(part: String, value: String = "") = DebugCheck(part, DebugLevel.OK, "", value)
    private fun warn(part: String, note: String, value: String = "") = DebugCheck(part, DebugLevel.WARN, note, value)
    private fun bad(part: String, note: String, value: String = "") = DebugCheck(part, DebugLevel.BAD, note, value)

    private fun normalizeNetwork(n: String): String = when (val v = n.trim().lowercase()) {
        "", "raw" -> "tcp"
        "mkcp" -> "kcp"
        "websocket" -> "ws"
        "h2", "http2" -> "http"
        "splithttp" -> "xhttp"
        else -> v
    }

    fun inspect(c: ProxyConfig): List<DebugCheck> {
        val out = ArrayList<DebugCheck>()
        val proto = c.protocol.trim().lowercase()

        out += when {
            proto.isEmpty() -> bad("dbg_part_protocol", "dbg_empty")
            proto !in KNOWN_PROTOCOLS -> bad("dbg_part_protocol", "dbg_unknown", c.protocol)
            else -> ok("dbg_part_protocol", proto)
        }

        val localOnly = proto == "tor" || proto == "aether"

        if (localOnly) {
            out += warn("dbg_part_endpoint", if (proto == "tor") "dbg_tor_local" else "dbg_aether_local")
        } else {
            val addr = c.address.trim()
            out += when {
                addr.isEmpty() -> bad("dbg_part_address", "dbg_empty")
                addr.contains("://") -> bad("dbg_part_address", "dbg_addr_scheme", c.address)
                addr.any { it.isWhitespace() } -> bad("dbg_part_address", "dbg_addr_space", c.address)
                else -> ok("dbg_part_address", addr)
            }
            out += if (c.port !in 1..65535) bad("dbg_part_port", "dbg_bad_port", "${c.port}")
            else ok("dbg_part_port", "${c.port}")
        }

        when (proto) {
            "vless" -> {
                out += uuidCheck(c.uuid)
                out += encryptionCheck(c.encryption)
                out += flowCheck(c)
                out += streamChecks(c)
            }
            "vmess" -> {
                out += uuidCheck(c.uuid)
                out += if (c.alterId == 0) ok("dbg_part_alterid", "0")
                else warn("dbg_part_alterid", "dbg_alterid_legacy", "${c.alterId}")
                out += streamChecks(c)
            }
            "trojan" -> {
                out += if (c.password.isBlank()) bad("dbg_part_password", "dbg_empty")
                else ok("dbg_part_password", masked(c.password))
                out += streamChecks(c)
            }
            "shadowsocks" -> {
                out += if (c.password.isBlank()) bad("dbg_part_password", "dbg_empty")
                else ok("dbg_part_password", masked(c.password))
                val m = c.method.trim().lowercase()
                out += when {
                    m.isEmpty() -> bad("dbg_part_method", "dbg_empty")
                    m !in KNOWN_METHODS -> bad("dbg_part_method", "dbg_unknown", c.method)
                    else -> ok("dbg_part_method", m)
                }
            }
            "hysteria2" -> {
                out += if (c.password.isBlank()) bad("dbg_part_password", "dbg_empty")
                else ok("dbg_part_password", masked(c.password))
                out += if (c.sni.isBlank() && c.address.isBlank()) bad("dbg_part_sni", "dbg_empty")
                else ok("dbg_part_sni", c.sni.ifBlank { c.address })
                out += alpnCheck(c.alpn)
                out += if (c.hyUpMbps <= 0 || c.hyDownMbps <= 0)
                    warn("dbg_part_hy_bw", "dbg_hy_bw_zero", "${c.hyUpMbps}/${c.hyDownMbps}")
                else ok("dbg_part_hy_bw", "${c.hyUpMbps}/${c.hyDownMbps} Mbps")
                if (c.hyObfs.isNotBlank() || c.hyObfsPassword.isNotBlank()) {
                    out += when {
                        !c.hyObfs.equals("salamander", ignoreCase = true) && c.hyObfs.isNotBlank() ->
                            bad("dbg_part_obfs", "dbg_unknown", c.hyObfs)
                        c.hyObfsPassword.isBlank() -> bad("dbg_part_obfs", "dbg_obfs_no_password")
                        else -> ok("dbg_part_obfs", "salamander")
                    }
                }
                if (c.fingerprint.isNotBlank())
                    out += warn("dbg_part_fingerprint", "dbg_hy_fingerprint", c.fingerprint)
                out += warn("dbg_part_probe", "dbg_udp_probe")
            }
            "wireguard" -> {
                out += keyCheck("dbg_part_privatekey", c.privateKey)
                out += keyCheck("dbg_part_publickey", c.publicKey)
                val addrs = c.localAddress.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                out += if (addrs.isEmpty()) bad("dbg_part_localaddr", "dbg_empty")
                else ok("dbg_part_localaddr", addrs.joinToString(", "))
                out += when {
                    c.mtu == 0 -> ok("dbg_part_mtu", "auto")
                    c.mtu !in 576..1500 -> warn("dbg_part_mtu", "dbg_mtu_range", "${c.mtu}")
                    else -> ok("dbg_part_mtu", "${c.mtu}")
                }
                if (c.reserved.isNotBlank()) {
                    val r = c.reserved.split(",").map { it.trim() }.mapNotNull { it.toIntOrNull() }
                    out += if (r.size != 3 || r.any { it !in 0..255 })
                        bad("dbg_part_reserved", "dbg_wg_reserved", c.reserved)
                    else ok("dbg_part_reserved", r.joinToString(", "))
                }
                out += warn("dbg_part_probe", "dbg_udp_probe")
            }
            "aether" -> {
                val m = c.aetherMode.trim().lowercase()
                out += when {
                    m.isEmpty() -> ok("dbg_part_aether_mode", "masque")
                    m !in KNOWN_AETHER_MODES -> bad("dbg_part_aether_mode", "dbg_unknown", c.aetherMode)
                    else -> ok("dbg_part_aether_mode", m)
                }
                out += warn("dbg_part_probe", "dbg_udp_probe")
            }
            "tor" -> {
                out += if (c.torBaseId.isBlank()) warn("dbg_part_tor_base", "dbg_tor_no_base")
                else ok("dbg_part_tor_base", c.torBaseId)
                out += ok("dbg_part_tor_route", if (c.torThroughVpn) "tunneled" else "direct")
                out += warn("dbg_part_probe", "dbg_udp_probe")
            }
            "ikev2" -> {
                out += warn("dbg_part_probe", "dbg_ikev2_nodebug")
            }
            "socks", "http" -> {
                out += if (c.uuid.isBlank() && c.password.isBlank()) ok("dbg_part_auth", "none")
                else ok("dbg_part_auth", c.uuid.ifBlank { "user" })
                out += streamChecks(c)
            }
        }

        return out
    }

    private fun masked(s: String): String =
        if (s.length <= 4) "••••" else s.take(2) + "••••" + s.takeLast(2)

    private val VMESS_CIPHERS = setOf("auto", "aes-128-gcm", "chacha20-poly1305", "zero")

    private fun encryptionCheck(e: String): DebugCheck {
        val v = e.trim()
        return when {
            v.isEmpty() || v.equals("none", ignoreCase = true) -> ok("dbg_part_encryption", "none")
            v.lowercase() in VMESS_CIPHERS -> bad("dbg_part_encryption", "dbg_vless_encryption", v)
            v.contains("mlkem", ignoreCase = true) ||
                    v.contains("x25519plus", ignoreCase = true) ->
                ok("dbg_part_encryption", v.substringBefore(".").ifBlank { v })
            else -> warn("dbg_part_encryption", "dbg_unknown", v.take(24))
        }
    }

    private fun uuidCheck(id: String): DebugCheck = when {
        id.isBlank() -> bad("dbg_part_uuid", "dbg_empty")
        !UUID_RE.matches(id.trim()) -> bad("dbg_part_uuid", "dbg_bad_uuid", id)
        else -> ok("dbg_part_uuid", id.trim())
    }

    private fun keyCheck(part: String, key: String): DebugCheck {
        val k = key.trim()
        return when {
            k.isEmpty() -> bad(part, "dbg_empty")
            !B64_RE.matches(k) -> bad(part, "dbg_bad_key", k)
            k.length != 44 -> bad(part, "dbg_key_length", "${k.length}")
            else -> ok(part, masked(k))
        }
    }

    private fun flowCheck(c: ProxyConfig): DebugCheck {
        val f = c.flow.trim()
        if (f.isEmpty()) return ok("dbg_part_flow", "none")
        if (f !in VISION_FLOWS) return bad("dbg_part_flow", "dbg_unknown", f)
        if (c.security != "tls" && c.security != "reality")
            return bad("dbg_part_flow", "dbg_flow_needs_tls", f)
        if (normalizeNetwork(c.network) != "tcp")
            return bad("dbg_part_flow", "dbg_flow_needs_tcp", c.network)
        return ok("dbg_part_flow", f)
    }

    private fun alpnCheck(alpn: String): DebugCheck {
        val list = alpn.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isEmpty()) return ok("dbg_part_alpn", "none")
        val unknown = list.filter { it !in KNOWN_ALPN }
        return if (unknown.isEmpty()) ok("dbg_part_alpn", list.joinToString(", "))
        else warn("dbg_part_alpn", "dbg_alpn_unknown", unknown.joinToString(", "))
    }

    private fun streamChecks(c: ProxyConfig): List<DebugCheck> {
        val out = ArrayList<DebugCheck>()
        val net = normalizeNetwork(c.network)
        out += if (net !in KNOWN_NETWORKS) bad("dbg_part_network", "dbg_unknown", c.network)
        else ok("dbg_part_network", net)

        when (net) {
            "ws", "httpupgrade", "xhttp", "http" -> {
                val p = c.path.trim()
                out += when {
                    p.isEmpty() -> warn("dbg_part_path", "dbg_path_default", "/")
                    !p.startsWith("/") -> bad("dbg_part_path", "dbg_path_slash", p)
                    else -> ok("dbg_part_path", p)
                }
                out += if (c.host.isBlank()) warn("dbg_part_host", "dbg_host_empty")
                else ok("dbg_part_host", c.host)
            }
            "grpc" -> {
                out += if (c.serviceName.isBlank()) bad("dbg_part_service", "dbg_empty")
                else ok("dbg_part_service", c.serviceName)
                val m = c.mode.trim().ifEmpty { "gun" }
                out += if (m !in KNOWN_GRPC_MODES) bad("dbg_part_mode", "dbg_unknown", c.mode)
                else ok("dbg_part_mode", m)
            }
            "kcp" -> {
                out += ok("dbg_part_header", c.headerType.ifBlank { "none" })
            }
        }

        if (net == "xhttp") {
            val m = c.mode.trim().ifEmpty { "auto" }
            out += if (m !in KNOWN_XHTTP_MODES) bad("dbg_part_mode", "dbg_unknown", c.mode)
            else ok("dbg_part_mode", m)
        }

        val sec = c.security.trim().ifEmpty { "none" }
        out += if (sec !in KNOWN_SECURITY) bad("dbg_part_security", "dbg_unknown", c.security)
        else ok("dbg_part_security", sec)

        if (sec == "tls") {
            val name = c.sni.trim().ifEmpty { c.host.substringBefore(",").trim() }
            out += when {
                name.isEmpty() && c.address.isBlank() -> bad("dbg_part_sni", "dbg_empty")
                name.isEmpty() -> warn("dbg_part_sni", "dbg_sni_fallback", c.address)
                else -> ok("dbg_part_sni", name)
            }
            out += alpnCheck(c.alpn)
            out += fingerprintCheck(c.fingerprint)
            if (c.allowInsecure) out += warn("dbg_part_insecure", "dbg_insecure_on")
        }

        if (sec == "reality") {
            if (c.protocol != "vless")
                out += warn("dbg_part_security", "dbg_reality_vless", c.protocol)
            out += if (c.sni.isBlank()) bad("dbg_part_sni", "dbg_empty") else ok("dbg_part_sni", c.sni)
            val pk = c.publicKey.trim()
            out += when {
                pk.isEmpty() -> bad("dbg_part_publickey", "dbg_empty")
                !B64_RE.matches(pk) -> bad("dbg_part_publickey", "dbg_bad_key", pk)
                pk.length != 43 -> bad("dbg_part_publickey", "dbg_key_length", "${pk.length}")
                else -> ok("dbg_part_publickey", masked(pk))
            }
            val sid = c.shortId.trim()
            out += when {
                sid.isEmpty() -> ok("dbg_part_shortid", "none")
                !HEX_RE.matches(sid) -> bad("dbg_part_shortid", "dbg_shortid_hex", sid)
                sid.length % 2 != 0 || sid.length > 16 -> bad("dbg_part_shortid", "dbg_shortid_len", sid)
                else -> ok("dbg_part_shortid", sid)
            }
            out += fingerprintCheck(c.fingerprint)
            if (net == "ws" || net == "httpupgrade")
                out += bad("dbg_part_network", "dbg_reality_no_ws", net)
            if (c.allowInsecure) out += warn("dbg_part_insecure", "dbg_insecure_reality")
        }

        return out
    }

    private fun fingerprintCheck(fp: String): DebugCheck {
        val f = fp.trim().lowercase()
        return when {
            f.isEmpty() -> warn("dbg_part_fingerprint", "dbg_fp_empty")
            f !in KNOWN_FINGERPRINTS -> warn("dbg_part_fingerprint", "dbg_unknown", fp)
            else -> ok("dbg_part_fingerprint", f)
        }
    }
}