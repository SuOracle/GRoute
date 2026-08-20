package net.gozar.app

import org.json.JSONArray
import org.json.JSONObject

internal data class PNode(
    val name: String,
    val address: String,
    val status: String,
    val message: String,
    val version: String
) {
    val healthy: Boolean get() = status.equals("connected", true) || status.equals("running", true)
}

internal data class PHost(
    val remark: String,
    val addresses: List<String>,
    val inboundTag: String,
    val port: Int?,
    val sni: List<String>,
    val host: List<String>,
    val path: String,
    val security: String,
    val alpn: List<String>,
    val fingerprint: String,
    val allowInsecure: Boolean?,
    val disabled: Boolean
)

internal data class PInbound(
    val tag: String,
    val protocol: String,
    val network: String,
    val port: Int? = null,
    val security: String = "",
    val sni: String = "",
    val path: String = "",
    val fingerprint: String = "",
    val alpn: List<String> = emptyList(),
    val publicKey: String = "",
    val shortId: String = "",
    val serviceName: String = "",
    val headerType: String = ""
)

internal data class PCore(val name: String, val config: JSONObject)

internal data class PanelModel(
    val nodes: List<PNode>,
    val hosts: List<PHost>,
    val inbounds: List<PInbound>,
    val cores: List<PCore>
)

internal object PanelAnalysis {

    private val FREEDOM = setOf("direct", "freedom", "out", "default", "")

    private fun sameAddress(a: String, b: String): Boolean =
        a.trim().equals(b.trim(), ignoreCase = true)

    private fun ipsOf(name: String): Set<String> = runCatching {
        java.net.InetAddress.getAllByName(name.trim())
            .mapNotNull { it.hostAddress }.filter { it.isNotEmpty() }.toSet()
    }.getOrDefault(emptySet())

    private fun nodeFor(model: PanelModel, address: String): PNode? {
        model.nodes.firstOrNull { sameAddress(it.address, address) }?.let { return it }
        val target = ipsOf(address)
        if (target.isEmpty()) return null
        model.nodes.firstOrNull { node ->
            val ips = ipsOf(node.address)
            ips.isNotEmpty() && ips.any { it in target }
        }?.let { return it }
        return model.nodes.firstOrNull { node -> node.address.trim() in target }
    }

    private fun describeMiss(address: String): String {
        val ips = ipsOf(address)
        return if (ips.isEmpty()) address else "$address  \u2192  ${ips.joinToString(", ")}"
    }

    private fun check(part: String, level: DebugLevel, note: String = "", value: String = "") =
        DebugCheck(part, level, note, value)

    fun analyse(config: ProxyConfig, model: PanelModel): List<DebugCheck> {
        val out = mutableListOf<DebugCheck>()
        val addr = config.address.trim()

        val entry = nodeFor(model, addr)
        if (entry == null) {
            out += check("pnl_part_node", DebugLevel.BAD, "pnl_node_missing", describeMiss(addr))
            return out
        }
        out += if (entry.healthy) check(
            "pnl_part_node", DebugLevel.OK, "",
            listOf(entry.name, entry.version).filter { it.isNotBlank() }.joinToString("  ")
        ) else check(
            "pnl_part_node", DebugLevel.BAD, "pnl_node_down",
            (entry.status + "  " + entry.message).trim().take(160)
        )

        val host = model.hosts.firstOrNull { h ->
            h.addresses.any { sameAddress(it, addr) }
        } ?: model.hosts.firstOrNull { it.port != null && it.port == config.port }

        val tag = host?.inboundTag.orEmpty()

        if (host == null) {
            out += check("pnl_part_host", DebugLevel.WARN, "pnl_host_missing", addr)
        } else if (host.disabled) {
            out += check("pnl_part_host", DebugLevel.BAD, "pnl_host_disabled", host.remark)
        } else {
            val diffs = hostDiffs(config, host)
            out += if (diffs.isEmpty()) check("pnl_part_host", DebugLevel.OK, "", host.remark)
            else check(
                "pnl_part_host", DebugLevel.BAD, "pnl_host_mismatch",
                diffs.joinToString("\n").take(400)
            )
        }

        val inbound = model.inbounds.firstOrNull { it.tag.equals(tag, true) }
        out += when {
            tag.isBlank() -> check("pnl_part_inbound", DebugLevel.WARN, "pnl_inbound_unknown")
            inbound == null -> check("pnl_part_inbound", DebugLevel.BAD, "pnl_inbound_missing", tag)
            else -> {
                val diffs = inboundDiffs(config, inbound, host)
                if (diffs.isEmpty()) check(
                    "pnl_part_inbound", DebugLevel.OK, "",
                    listOf(tag, inbound.protocol, inbound.network, inbound.security)
                        .filter { it.isNotBlank() }.joinToString("  ")
                ) else check(
                    "pnl_part_inbound", DebugLevel.BAD, "pnl_inbound_mismatch",
                    diffs.joinToString("\n").take(400)
                )
            }
        }

        if (tag.isNotBlank()) out += chainChecks(model, tag)
        return out
    }

    private fun inboundDiffs(config: ProxyConfig, inbound: PInbound, host: PHost?): List<String> {
        val diffs = mutableListOf<String>()
        fun cmp(name: String, panel: String, mine: String, overridden: Boolean = false) {
            if (overridden) return
            if (panel.isBlank() || mine.isBlank()) return
            if (!panel.equals(mine, true)) diffs += "$name  panel=$panel  config=$mine"
        }
        cmp("protocol", inbound.protocol, config.protocol.trim())
        cmp("network", inbound.network, config.network.trim())
        if (inbound.port != null && host?.port == null && inbound.port != config.port) {
            diffs += "port  panel=${inbound.port}  config=${config.port}"
        }
        val secOverridden = host != null && host.security.isNotBlank() &&
                !host.security.equals("inbound_default", true)
        cmp("security", inbound.security, config.security.trim(), secOverridden)
        cmp("sni", inbound.sni, config.sni.trim(), host?.sni?.isNotEmpty() == true)
        cmp("path", inbound.path, config.path.trim(), host?.path?.isNotBlank() == true)
        cmp(
            "fingerprint", inbound.fingerprint, config.fingerprint.trim(),
            host != null && host.fingerprint.isNotBlank() && !host.fingerprint.equals("none", true)
        )
        cmp("publicKey", inbound.publicKey, config.publicKey.trim())
        cmp("shortId", inbound.shortId, config.shortId.trim())
        cmp("serviceName", inbound.serviceName, config.serviceName.trim())
        cmp("headerType", inbound.headerType, config.headerType.trim())
        val alpn = config.alpn.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (inbound.alpn.isNotEmpty() && alpn.isNotEmpty() && host?.alpn?.isEmpty() != false &&
            inbound.alpn.map { it.lowercase() }.toSet() != alpn.map { it.lowercase() }.toSet()
        ) {
            diffs += "alpn  panel=${inbound.alpn.joinToString(",")}  config=${config.alpn}"
        }
        return diffs
    }

    private fun hostDiffs(config: ProxyConfig, host: PHost): List<String> {
        val diffs = mutableListOf<String>()
        if (host.port != null && host.port != config.port) {
            diffs += "port  panel=${host.port}  config=${config.port}"
        }
        val sni = config.sni.trim()
        if (host.sni.isNotEmpty() && sni.isNotEmpty() &&
            host.sni.none { it.equals(sni, true) }
        ) {
            diffs += "sni  panel=${host.sni.joinToString(",")}  config=$sni"
        }
        val hostHeader = config.host.substringBefore(',').trim()
        if (host.host.isNotEmpty() && hostHeader.isNotEmpty() &&
            host.host.none { it.equals(hostHeader, true) }
        ) {
            diffs += "host  panel=${host.host.joinToString(",")}  config=$hostHeader"
        }
        val path = config.path.trim()
        if (host.path.isNotBlank() && path.isNotBlank() && !host.path.equals(path, true)) {
            diffs += "path  panel=${host.path}  config=$path"
        }
        val sec = host.security.lowercase()
        if (sec.isNotBlank() && sec != "inbound_default" && sec != "none" &&
            !sec.equals(config.security.trim(), true)
        ) {
            diffs += "security  panel=${host.security}  config=${config.security}"
        }
        val fp = host.fingerprint.lowercase()
        if (fp.isNotBlank() && fp != "none" && !fp.equals(config.fingerprint.trim(), true)) {
            diffs += "fingerprint  panel=${host.fingerprint}  config=${config.fingerprint}"
        }
        val alpn = config.alpn.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (host.alpn.isNotEmpty() && alpn.isNotEmpty() &&
            host.alpn.map { it.lowercase() }.toSet() != alpn.map { it.lowercase() }.toSet()
        ) {
            diffs += "alpn  panel=${host.alpn.joinToString(",")}  config=${config.alpn}"
        }
        if (host.allowInsecure != null && host.allowInsecure != config.allowInsecure) {
            diffs += "allowInsecure  panel=${host.allowInsecure}  config=${config.allowInsecure}"
        }
        return diffs
    }

    private fun chainChecks(model: PanelModel, tag: String): List<DebugCheck> {
        val out = mutableListOf<DebugCheck>()
        val core = model.cores.firstOrNull { core ->
            val ins = core.config.optJSONArray("inbounds") ?: JSONArray()
            (0 until ins.length()).any { ins.optJSONObject(it)?.optString("tag").equals(tag, true) }
        } ?: model.cores.firstOrNull()

        if (core == null) {
            out += check("pnl_part_route", DebugLevel.WARN, "pnl_core_missing")
            return out
        }

        val rules = core.config.optJSONObject("routing")?.optJSONArray("rules") ?: JSONArray()
        var outboundTag = ""
        var ruleIndex = -1
        for (i in 0 until rules.length()) {
            val rule = rules.optJSONObject(i) ?: continue
            val tags = rule.optJSONArray("inboundTag") ?: continue
            val hit = (0 until tags.length()).any { tags.optString(it).equals(tag, true) }
            if (hit) {
                outboundTag = rule.optString("outboundTag").ifBlank { rule.optString("balancerTag") }
                ruleIndex = i
                break
            }
        }

        if (ruleIndex < 0 || outboundTag.lowercase() in FREEDOM) {
            out += check("pnl_part_route", DebugLevel.OK, "", "direct")
            return out
        }

        out += check("pnl_part_route", DebugLevel.OK, "", "$tag \u2192 $outboundTag")

        val outbounds = core.config.optJSONArray("outbounds") ?: JSONArray()
        val chosen = (0 until outbounds.length()).mapNotNull { outbounds.optJSONObject(it) }
            .firstOrNull { it.optString("tag").equals(outboundTag, true) }

        if (chosen == null) {
            out += check("pnl_part_outbound", DebugLevel.BAD, "pnl_outbound_missing", outboundTag)
            return out
        }

        val protocol = chosen.optString("protocol")
        val exitAddress = exitAddressOf(chosen)
        out += if (exitAddress.isBlank()) {
            check("pnl_part_outbound", DebugLevel.WARN, "pnl_outbound_unknown", protocol)
        } else {
            check("pnl_part_outbound", DebugLevel.OK, "", "$protocol \u2192 $exitAddress")
        }

        if (exitAddress.isNotBlank()) {
            val exit = nodeFor(model, exitAddress)
            out += when {
                exit == null ->
                    check("pnl_part_exit", DebugLevel.BAD, "pnl_exit_missing", describeMiss(exitAddress))
                exit.healthy -> check(
                    "pnl_part_exit", DebugLevel.OK, "",
                    listOf(exit.name, exit.version).filter { it.isNotBlank() }.joinToString("  ")
                )
                else -> check(
                    "pnl_part_exit", DebugLevel.BAD, "pnl_node_down",
                    (exit.status + "  " + exit.message).trim().take(160)
                )
            }
        }
        return out
    }

    private fun exitAddressOf(outbound: JSONObject): String {
        val settings = outbound.optJSONObject("settings") ?: return ""
        settings.optJSONArray("vnext")?.optJSONObject(0)?.let { return it.optString("address") }
        settings.optJSONArray("servers")?.optJSONObject(0)?.let { return it.optString("address") }
        settings.optJSONArray("peers")?.optJSONObject(0)?.let { return it.optString("endpoint") }
        return ""
    }
}
