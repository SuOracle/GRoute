package net.gozar.app

import gozarcore.Gozarcore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun countryName(code: String): String {
    if (code.isBlank()) return ""
    val name = java.util.Locale("", code.uppercase())
        .getDisplayCountry(java.util.Locale.ENGLISH)
    return name.ifBlank { code.uppercase() }
}

data class ProbeInfo(
    val method: String,
    val entryIp: String,
    val entryIsp: String,
    val entryCountry: String,
    val exitIp: String,
    val exitIsp: String,
    val exitCountry: String,
    val kind: String,
    val reputation: Int,
    val repBand: String,
    val flags: String,
    val flagged: Boolean,
    val vpnExposed: Boolean
)

data class ProbeResult(
    val state: DebugState,
    val pingMs: Int,
    val causeKey: String,
    val findings: List<DebugCheck>,
    val info: ProbeInfo? = null
)

object TransportMethod {

    private val CDNS = listOf(
        "cloudflare" to "Cloudflare",
        "fastly" to "Fastly",
        "akamai" to "Akamai",
        "cloudfront" to "CloudFront",
        "amazon" to "CloudFront",
        "gcore" to "Gcore",
        "g-core" to "Gcore",
        "bunny" to "BunnyCDN",
        "arvan" to "ArvanCloud",
        "derak" to "Derak",
        "azure" to "Azure",
        "microsoft" to "Azure",
        "stackpath" to "StackPath",
        "cdn77" to "CDN77",
        "keycdn" to "KeyCDN",
        "imperva" to "Imperva",
        "incapsula" to "Imperva",
        "sucuri" to "Sucuri"
    )

    private val WEB_PORTS = setOf(
        80, 443, 2052, 2053, 2082, 2083, 2086, 2087, 2095, 2096, 8080, 8443, 8880
    )

    private val CDN_ASN = mapOf(
        13335 to "Cloudflare", 209242 to "Cloudflare", 132892 to "Cloudflare",
        395747 to "Cloudflare", 203898 to "Cloudflare", 14789 to "Cloudflare",
        54113 to "Fastly", 394192 to "Fastly",
        20940 to "Akamai", 16625 to "Akamai", 32787 to "Akamai",
        35994 to "Akamai", 12222 to "Akamai",
        16509 to "CloudFront", 14618 to "CloudFront",
        15169 to "Google", 396982 to "Google",
        8075 to "Azure",
        199524 to "Gcore", 200325 to "BunnyCDN", 205585 to "ArvanCloud",
        57724 to "DDoS-Guard", 19551 to "Imperva", 30148 to "Sucuri",
        60068 to "CDN77", 12989 to "StackPath", 33438 to "StackPath",
        22822 to "Edgio", 45102 to "Alibaba", 132203 to "Tencent"
    )

    private val CLOUDFLARE_V4 = listOf(
        "173.245.48.0/20", "103.21.244.0/22", "103.22.200.0/22", "103.31.4.0/22",
        "141.101.64.0/18", "108.162.192.0/18", "190.93.240.0/20", "188.114.96.0/20",
        "197.234.240.0/22", "198.41.128.0/17", "162.158.0.0/15", "104.16.0.0/13",
        "104.24.0.0/14", "172.64.0.0/13", "131.0.72.0/22"
    )

    private fun ipToLong(ip: String): Long? {
        val parts = ip.split(".")
        if (parts.size != 4) return null
        var value = 0L
        for (part in parts) {
            val n = part.toIntOrNull() ?: return null
            if (n !in 0..255) return null
            value = (value shl 8) or n.toLong()
        }
        return value
    }

    private fun inCidr(ip: Long, cidr: String): Boolean {
        val slash = cidr.indexOf('/')
        if (slash < 0) return false
        val bits = cidr.substring(slash + 1).toIntOrNull() ?: return false
        val net = ipToLong(cidr.substring(0, slash)) ?: return false
        val mask = if (bits <= 0) 0L else (-1L shl (32 - bits)) and 0xFFFFFFFFL
        return (ip and mask) == (net and mask)
    }

    fun cdnName(ip: String, org: String, asn: String): String? {
        val value = ipToLong(ip)
        if (value != null && CLOUDFLARE_V4.any { inCidr(value, it) }) return "Cloudflare"
        val number = asn.removePrefix("AS").toIntOrNull()
        if (number != null) CDN_ASN[number]?.let { return it }
        val hay = (org + " " + asn).lowercase()
        return CDNS.firstOrNull { hay.contains(it.first) }?.second
    }

    private fun netName(n: String): String = when (n.trim().lowercase()) {
        "", "tcp", "raw" -> "TCP"
        "kcp", "mkcp" -> "mKCP"
        "ws", "websocket" -> "WebSocket"
        "httpupgrade" -> "HTTPUpgrade"
        "xhttp", "splithttp" -> "XHTTP"
        "grpc" -> "gRPC"
        "http", "h2", "http2" -> "HTTP/2"
        else -> n.uppercase()
    }

    private fun secName(s: String): String = when (s.trim().lowercase()) {
        "reality" -> "Reality"
        "tls" -> "TLS"
        else -> ""
    }

    private fun fronted(n: String): Boolean = when (n.trim().lowercase()) {
        "ws", "websocket", "xhttp", "splithttp", "httpupgrade", "http", "h2", "http2", "grpc" -> true
        else -> false
    }

    fun label(
        c: ProxyConfig,
        entryIp: String,
        entryOrg: String,
        entryAsn: String,
        entryCountry: String,
        exitCountry: String
    ): String {
        val proto = c.protocol.trim().lowercase()
        val transport = when (proto) {
            "wireguard" -> "WireGuard"
            "tor" -> "Tor"
            "aether" -> "MASQUE"
            "hysteria2" -> "QUIC"
            else -> ""
        }
        if (transport.isNotEmpty()) return "$proto / $transport"

        cdnName(entryIp, entryOrg, entryAsn)?.let { return "$proto / CDN ($it)" }

        if (entryCountry.isNotEmpty() && exitCountry.isNotEmpty() &&
            !entryCountry.equals(exitCountry, ignoreCase = true)
        ) return "$proto / Tunneled"

        val net = netName(c.network)
        val sec = secName(c.security)
        return if (sec.isEmpty()) "$proto / $net Direct" else "$proto / $net $sec Direct"
    }
}

object TunnelHealth {

    private val _alive = kotlinx.coroutines.flow.MutableStateFlow<Boolean?>(null)
    val alive: kotlinx.coroutines.flow.StateFlow<Boolean?> = _alive

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.IO
    )
    private var job: kotlinx.coroutines.Job? = null

    fun reset() {
        job?.cancel()
        _alive.value = null
    }

    fun check() {
        job?.cancel()
        job = scope.launch {
            _alive.value = null
            var settled = false
            var attempt = 0
            while (VpnState.state.value == Connection.CONNECTED) {
                val ip = withTimeoutOrNull(5000L) { ConfigProbe.exitIpNow(5000) } ?: ""
                if (ip.isNotEmpty()) {
                    _alive.value = true
                    settled = true
                } else if (!settled && attempt >= 2) {
                    _alive.value = false
                    settled = true
                } else if (settled) {
                    _alive.value = false
                }
                attempt++
                delay(if (_alive.value == true) 30000L else 4000L)
            }
        }
    }
}

object RadarRunner {

    private val _states = kotlinx.coroutines.flow.MutableStateFlow<Map<String, NetMonitor.State>>(emptyMap())
    val states: kotlinx.coroutines.flow.StateFlow<Map<String, NetMonitor.State>> = _states

    private val _running = kotlinx.coroutines.flow.MutableStateFlow(false)
    val running: kotlinx.coroutines.flow.StateFlow<Boolean> = _running

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.IO
    )
    private var job: kotlinx.coroutines.Job? = null

    fun start(viaTunnel: Boolean) {
        job?.cancel()
        job = scope.launch {
            _running.value = true
            _states.value = NetMonitor.Essential.associate {
                it.host to (NetMonitor.State.Testing as NetMonitor.State)
            }
            NetMonitor.probeAll(viaTunnel, NetMonitor.Essential) { site, state ->
                _states.value = _states.value + (site.host to state)
            }
            _running.value = false
        }
    }
}

object DebugRunner {

    private val _results = kotlinx.coroutines.flow.MutableStateFlow<Map<String, ProbeResult>>(emptyMap())
    val results: kotlinx.coroutines.flow.StateFlow<Map<String, ProbeResult>> = _results

    private val _running = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val running: kotlinx.coroutines.flow.StateFlow<Set<String>> = _running

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.Default
    )
    private var job: kotlinx.coroutines.Job? = null

    fun start(config: ProxyConfig, store: ConfigStore?) {
        job?.cancel()
        job = scope.launch {
            _running.value = _running.value + config.id
            val bad = ConfigDebug.inspect(config).firstOrNull { it.level == DebugLevel.BAD }
            val outcome = if (bad != null)
                ProbeResult(DebugState.BROKEN, -1, bad.noteKey, emptyList())
            else ConfigProbe.run(config, store)
            _results.value = _results.value + (config.id to outcome)
            _running.value = _running.value - config.id
        }
    }

    fun clear(id: String) {
        _results.value = _results.value - id
    }
}

object ConfigProbe {

    private const val CONNECT_MS = 3500
    private const val ALT_MS = 2500
    private const val TLS_MS = 5000
    private const val HTTP_MS = 5000
    private const val DNS_MS = 5000L
    private const val CONTROL_HOST = "cloudflare.com"

    private val POISON = setOf(
        "10.10.34.34", "10.10.34.35", "10.10.34.36",
        "10.10.35.34", "10.10.35.35", "10.10.35.36",
        "0.0.0.0", "127.0.0.1", "::1"
    )

    private val IP_LITERAL = Regex("^[0-9.]+$|^[0-9a-fA-F:]+:[0-9a-fA-F:]*$")

    private sealed interface Dial {
        data class Ok(val ms: Int) : Dial
        data object Refused : Dial
        data object Timeout : Dial
    }

    private data class TlsResult(val kind: Tls, val detail: String)

    private sealed interface SniScan {
        data object Ok : SniScan
        data object NoResolve : SniScan
        data object Hijacked : SniScan
        data object Poisoned : SniScan
        data object Filtered : SniScan
        data object NoTls13 : SniScan
        data object Unreachable : SniScan
    }

    private sealed interface Tls {
        data object Ok : Tls
        data object NameMismatch : Tls
        data object CertError : Tls
        data object Reset : Tls
        data object Timeout : Tls
    }

    private fun check(part: String, level: DebugLevel, note: String, value: String) =
        DebugCheck(part, level, note, value)

    private suspend fun dial(host: String, port: Int, timeout: Int = CONNECT_MS): Dial =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { s ->
                    val t0 = System.currentTimeMillis()
                    s.connect(InetSocketAddress(host, port), timeout)
                    Dial.Ok((System.currentTimeMillis() - t0).toInt())
                }
            } catch (e: ConnectException) {
                Dial.Refused
            } catch (e: SocketTimeoutException) {
                Dial.Timeout
            } catch (e: Exception) {
                val m = (e.message ?: "").lowercase()
                if (m.contains("refused") || m.contains("reset")) Dial.Refused else Dial.Timeout
            }
        }

    private suspend fun resolve(host: String): List<String> = withContext(Dispatchers.IO) {
        runCatching {
            InetAddress.getAllByName(host).mapNotNull { it.hostAddress }
        }.getOrDefault(emptyList())
    }

    private val trustAll = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private fun certNames(cert: X509Certificate): List<String> {
        val names = ArrayList<String>()
        runCatching {
            cert.subjectAlternativeNames?.forEach { entry ->
                val type = entry.elementAtOrNull(0) as? Int
                val value = entry.elementAtOrNull(1) as? String
                if (type == 2 && !value.isNullOrBlank()) names.add(value.trim().lowercase())
            }
        }
        runCatching {
            Regex("CN=([^,]+)").find(cert.subjectX500Principal.name)
                ?.groupValues?.get(1)?.trim()?.lowercase()?.let { names.add(it) }
        }
        return names
    }

    private fun nameMatches(sni: String, names: List<String>): Boolean {
        val host = sni.trim().lowercase()
        if (host.isEmpty() || names.isEmpty()) return false
        return names.any { name ->
            if (name.startsWith("*.")) {
                val suffix = name.substring(1)
                val head = host.removeSuffix(suffix)
                host.endsWith(suffix) && head.isNotEmpty() && !head.contains('.')
            } else name == host
        }
    }

    private suspend fun scanSni(sni: String): SniScan = withContext(Dispatchers.IO) {
        val host = sni.trim()
        if (host.isEmpty() || IP_LITERAL.matches(host)) return@withContext SniScan.Ok
        val ips = runCatching { InetAddress.getAllByName(host).mapNotNull { it.hostAddress } }
            .getOrDefault(emptyList())
        if (ips.isEmpty()) return@withContext SniScan.NoResolve
        if (ips.any { it in POISON }) return@withContext SniScan.Poisoned
        var plain: Socket? = null
        try {
            val s = Socket()
            s.connect(InetSocketAddress(ips.first(), 443), CONNECT_MS)
            s.soTimeout = TLS_MS
            plain = s
            val factory = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
            }.socketFactory
            val ssl = factory.createSocket(s, host, 443, true) as SSLSocket
            ssl.soTimeout = TLS_MS
            runCatching {
                val params = ssl.sslParameters
                params.serverNames = listOf(SNIHostName(host))
                ssl.sslParameters = params
            }
            ssl.startHandshake()
            val protocol = runCatching { ssl.session.protocol ?: "" }.getOrDefault("")
            val leaf = runCatching {
                ssl.session.peerCertificates.firstOrNull() as? X509Certificate
            }.getOrNull()
            runCatching { ssl.close() }
            when {
                leaf == null -> SniScan.Hijacked
                !nameMatches(host, certNames(leaf)) -> SniScan.Hijacked
                protocol.contains("1.3") -> SniScan.Ok
                else -> SniScan.NoTls13
            }
        } catch (e: SocketTimeoutException) {
            SniScan.Filtered
        } catch (e: Exception) {
            val m = (e.message ?: "").lowercase()
            if (m.contains("reset") || m.contains("eof") || m.contains("broken pipe"))
                SniScan.Filtered else SniScan.Unreachable
        } finally {
            runCatching { plain?.close() }
        }
    }

    private suspend fun tlsProbe(
        host: String,
        port: Int,
        sni: String,
        strict: Boolean
    ): TlsResult = withContext(Dispatchers.IO) {
        var plain: Socket? = null
        try {
            val s = Socket()
            s.connect(InetSocketAddress(host, port), CONNECT_MS)
            s.soTimeout = TLS_MS
            plain = s
            val factory = if (strict) SSLSocketFactory.getDefault() as SSLSocketFactory
            else SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
            }.socketFactory
            val ssl = factory.createSocket(s, sni, port, true) as SSLSocket
            ssl.soTimeout = TLS_MS
            runCatching {
                val params = ssl.sslParameters
                params.serverNames = listOf(SNIHostName(sni))
                if (strict) params.endpointIdentificationAlgorithm = "HTTPS"
                ssl.sslParameters = params
            }
            ssl.startHandshake()
            val leaf = runCatching {
                ssl.session.peerCertificates.firstOrNull() as? X509Certificate
            }.getOrNull()
            runCatching { ssl.close() }
            if (!strict && leaf == null) TlsResult(Tls.Reset, "")
            else TlsResult(Tls.Ok, "")
        } catch (e: SSLHandshakeException) {
            val m = (e.message ?: "").lowercase()
            val detail = "hs " + e.javaClass.simpleName + " " + (e.message ?: "").take(90)
            when {
                m.contains("subject alternative") || m.contains("not verified") ||
                        m.contains("does not match") || m.contains("hostname") ->
                    TlsResult(Tls.NameMismatch, detail)
                m.contains("cert") || m.contains("trust") || m.contains("chain") ||
                        m.contains("verif") -> TlsResult(Tls.CertError, detail)
                else -> TlsResult(Tls.Reset, detail)
            }
        } catch (e: SocketTimeoutException) {
            TlsResult(Tls.Timeout, "timeout")
        } catch (e: Exception) {
            val m = (e.message ?: "").lowercase()
            val detail = e.javaClass.simpleName + " " + (e.message ?: "").take(90)
            if (m.contains("reset") || m.contains("broken pipe") || m.contains("eof"))
                TlsResult(Tls.Reset, detail) else TlsResult(Tls.Timeout, detail)
        } finally {
            runCatching { plain?.close() }
        }
    }

    private suspend fun transportProbe(
        addr: String,
        port: Int,
        sni: String,
        hostHeader: String,
        path: String,
        network: String,
        serviceName: String,
        useTls: Boolean
    ): Int = withContext(Dispatchers.IO) {
        var sock: Socket? = null
        try {
            val s = Socket()
            s.connect(InetSocketAddress(addr, port), CONNECT_MS)
            s.soTimeout = TLS_MS
            sock = s
            val name = sni.ifBlank { hostHeader }.ifBlank { addr }
            val stream: Socket = if (useTls) {
                val factory = SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
                }.socketFactory
                val ssl = factory.createSocket(s, name, port, true) as SSLSocket
                ssl.soTimeout = TLS_MS
                runCatching {
                    val params = ssl.sslParameters
                    params.serverNames = listOf(SNIHostName(name))
                    ssl.sslParameters = params
                }
                ssl.startHandshake()
                ssl
            } else s
            val hostLine = hostHeader.ifBlank { name }
            val target = if (path.startsWith("/")) path else "/" + path
            val agent = "User-Agent: Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36\r\n" +
                    "Accept: */*\r\nAccept-Language: en-US,en;q=0.9\r\n"
            val request = if (network == "grpc")
                "POST /" + serviceName.trim('/') + "/Tun HTTP/1.1\r\n" +
                        "Host: " + hostLine + "\r\n" + agent +
                        "content-type: application/grpc\r\nte: trailers\r\n" +
                        "Connection: close\r\n\r\n"
            else
                "GET " + target + " HTTP/1.1\r\n" +
                        "Host: " + hostLine + "\r\n" + agent +
                        "Upgrade: websocket\r\nConnection: Upgrade\r\n" +
                        "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" +
                        "Sec-WebSocket-Version: 13\r\n\r\n"
            stream.getOutputStream().apply {
                write(request.toByteArray())
                flush()
            }
            val line = stream.getInputStream().bufferedReader().readLine().orEmpty()
            runCatching { stream.close() }
            Regex("HTTP/1\\.[01] (\\d{3})").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: -1
        } catch (e: Exception) {
            -1
        } finally {
            runCatching { sock?.close() }
        }
    }

    private suspend fun httpProbe(host: String, path: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val p = if (path.startsWith("/")) path else "/$path"
                val conn = URL("https://$host$p").openConnection() as HttpsURLConnection
                conn.connectTimeout = CONNECT_MS
                conn.readTimeout = HTTP_MS
                conn.instanceFollowRedirects = false
                conn.requestMethod = "GET"
                val code = conn.responseCode
                runCatching { conn.disconnect() }
                code > 0
            }.getOrDefault(false)
        }

    private val STRICT_SITES = listOf(
        "https://gemini.google.com/",
        "https://aistudio.google.com/",
        "https://claude.ai/"
    )

    private suspend fun vpnExposed(): Boolean {
        if (!tunnelReady()) return false
        return coroutineScope {
            STRICT_SITES.map { url ->
                async(Dispatchers.IO) {
                    runCatching {
                        val conn = URL(url).openConnection(socksProxy()) as HttpsURLConnection
                        conn.connectTimeout = 9000
                        conn.readTimeout = 9000
                        conn.instanceFollowRedirects = false
                        conn.requestMethod = "GET"
                        conn.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"
                        )
                        val code = conn.responseCode
                        runCatching { conn.disconnect() }
                        code == 403
                    }.getOrDefault(false)
                }
            }.awaitAll().any { it }
        }
    }

    private fun socksProxy(): Proxy = Proxy(
        Proxy.Type.SOCKS,
        InetSocketAddress("127.0.0.1", MixedPort.value)
    )

    private fun tunnelReady(): Boolean =
        VpnState.state.value == Connection.CONNECTED && activeConfigId == VpnState.activeId.value

    @Volatile
    private var activeConfigId: String? = null

    suspend fun exitIpNow(timeoutMs: Int = 5000): String = withContext(Dispatchers.IO) {
        if (VpnState.state.value != Connection.CONNECTED) return@withContext ""
        runCatching {
            val conn = URL("https://api4.ipify.org")
                .openConnection(socksProxy()) as HttpsURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            val body = conn.inputStream.bufferedReader().use { it.readText() }.trim()
            runCatching { conn.disconnect() }
            body
        }.getOrDefault("")
    }

    private suspend fun exitIp(c: ProxyConfig): String = withContext(Dispatchers.IO) {
        if (!tunnelReady()) return@withContext ""
        runCatching {
            val conn = URL("https://api4.ipify.org").openConnection(socksProxy()) as HttpsURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            val body = conn.inputStream.bufferedReader().use { it.readText() }.trim()
            runCatching { conn.disconnect() }
            body
        }.getOrDefault("")
    }

    private suspend fun collectInfo(c: ProxyConfig, entryIp: String): ProbeInfo {
        val exit = exitIp(c)

        val entryIntel = if (entryIp.isNotEmpty()) IpIntelligence.lookup(entryIp) else null
        val exitIntel = if (exit.isNotEmpty() && exit != entryIp)
            IpIntelligence.lookup(exit) else entryIntel
        val shown = exitIntel ?: entryIntel

        return ProbeInfo(
            method = TransportMethod.label(
                c,
                entryIp,
                entryIntel?.org ?: "",
                entryIntel?.asn ?: "",
                entryIntel?.countryCode ?: "",
                exitIntel?.countryCode ?: ""
            ),
            entryIp = entryIp,
            entryIsp = entryIntel?.org ?: "",
            entryCountry = entryIntel?.countryCode ?: "",
            exitIp = exit,
            exitIsp = exitIntel?.org ?: "",
            exitCountry = exitIntel?.countryCode ?: "",
            kind = shown?.kind ?: "",
            reputation = shown?.reputation ?: -1,
            repBand = shown?.repBand ?: "",
            flags = shown?.flags ?: "",
            flagged = shown?.flagged ?: false,
            vpnExposed = vpnExposed()
        )
    }

    private suspend fun coreDelay(c: ProxyConfig, chain: ProxyConfig?): Long =
        withContext(Dispatchers.IO) {
            runCatching {
                Gozarcore.measureDelay(ConfigBuilder.buildForTest(c, chain))
            }.getOrDefault(-1L)
        }

    private suspend fun checkHost(target: String, kind: String): Boolean? {
        val session = withTimeoutOrNull(15000L) { CheckHost.start(target, kind, 14) } ?: return null
        repeat(10) {
            delay(1500)
            val res = CheckHost.poll(session.requestId) ?: return@repeat
            if (res.values.any { it is CheckHost.NodeResult.Ok }) return true
            if (res.isNotEmpty() && res.values.none { it is CheckHost.NodeResult.Pending }) return false
        }
        return null
    }

    private suspend fun remotePortUp(c: ProxyConfig, addr: String, port: Int): Boolean? =
        checkHost("$addr:$port", if (udpProtocol(c)) "udp" else "tcp")

    private suspend fun remoteAddrUp(addr: String): Boolean? = checkHost(addr, "ping")

    private suspend fun localAddrUp(addr: String, port: Int): Boolean {
        if (port != 443 && dial(addr, 443, ALT_MS) is Dial.Ok) return true
        if (port != 80 && dial(addr, 80, ALT_MS) is Dial.Ok) return true
        if (dial(addr, 53, ALT_MS) is Dial.Ok) return true
        return false
    }

    private fun udpProtocol(c: ProxyConfig): Boolean = when (c.protocol.trim().lowercase()) {
        "hysteria2", "wireguard" -> true
        else -> false
    }

    private fun normalizeNetwork(network: String): String = when (network.trim().lowercase()) {
        "", "raw" -> "tcp"
        "mkcp" -> "kcp"
        "websocket" -> "ws"
        "h2", "http2" -> "http"
        "splithttp" -> "xhttp"
        else -> network.trim().lowercase()
    }

    private fun fronted(network: String): Boolean = when (network.trim().lowercase()) {
        "ws", "websocket", "xhttp", "splithttp", "httpupgrade", "http", "h2", "http2" -> true
        else -> false
    }

    private fun chainOf(c: ProxyConfig, store: ConfigStore?): ProxyConfig? {
        if (c.chainId.isEmpty()) return null
        return store?.configs?.value?.firstOrNull { it.id == c.chainId && it.id != c.id }
    }

    private suspend fun quotaCheck(c: ProxyConfig, store: ConfigStore?): DebugCheck? {
        val sub = store?.subscriptions?.value?.firstOrNull { it.id == c.subId } ?: return null
        if (!(sub.total > 0 || sub.expire > 0)) return null
        val now = System.currentTimeMillis() / 1000
        if (sub.expire in 1 until now)
            return check("dbg_part_quota", DebugLevel.BAD, "dbg_sub_expired", sub.name)
        if (sub.total > 0 && sub.used >= sub.total)
            return check("dbg_part_quota", DebugLevel.BAD, "dbg_sub_exhausted", sub.name)
        if (sub.total > 0 && sub.used > sub.total * 9 / 10)
            return check("dbg_part_quota", DebugLevel.WARN, "dbg_sub_low", sub.name)
        if (sub.expire > 0 && sub.expire - now < 3 * 86400)
            return check("dbg_part_quota", DebugLevel.WARN, "dbg_sub_ending", sub.name)
        return null
    }

    private suspend fun unreachable(
        c: ProxyConfig,
        addr: String,
        port: Int,
        refused: Boolean,
        findings: List<DebugCheck>
    ): ProbeResult {
        if (refused) return ProbeResult(
            DebugState.OFFLINE, -1, "dbg_addr_mismatch",
            findings + check("dbg_part_port", DebugLevel.BAD, "dbg_addr_mismatch", "$port")
        )
        if (dial(CONTROL_HOST, 443, ALT_MS) !is Dial.Ok) return ProbeResult(
            DebugState.TIMEOUT, -1, "dbg_no_internet",
            findings + check("dbg_part_address", DebugLevel.WARN, "dbg_no_internet", addr)
        )

        val remotePort = remotePortUp(c, addr, port)
        val remoteAddr = remoteAddrUp(addr)
        if (remotePort == null && remoteAddr == null) return ProbeResult(
            DebugState.TIMEOUT, -1, "dbg_undetermined",
            findings + check("dbg_part_remote", DebugLevel.WARN, "dbg_undetermined", "")
        )

        if (remotePort == true && remoteAddr != false) {
            val userAddr = localAddrUp(addr, port)
            val note = if (userAddr) "dbg_port_blocked" else "dbg_ip_blocked"
            val part = if (userAddr) "dbg_part_port" else "dbg_part_address"
            return ProbeResult(
                DebugState.BLOCKED, -1, note,
                findings +
                        check(part, DebugLevel.BAD, note, if (userAddr) "$port" else addr)
            )
        }

        val note = if (remoteAddr == true) "dbg_addr_mismatch" else "dbg_addr_offline"
        val part = if (remoteAddr == true) "dbg_part_port" else "dbg_part_address"
        return ProbeResult(
            DebugState.OFFLINE, -1, note,
            findings +
                    check(part, DebugLevel.BAD, note, if (remoteAddr == true) "$port" else addr)
        )
    }

    suspend fun run(c: ProxyConfig, store: ConfigStore? = null): ProbeResult {
        activeConfigId = c.id
        val findings = ArrayList<DebugCheck>()
        val addr = c.address.trim()
        val port = c.port

        if (c.protocol == "tor" || c.protocol == "aether" || c.protocol == "ikev2")
            return ProbeResult(DebugState.TIMEOUT, -1, "dbg_udp_probe", findings)
        if (addr.isEmpty() || port !in 1..65535)
            return ProbeResult(DebugState.BROKEN, -1, "dbg_empty", findings)

        var entryIp = if (IP_LITERAL.matches(addr)) addr else ""

        if (!IP_LITERAL.matches(addr)) {
            val ips = withTimeoutOrNull(DNS_MS) { resolve(addr) } ?: emptyList()
            if (ips.isEmpty()) {
                val ctrl = withTimeoutOrNull(DNS_MS) { resolve(CONTROL_HOST) } ?: emptyList()
                return if (ctrl.isEmpty()) ProbeResult(
                    DebugState.TIMEOUT, -1, "dbg_no_internet",
                    findings + check("dbg_part_dns", DebugLevel.WARN, "dbg_no_internet", addr)
                ) else ProbeResult(
                    DebugState.BROKEN, -1, "dbg_dns_fail",
                    findings + check("dbg_part_dns", DebugLevel.BAD, "dbg_dns_fail", addr)
                )
            }
            val poisoned = ips.firstOrNull { it in POISON }
            if (poisoned != null) return ProbeResult(
                DebugState.BLOCKED, -1, "dbg_domain_filtered",
                findings + check("dbg_part_domain", DebugLevel.BAD, "dbg_domain_filtered", addr)
            )
            entryIp = ips.first()
            findings += check("dbg_part_dns", DebugLevel.OK, "", entryIp)
        }

        if (udpProtocol(c)) {
            findings += check("dbg_part_probe", DebugLevel.WARN, "dbg_local_udp", "")
            val remote = remotePortUp(c, addr, port)
            if (remote == true) {
                findings += check("dbg_part_remote", DebugLevel.OK, "", "check-host")
                val real = coreDelay(c, chainOf(c, store))
                if (real < 0) {
                    val quota = quotaCheck(c, store)
                    if (quota != null && quota.level == DebugLevel.BAD) return ProbeResult(
                        DebugState.BROKEN, -1, quota.noteKey, findings + quota
                    )
                    return ProbeResult(
                        DebugState.BROKEN, -1, "dbg_handshake_failed",
                        findings + check("dbg_part_core", DebugLevel.BAD, "dbg_handshake_failed", "")
                    )
                }
                quotaCheck(c, store)?.let { findings += it }
                findings += check("dbg_part_core", DebugLevel.OK, "", "$real")
                val udpInfo = collectInfo(c, entryIp)
                if (udpInfo.flagged) findings += check(
                    "dbg_part_iptype", DebugLevel.WARN, "dbg_rep_flagged", udpInfo.flags
                )
                if (udpInfo.vpnExposed) findings += check(
                    "dbg_part_exposure", DebugLevel.WARN, "dbg_vpn_exposed", ""
                )
                return ProbeResult(DebugState.HEALTHY, real.toInt(), "", findings, udpInfo)
            }
            return unreachable(c, addr, port, false, findings)
        }

        var ping = -1
        when (val main = dial(addr, port)) {
            is Dial.Ok -> {
                ping = main.ms
                findings += check("dbg_part_tcp", DebugLevel.OK, "", "${main.ms}")
            }
            Dial.Refused -> return unreachable(c, addr, port, true, findings)
            Dial.Timeout -> return unreachable(c, addr, port, false, findings)
        }

        var tlsIssue = ""
        val sec = c.security.trim().lowercase()
        if (sec == "tls" || sec == "reality") {
            val sni = c.sni.trim()
                .ifBlank { c.host.substringBefore(",").trim() }
                .ifBlank { addr }
            val sniMustResolve = sec == "reality" || sni.equals(addr, ignoreCase = true)
            if (!sniMustResolve) {
                findings += check("dbg_part_sni", DebugLevel.OK, "", sni)
            } else when (scanSni(sni)) {
                SniScan.Ok -> findings += check("dbg_part_sni", DebugLevel.OK, "", sni)
                SniScan.NoResolve -> tlsIssue = "dbg_sni_no_resolve"
                SniScan.Hijacked -> tlsIssue = "dbg_sni_no_resolve"
                SniScan.Poisoned -> tlsIssue = "dbg_sni_filtered"
                SniScan.Filtered -> tlsIssue = "dbg_sni_filtered"
                SniScan.Unreachable -> tlsIssue = "dbg_sni_unreachable"
                SniScan.NoTls13 -> if (sec == "reality") tlsIssue = "dbg_sni_no_tls13"
                else findings += check("dbg_part_sni", DebugLevel.WARN, "dbg_sni_no_tls13", sni)
            }

            val pinned = CertPin.isValid(c.pinnedCertSha256)
            val tls = tlsProbe(addr, port, sni, sec == "tls" && !pinned)
            when (tls.kind) {
                Tls.Ok -> if (!pinned) findings += check("dbg_part_tls", DebugLevel.OK, "", sni)
                Tls.NameMismatch -> if (sec != "reality" && !pinned) tlsIssue = "dbg_sni_mismatch"
                Tls.CertError -> if (sec != "reality" && !pinned) return ProbeResult(
                    DebugState.BROKEN, ping,
                    if (c.allowInsecure) "dbg_tls_cert_insecure" else "dbg_tls_cert",
                    findings + check(
                        "dbg_part_tls", DebugLevel.BAD,
                        if (c.allowInsecure) "dbg_tls_cert_insecure" else "dbg_tls_cert", sni
                    )
                )
                Tls.Reset -> if (sec != "reality") tlsIssue = "dbg_sni_blocked"
                Tls.Timeout -> if (sec != "reality") tlsIssue = "dbg_sni_timeout"
            }

            if (pinned) {
                val actual = CertPin.fetch(addr, port, sni)
                val expected = c.pinnedCertSha256.split(',')
                    .map { it.trim().replace(":", "").lowercase() }
                    .filter { it.isNotEmpty() }
                when {
                    actual == null -> findings += check(
                        "dbg_part_pin", DebugLevel.WARN, "dbg_pin_unreachable", sni
                    )
                    actual.lowercase() in expected -> findings += check(
                        "dbg_part_pin", DebugLevel.OK, "", actual.take(16)
                    )
                    else -> return ProbeResult(
                        DebugState.BROKEN, ping, "dbg_pin_mismatch",
                        findings + check(
                            "dbg_part_pin", DebugLevel.BAD, "dbg_pin_mismatch", actual.take(16)
                        )
                    )
                }
            }
        }

        var hostIssue = ""
        var pathIssue = ""
        var pathCode = ""
        val hostHeader = c.host.substringBefore(",").trim()
        if (fronted(c.network)) {
            if (hostHeader.isNotEmpty() && !hostHeader.equals(addr, ignoreCase = true)) {
                if (httpProbe(hostHeader, c.path.ifBlank { "/" }))
                    findings += check("dbg_part_host", DebugLevel.OK, "", hostHeader)
                else hostIssue = "dbg_host_blocked"
            }
            val code = transportProbe(
                addr, port,
                c.sni.trim().ifBlank { hostHeader },
                hostHeader,
                c.path.ifBlank { "/" },
                normalizeNetwork(c.network),
                c.serviceName,
                sec == "tls" || sec == "reality"
            )
            if (code == 101 || code == 200)
                findings += check("dbg_part_path", DebugLevel.OK, "", c.path.ifBlank { "/" })
            else {
                pathIssue = "dbg_path_unverified"
                pathCode = if (code > 0) "HTTP $code" else "no response"
            }
        }

        val generic = if (c.security.equals("reality", ignoreCase = true))
            "dbg_reality_refused" else "dbg_handshake_failed"

        val real = coreDelay(c, chainOf(c, store))
        if (real < 0) {
            val quota = quotaCheck(c, store)
            if (quota != null && quota.level == DebugLevel.BAD) return ProbeResult(
                DebugState.BROKEN, ping, quota.noteKey, findings + quota
            )
            if (pathIssue.isNotEmpty())
                findings += check("dbg_part_path", DebugLevel.WARN, pathIssue, pathCode)
            val cause = tlsIssue.ifEmpty { hostIssue }.ifEmpty { generic }
            val part = when {
                tlsIssue.isNotEmpty() -> "dbg_part_sni"
                hostIssue.isNotEmpty() -> "dbg_part_host"
                else -> "dbg_part_core"
            }
            return ProbeResult(
                DebugState.BROKEN, ping, cause,
                findings + check(part, DebugLevel.BAD, cause, "")
            )
        }
        findings += check("dbg_part_core", DebugLevel.OK, "", "$real")
        if (tlsIssue.isNotEmpty())
            findings += check("dbg_part_sni", DebugLevel.WARN, tlsIssue, "")
        if (hostIssue.isNotEmpty())
            findings += check("dbg_part_host", DebugLevel.WARN, hostIssue, hostHeader)
        if (pathIssue.isNotEmpty())
            findings += check("dbg_part_path", DebugLevel.WARN, pathIssue, pathCode)
        quotaCheck(c, store)?.let { findings += it }

        val info = collectInfo(c, entryIp)
        if (info.exitIp.isBlank() && tunnelReady()) return ProbeResult(
            DebugState.BROKEN, ping, generic,
            findings + check("dbg_part_core", DebugLevel.BAD, "dbg_no_traffic", "")
        )
        if (info.flagged) findings += check(
            "dbg_part_iptype", DebugLevel.WARN, "dbg_rep_flagged", info.flags
        )
        if (info.vpnExposed) findings += check(
            "dbg_part_exposure", DebugLevel.WARN, "dbg_vpn_exposed", ""
        )
        if (info.exitIp.isNotBlank() && !info.exitIp.equals(entryIp, ignoreCase = true) &&
            dial(info.exitIp, 443, ALT_MS) is Dial.Timeout
        ) findings += check("dbg_part_ip", DebugLevel.WARN, "dbg_exit_blocked", info.exitIp)
        return ProbeResult(DebugState.HEALTHY, real.toInt(), "", findings, info)
    }
}