package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

enum class PanelKind { XUI, PASARGUARD }

data class PanelReport(
    val checks: List<DebugCheck>,
    val transcript: List<Pair<String, String>>
)

object PanelProbe {

    private const val TIMEOUT = 8_000

    private data class Http(
        val code: Int,
        val body: String,
        val cookies: List<String>,
        val error: String = "",
        val location: String = ""
    )

    private fun call(
        url: String,
        method: String = "GET",
        body: String? = null,
        contentType: String? = null,
        headers: Map<String, String> = emptyMap(),
        cookies: List<String> = emptyList(),
        timeout: Int = TIMEOUT
    ): Http = try {
        val conn = (URL(url).openConnection(route()) as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = timeout
            readTimeout = timeout
            instanceFollowRedirects = false
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "GRoute")
            if (contentType != null) setRequestProperty("Content-Type", contentType)
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
            if (cookies.isNotEmpty()) {
                setRequestProperty("Cookie", cookies.joinToString("; ") { it.substringBefore(';') })
            }
            if (body != null) {
                doOutput = true
                outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
        }
        val code = conn.responseCode
        val text = runCatching {
            (if (code in 200..399) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
        }.getOrDefault("")
        val set = conn.headerFields.orEmpty().entries
            .filter { it.key?.equals("Set-Cookie", ignoreCase = true) == true }
            .flatMap { it.value.orEmpty() }
        val location = conn.headerFields.orEmpty().entries
            .firstOrNull { it.key?.equals("Location", ignoreCase = true) == true }
            ?.value?.firstOrNull().orEmpty()
        runCatching { conn.disconnect() }
        Http(code, text, set, "", location)
    } catch (e: Throwable) {
        Http(-1, "", emptyList(), e.message.orEmpty(), "")
    }

    fun viaTunnel(): Boolean =
        VpnState.state.value == Connection.CONNECTED && !IkeController.active

    private fun route(): Proxy =
        if (viaTunnel()) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MixedPort.value))
        else Proxy.NO_PROXY

    private fun candidates(raw: String): List<String> {
        val u = raw.trim().trimEnd('/')
        if (u.isEmpty()) return emptyList()
        if (u.startsWith("http://") || u.startsWith("https://")) return listOf(u)
        return listOf("https://$u")
    }

    private fun errorKind(e: String): String {
        val m = e.lowercase()
        return when {
            m.contains("certpath") || m.contains("trust anchor") || m.contains("ssl") ||
                m.contains("handshake") || m.contains("certificate") -> "pnl_tls"
            m.contains("timed out") || m.contains("timeout") -> "pnl_timeout"
            m.contains("unable to resolve") || m.contains("no address") ||
                m.contains("nodename") -> "pnl_dns"
            m.contains("econnrefused") || m.contains("refused") -> "pnl_refused"
            else -> "pnl_unreachable"
        }
    }

    private fun enc(v: String): String = java.net.URLEncoder.encode(v, "UTF-8")

    private fun absolute(from: String, location: String): String = runCatching {
        URL(URL(from), location).toString()
    }.getOrDefault(location)

    private fun stripKnownSuffix(url: String): String {
        var u = url.trimEnd('/')
        for (suffix in listOf("/login", "/panel", "/dashboard", "/index.html")) {
            if (u.endsWith(suffix, ignoreCase = true)) u = u.dropLast(suffix.length)
        }
        return u.trimEnd('/')
    }

    suspend fun run(
        kind: PanelKind,
        baseUrl: String,
        username: String,
        password: String,
        config: ProxyConfig
    ): PanelReport = withContext(Dispatchers.IO) {
        val checks = mutableListOf<DebugCheck>()
        val transcript = mutableListOf<Pair<String, String>>()
        val tries = candidates(baseUrl)

        if (tries.isEmpty()) {
            checks += DebugCheck("pnl_part_reach", DebugLevel.BAD, "pnl_no_url")
            return@withContext PanelReport(checks, transcript)
        }

        var base = ""
        var lastError = ""
        var lastCode = -1
        for (candidate in tries) {
            var target = candidate
            var reach = call(target)
            var hops = 0
            while (reach.code in 300..399 && reach.location.isNotBlank() && hops < 3) {
                target = absolute(target, reach.location).trimEnd('/')
                reach = call(target)
                hops++
            }
            transcript += "GET $target" to "HTTP ${reach.code} ${reach.error}".trim()
            if (reach.code >= 0) {
                base = stripKnownSuffix(target)
                lastCode = reach.code
                break
            }
            lastError = reach.error
        }
        if (base.isEmpty()) {
            checks += DebugCheck(
                "pnl_part_reach", DebugLevel.BAD, errorKind(lastError), lastError.take(110)
            )
            return@withContext PanelReport(checks, transcript)
        }
        checks += DebugCheck("pnl_part_reach", DebugLevel.OK, "", "$base  HTTP $lastCode")

        when (kind) {
            PanelKind.XUI -> runXui(base, username, password, config, checks, transcript)
            PanelKind.PASARGUARD ->
                runPasarGuard(base, username, password, config, checks, transcript)
        }
        PanelReport(checks, transcript)
    }

    private fun runXui(
        base: String,
        username: String,
        password: String,
        config: ProxyConfig,
        checks: MutableList<DebugCheck>,
        transcript: MutableList<Pair<String, String>>
    ) {
        val csrf = call("$base/csrf-token")
        transcript += "GET /csrf-token" to "HTTP ${csrf.code} ${csrf.body.take(120)}"
        val token = runCatching { JSONObject(csrf.body).optString("obj") }.getOrDefault("")
        val jar = csrf.cookies.toMutableList()

        val payload = JSONObject()
            .put("username", username)
            .put("password", password)
            .toString()
        val login = call(
            "$base/login",
            method = "POST",
            body = payload,
            contentType = "application/json",
            headers = if (token.isBlank()) emptyMap() else mapOf("X-CSRF-Token" to token),
            cookies = jar
        )
        transcript += "POST /login" to "HTTP ${login.code} ${login.body.take(160)}"
        val ok = runCatching { JSONObject(login.body).optBoolean("success", false) }
            .getOrDefault(false)
        if (!ok) {
            checks += DebugCheck(
                "pnl_part_login", DebugLevel.BAD,
                if (login.code == 403) "pnl_csrf" else "pnl_bad_login",
                runCatching { JSONObject(login.body).optString("msg") }.getOrDefault("").take(90)
            )
            return
        }
        jar += login.cookies

        fun get(path: String): String {
            val r = call("$base$path", cookies = jar)
            transcript += "GET $path" to "HTTP ${r.code} ${r.body.take(300)}"
            return if (r.code in 200..299) r.body else ""
        }

        val status = get("/panel/api/server/status")
        readXuiStatus(status, checks)

        val (inbounds, hosts) = parseXuiInbounds(get("/panel/api/inbounds/list"))
        val model = PanelModel(
            nodes = parseXuiNodes(get("/panel/api/nodes/list")),
            hosts = hosts,
            inbounds = inbounds,
            cores = emptyList()
        )
        checks += PanelAnalysis.analyse(config, model)
    }

    private fun readXuiStatus(body: String, checks: MutableList<DebugCheck>) {
        val obj = runCatching { JSONObject(body).optJSONObject("obj") }.getOrNull()
        if (obj == null) {
            checks += DebugCheck("pnl_part_core", DebugLevel.WARN, "pnl_unknown")
            return
        }
        val xray = obj.optJSONObject("xray")
        val state = xray?.optString("state").orEmpty()
        val err = xray?.optString("errorMsg").orEmpty()
        checks += when {
            state.equals("running", true) ->
                DebugCheck("pnl_part_core", DebugLevel.OK, "", xray?.optString("version").orEmpty())
            err.isNotBlank() -> DebugCheck("pnl_part_core", DebugLevel.BAD, "pnl_core_error", err.take(120))
            else -> DebugCheck("pnl_part_core", DebugLevel.BAD, "pnl_core_down", state)
        }
        val uptime = obj.optLong("uptime", -1L)
        if (uptime > 0) {
            checks += DebugCheck("pnl_part_uptime", DebugLevel.OK, "", fmtUptime(uptime))
        }
    }

    private fun runPasarGuard(
        base: String,
        username: String,
        password: String,
        config: ProxyConfig,
        checks: MutableList<DebugCheck>,
        transcript: MutableList<Pair<String, String>>
    ) {
        val form = "grant_type=password&username=${enc(username)}&password=${enc(password)}"
        val auth = call(
            "$base/api/admin/token",
            method = "POST",
            body = form,
            contentType = "application/x-www-form-urlencoded"
        )
        transcript += "POST /api/admin/token" to "HTTP ${auth.code} ${auth.body.take(160)}"
        val token = runCatching { JSONObject(auth.body).optString("access_token") }.getOrDefault("")
        if (token.isBlank()) {
            checks += DebugCheck(
                "pnl_part_login", DebugLevel.BAD,
                if (auth.code == 401 || auth.code == 422) "pnl_bad_login" else "pnl_unknown",
                "HTTP ${auth.code}"
            )
            return
        }
        val bearer = mapOf("Authorization" to "Bearer $token")

        fun get(path: String): String {
            val r = call("$base$path", headers = bearer)
            transcript += "GET $path" to "HTTP ${r.code} ${r.body.take(300)}"
            return if (r.code in 200..299) r.body else ""
        }

        val cores = parsePgCores(get("/api/cores"))
        val model = PanelModel(
            nodes = parsePgNodes(get("/api/nodes")),
            hosts = parsePgHosts(get("/api/hosts")),
            inbounds = mergeInbounds(
                parsePgInbounds(get("/api/inbounds/details")),
                inboundsFromCores(cores)
            ),
            cores = cores
        )
        checks += PanelAnalysis.analyse(config, model)
    }

    private fun jsonArray(raw: String): JSONArray = runCatching {
        val t = raw.trim()
        if (t.startsWith("[")) JSONArray(t)
        else {
            val o = JSONObject(t)
            o.optJSONArray("nodes") ?: o.optJSONArray("cores") ?: o.optJSONArray("hosts")
                ?: o.optJSONArray("obj") ?: o.optJSONArray("data") ?: JSONArray()
        }
    }.getOrDefault(JSONArray())

    private fun strList(o: JSONObject, key: String): List<String> {
        o.optJSONArray(key)?.let { a ->
            return (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() }
        }
        val single = o.optString(key)
        return if (single.isBlank()) emptyList() else listOf(single)
    }

    private fun parsePgNodes(raw: String): List<PNode> {
        val a = jsonArray(raw)
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            PNode(
                name = o.optString("name"),
                address = o.optString("address"),
                status = o.optString("status"),
                message = o.optString("message"),
                version = o.optString("xray_version").ifBlank { o.optString("core_version") }
            )
        }
    }

    private fun parsePgHosts(raw: String): List<PHost> {
        val a = jsonArray(raw)
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            PHost(
                remark = o.optString("remark"),
                addresses = strList(o, "address"),
                inboundTag = o.optString("inbound_tag"),
                port = if (o.isNull("port")) null else o.optInt("port"),
                sni = strList(o, "sni"),
                host = strList(o, "host"),
                path = o.optString("path"),
                security = o.optString("security"),
                alpn = strList(o, "alpn"),
                fingerprint = o.optString("fingerprint"),
                allowInsecure = if (o.isNull("allowinsecure")) null else o.optBoolean("allowinsecure"),
                disabled = o.optBoolean("is_disabled", false)
            )
        }
    }

    private fun parsePgInbounds(raw: String): List<PInbound> {
        val a = jsonArray(raw)
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            PInbound(o.optString("tag"), o.optString("protocol"), o.optString("network"))
        }
    }

    private fun inboundsFromCores(cores: List<PCore>): List<PInbound> {
        val out = mutableListOf<PInbound>()
        for (core in cores) {
            val ins = core.config.optJSONArray("inbounds") ?: continue
            for (i in 0 until ins.length()) {
                val o = ins.optJSONObject(i) ?: continue
                val stream = o.optJSONObject("streamSettings")
                val network = stream?.optString("network").orEmpty()
                val security = stream?.optString("security").orEmpty()
                val tls = stream?.optJSONObject("tlsSettings")
                val reality = stream?.optJSONObject("realitySettings")
                val alpnArr = tls?.optJSONArray("alpn")
                out += PInbound(
                    tag = o.optString("tag"),
                    protocol = o.optString("protocol"),
                    network = network,
                    port = o.optInt("port").takeIf { it > 0 },
                    security = security,
                    sni = tls?.optString("serverName")
                        .orEmpty().ifBlank { reality?.optString("serverNames").orEmpty() },
                    path = stream?.optJSONObject("wsSettings")?.optString("path")
                        .orEmpty().ifBlank {
                            stream?.optJSONObject("httpupgradeSettings")?.optString("path").orEmpty()
                        },
                    fingerprint = (tls ?: reality)?.optString("fingerprint").orEmpty(),
                    alpn = (0 until (alpnArr?.length() ?: 0)).map { alpnArr!!.optString(it) },
                    publicKey = reality?.optString("publicKey").orEmpty(),
                    shortId = reality?.optJSONArray("shortIds")?.optString(0).orEmpty()
                        .ifBlank { reality?.optString("shortId").orEmpty() },
                    serviceName = stream?.optJSONObject("grpcSettings")
                        ?.optString("serviceName").orEmpty(),
                    headerType = stream?.optJSONObject("tcpSettings")
                        ?.optJSONObject("header")?.optString("type").orEmpty()
                )
            }
        }
        return out
    }

    private fun mergeInbounds(summary: List<PInbound>, rich: List<PInbound>): List<PInbound> {
        if (rich.isEmpty()) return summary
        val byTag = rich.associateBy { it.tag.lowercase() }
        val merged = rich.toMutableList()
        summary.forEach { s -> if (!byTag.containsKey(s.tag.lowercase())) merged += s }
        return merged
    }

    private fun parsePgCores(raw: String): List<PCore> {
        val a = jsonArray(raw)
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            PCore(o.optString("name"), o.optJSONObject("config") ?: JSONObject())
        }
    }

    private fun parseXuiInbounds(raw: String): Pair<List<PInbound>, List<PHost>> {
        val a = jsonArray(raw)
        val inbounds = mutableListOf<PInbound>()
        val hosts = mutableListOf<PHost>()
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val tag = o.optString("tag").ifBlank { o.optString("remark") }
            val stream = runCatching { JSONObject(o.optString("streamSettings")) }.getOrNull()
            val network = stream?.optString("network").orEmpty()
            val security = stream?.optString("security").orEmpty()
            val tls = stream?.optJSONObject("tlsSettings")
                ?: stream?.optJSONObject("realitySettings")
            inbounds += PInbound(tag, o.optString("protocol"), network)
            hosts += PHost(
                remark = o.optString("remark"),
                addresses = strList(o, "listen"),
                inboundTag = tag,
                port = o.optInt("port").takeIf { it > 0 },
                sni = listOfNotNull(tls?.optString("serverName")?.takeIf { it.isNotBlank() }),
                host = emptyList(),
                path = stream?.optJSONObject("wsSettings")?.optString("path").orEmpty(),
                security = security,
                alpn = emptyList(),
                fingerprint = tls?.optString("fingerprint").orEmpty(),
                allowInsecure = null,
                disabled = !o.optBoolean("enable", true)
            )
        }
        return inbounds to hosts
    }

    private fun parseXuiNodes(raw: String): List<PNode> {
        val a = jsonArray(raw)
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            PNode(
                name = o.optString("name").ifBlank { o.optString("remark") },
                address = o.optString("address").ifBlank { o.optString("host") },
                status = if (o.optBoolean("online", false)) "connected" else o.optString("status"),
                message = o.optString("xrayError").ifBlank { o.optString("message") },
                version = o.optString("xrayVersion")
            )
        }
    }

    private fun fmtUptime(seconds: Long): String {
        val d = seconds / 86400
        val h = (seconds % 86400) / 3600
        return if (d > 0) "${d}d ${h}h" else "${h}h"
    }
}
