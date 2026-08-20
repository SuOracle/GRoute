package net.gozar.app

import gozarcore.Gozarcore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

object FreeConfigs {

    const val CHANNEL = "ConfigsHUB"
    const val SOURCE_URL = "https://t.me/s/$CHANNEL"

    private const val TAG = "GRouteFree"
    private const val CONCURRENCY = 16
    private const val MESSAGES = 50
    private const val MAX_TEST = 200
    private const val MAX_PAGES = 8
    private const val HTTP_TIMEOUT = 12_000
    private const val MEASURE_TIMEOUT_MS = 20_000L

    const val CONFIG_NAME = "GRoute-Free"


    private val LINK = Regex(
        "(?:vless|vmess|trojan|ss|hysteria2|hy2)://[^\\s\"'<>\\\\]+",
        RegexOption.IGNORE_CASE
    )
    private val POST_ID = Regex("data-post=\"[^/\"]+/(\\d+)\"")

    data class Progress(val tested: Int, val total: Int, val alive: Int)

    private val _progress = MutableStateFlow<Progress?>(null)
    val progress: StateFlow<Progress?> = _progress.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun subscriptionOf(store: ConfigStore): Subscription? =
        store.subscriptions.value.firstOrNull { it.url == SOURCE_URL }

    fun isAdded(store: ConfigStore): Boolean = subscriptionOf(store) != null

    private fun route(): Proxy =
        if (VpnState.state.value == Connection.CONNECTED && !IkeController.active)
            Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MixedPort.value))
        else Proxy.NO_PROXY

    private fun unescape(raw: String): String = raw
        .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
        .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ")

    private fun page(before: Long): String = runCatching {
        val url = if (before <= 0) SOURCE_URL else "$SOURCE_URL?before=$before"
        val conn = (URL(url).openConnection(route()) as HttpURLConnection).apply {
            connectTimeout = HTTP_TIMEOUT
            readTimeout = HTTP_TIMEOUT
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            setRequestProperty("Accept-Language", "en")
        }
        val code = conn.responseCode
        val body = if (code in 200..299)
            conn.inputStream.bufferedReader().use { it.readText() } else ""
        runCatching { conn.disconnect() }
        body
    }.getOrDefault("")

    data class Scrape(val reachable: Boolean, val messages: Int, val configs: List<ProxyConfig>)

    private suspend fun scrape(): Scrape = withContext(Dispatchers.IO) {
        val seen = LinkedHashMap<String, ProxyConfig>()
        var before = 0L
        var pages = 0
        var messages = 0
        var reachable = false
        var links = 0

        while (pages < MAX_PAGES && messages < MESSAGES) {
            val html = page(before)
            if (html.isBlank()) break
            reachable = true

            val ids = POST_ID.findAll(html).mapNotNull { it.groupValues[1].toLongOrNull() }.toList()
            if (ids.isEmpty()) break
            val room = MESSAGES - messages
            val take = ids.sortedDescending().take(room).toSet()
            messages += take.size

            LINK.findAll(html).forEach { m ->
                links++
                val uri = unescape(m.value)
                val cfg = runCatching { ConfigParser.parse(uri, ConfigSource.COMMUNITY) }.getOrNull()
                if (cfg != null && cfg.address.isNotBlank() && cfg.port in 1..65535) {
                    seen.putIfAbsent(sig(cfg), rename(cfg))
                }
            }

            val oldest = ids.minOrNull() ?: break
            if (before != 0L && oldest >= before) break
            before = oldest
            pages++
        }

        android.util.Log.i(
            TAG,
            "pages=$pages messages=$messages links=$links parsed=${seen.size} reachable=$reachable"
        )
        Scrape(reachable, messages, seen.values.toList())
    }

    private fun rename(cfg: ProxyConfig): ProxyConfig = cfg.copy(name = CONFIG_NAME)

    private fun sig(c: ProxyConfig): String =
        "${c.protocol}|${c.address}|${c.port}|${c.uuid}|${c.password}"

    const val BUSY = -1
    const val UNREACHABLE = -2
    const val NO_CONFIGS = -3

    suspend fun refresh(store: ConfigStore, label: String): Int {
        if (_busy.value) return BUSY
        _busy.value = true
        try {
            val found = scrape()
            if (!found.reachable) {
                android.util.Log.w(TAG, "channel unreachable")
                return UNREACHABLE
            }
            if (found.configs.isEmpty()) {
                android.util.Log.w(TAG, "no parsable configs in ${found.messages} messages")
                return NO_CONFIGS
            }
            val existing = subscriptionOf(store)
            val current = if (existing == null) emptyList()
            else store.configs.value.filter { it.subId == existing.id }

            val merged = (current + found.configs).distinctBy { sig(it) }.take(MAX_TEST)
            android.util.Log.i(
                TAG,
                "kept=${current.size} scraped=${found.configs.size} merged=${merged.size}"
            )

            val alive = measureAll(merged)
            val sub = (existing ?: Subscription(name = label, url = SOURCE_URL))
                .copy(lastUpdated = System.currentTimeMillis())
            if (alive.isNotEmpty() || existing != null) store.upsertSubscription(sub, alive)
            android.util.Log.i(TAG, "kept ${alive.size} of ${found.configs.size}")
            return alive.size
        } finally {
            _busy.value = false
            _progress.value = null
        }
    }

    private suspend fun measureAll(configs: List<ProxyConfig>): List<ProxyConfig> = coroutineScope {
        val total = configs.size
        val alive = java.util.Collections.synchronizedList(mutableListOf<Pair<ProxyConfig, Int>>())
        var done = 0
        val lock = Any()
        _progress.value = Progress(0, total, 0)

        val sem = Semaphore(CONCURRENCY)
        configs.map { cfg ->
            launch {
                sem.withPermit {
                    val ms = withContext(Dispatchers.IO) {
                        runCatching {
                            kotlinx.coroutines.withTimeoutOrNull(MEASURE_TIMEOUT_MS) {
                                Gozarcore.measureDelay(ConfigBuilder.buildForTest(cfg))
                            } ?: -1L
                        }.getOrDefault(-1L)
                    }
                    if (ms >= 0) alive += cfg to ms.toInt()
                    else android.util.Log.d(TAG, "dead ${cfg.protocol} ${cfg.address}:${cfg.port}")
                    synchronized(lock) {
                        done++
                        _progress.value = Progress(done, total, alive.size)
                    }
                }
            }
        }.joinAll()

        alive.sortedBy { it.second }.map { it.first }
    }

}
