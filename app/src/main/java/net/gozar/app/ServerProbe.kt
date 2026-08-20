package net.gozar.app

data class ServerReport(
    val checks: List<DebugCheck>,
    val transcript: List<Pair<String, String>>
)

object ServerProbe {

    private const val CORE_NAMES = "xray|v2ray|sing-box|hysteria|trojan-go"

    private val HOSTNAME = Regex("^[A-Za-z0-9._:-]{1,253}$")

    fun exactMatch(store: SshStore, config: ProxyConfig): SshHost? {
        val addr = config.address.trim().lowercase()
        if (addr.isEmpty()) return null
        return store.hosts.value.firstOrNull { it.address.trim().lowercase() == addr }
    }

    private fun addressesOf(name: String): Set<String> = runCatching {
        java.net.InetAddress.getAllByName(name).map { it.hostAddress.orEmpty() }
            .filter { it.isNotEmpty() }.toSet()
    }.getOrDefault(emptySet())

    suspend fun bestMatch(store: SshStore, config: ProxyConfig): SshHost? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            exactMatch(store, config)?.let { return@withContext it }
            val target = config.address.trim()
            if (target.isEmpty()) return@withContext null
            val targetIps = addressesOf(target)
            if (targetIps.isEmpty()) return@withContext null
            store.hosts.value.firstOrNull { host ->
                val hostIps = addressesOf(host.address.trim())
                hostIps.isNotEmpty() && hostIps.any { it in targetIps }
            }
        }

    suspend fun run(hostId: String, config: ProxyConfig): ServerReport {
        val port = config.port
        val sni = config.sni.trim().ifEmpty { config.address.trim() }
        val checks = mutableListOf<DebugCheck>()
        val transcript = mutableListOf<Pair<String, String>>()

        suspend fun run(label: String, cmd: String): SshManager.ExecResult {
            val r = SshManager.exec(hostId, cmd)
            transcript += label to r.text.ifBlank { "(no output)" }
            return r
        }

        val listen = run(
            "listen",
            "ss -tlnp 2>/dev/null | grep -w ':$port' || netstat -tlnp 2>/dev/null | grep -w ':$port'"
        )
        checks += if (listen.out.isNotBlank()) {
            DebugCheck("srv_part_listen", DebugLevel.OK, "", listen.out.lines().first().trim())
        } else {
            DebugCheck("srv_part_listen", DebugLevel.BAD, "srv_no_listen", port.toString())
        }

        val core = run("core", "pgrep -a -f '$CORE_NAMES' 2>/dev/null | head -3")
        checks += if (core.out.isNotBlank()) {
            val name = core.out.lines().first().trim().substringAfter(' ').substringBefore(' ')
                .substringAfterLast('/')
            DebugCheck("srv_part_core", DebugLevel.OK, "", name)
        } else {
            DebugCheck("srv_part_core", DebugLevel.BAD, "srv_no_core")
        }

        val fw = run(
            "firewall",
            "{ iptables -S INPUT 2>/dev/null; ufw status 2>/dev/null; } | head -40"
        )
        val fwText = fw.out
        checks += when {
            fwText.isBlank() -> DebugCheck("srv_part_firewall", DebugLevel.WARN, "srv_unknown")
            fwText.contains("DROP") && !fwText.contains(port.toString()) ->
                DebugCheck("srv_part_firewall", DebugLevel.WARN, "srv_fw_no_rule", port.toString())
            else -> DebugCheck("srv_part_firewall", DebugLevel.OK, "", "ok")
        }

        val safeSni = if (HOSTNAME.matches(sni)) sni else ""
        val cert = if (safeSni.isEmpty()) SshManager.ExecResult(-1, "", "unsafe sni") else run(
            "certificate",
            "echo | timeout 8 openssl s_client -connect 127.0.0.1:$port " +
                "-servername '$safeSni' 2>/dev/null | openssl x509 -noout -subject -dates 2>/dev/null"
        )
        checks += if (cert.out.contains("notAfter")) {
            val until = cert.out.lines().firstOrNull { it.startsWith("notAfter") }
                ?.substringAfter('=')?.trim().orEmpty()
            DebugCheck("srv_part_cert", DebugLevel.OK, "", until)
        } else {
            DebugCheck("srv_part_cert", DebugLevel.WARN, "srv_no_cert", sni)
        }

        val errors = run(
            "core log",
            "{ journalctl -u xray -n 25 --no-pager 2>/dev/null; " +
                "tail -n 25 /var/log/xray/error.log 2>/dev/null; } " +
                "| grep -iE 'error|failed|refused|denied' | tail -6"
        )
        checks += if (errors.out.isNotBlank()) {
            DebugCheck("srv_part_log", DebugLevel.BAD, "", errors.out.lines().last().take(120))
        } else {
            DebugCheck("srv_part_log", DebugLevel.OK, "", "clean")
        }

        val health = run("health", "nproc; cat /proc/loadavg")
        val healthLines = health.out.lines()
        val cpus = healthLines.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
        val load1 = healthLines.getOrNull(1)?.trim()?.split(' ')?.getOrNull(0)?.toDoubleOrNull()
        checks += if (load1 == null || cpus <= 0) {
            DebugCheck("srv_part_load", DebugLevel.WARN, "srv_unknown")
        } else {
            val ratio = load1 / cpus
            val level = when {
                ratio >= 2.0 -> DebugLevel.BAD
                ratio >= 1.0 -> DebugLevel.WARN
                else -> DebugLevel.OK
            }
            val note = when (level) {
                DebugLevel.BAD -> "srv_load_critical"
                DebugLevel.WARN -> "srv_load_high"
                else -> ""
            }
            DebugCheck("srv_part_load", level, note, "$load1 / $cpus")
        }

        return ServerReport(checks, transcript)
    }
}
