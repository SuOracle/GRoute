package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

data class WindscribeNode(
    val label: String,
    val hostname: String,
    val ip: String,
    val country: String,
    val premium: Boolean
)

object WindscribeFetcher {

    private const val LIST_FREE = "https://assets.windscribe.com/serverlist/mob-v2/0/"
    private const val LIST_ALL = "https://assets.windscribe.com/serverlist/mob-v2/1/"
    private const val MIRROR =
        "https://raw.githubusercontent.com/tn3w/Windscribe-IPs/master/windscribe_serverlist.json"

    suspend fun fetch(): List<WindscribeNode> = withContext(Dispatchers.IO) {
        val stamp = System.currentTimeMillis() / 1000
        val out = LinkedHashMap<String, WindscribeNode>()
        val sources = listOf(MIRROR, LIST_ALL + stamp, LIST_FREE + stamp)
        for (url in sources) {
            val body = get(url) ?: continue
            parse(body, out)
            if (out.isNotEmpty()) break
        }
        out.values.sortedWith(compareBy({ it.country }, { it.label }))
    }

    private fun get(url: String): String? = runCatching {
        val conn = (URL(url).openConnection(Proxy.NO_PROXY) as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            setRequestProperty("Accept", "application/json")
        }
        val code = conn.responseCode
        if (code !in 200..299) {
            runCatching { conn.disconnect() }
            return@runCatching null
        }
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        runCatching { conn.disconnect() }
        text
    }.getOrNull()

    private fun parse(body: String, out: LinkedHashMap<String, WindscribeNode>) {
        runCatching {
            val trimmed = body.trim()
            val locations: JSONArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val root = JSONObject(trimmed)
                root.optJSONArray("data")
                    ?: root.optJSONObject("data")?.optJSONArray("locations")
                    ?: root.optJSONArray("locations")
                    ?: return
            }
            for (i in 0 until locations.length()) {
                val loc = locations.optJSONObject(i) ?: continue
                val country = loc.optString("country_code")
                val region = loc.optString("name")
                val locPro = loc.optInt("premium_only", 0) == 1
                val groups = loc.optJSONArray("groups") ?: continue
                for (g in 0 until groups.length()) {
                    val grp = groups.optJSONObject(g) ?: continue
                    val wg = grp.optString("wg_endpoint").trim()
                    val host = when {
                        wg.contains("-wg.") -> wg.replace("-wg.", "-ike.")
                        else -> ""
                    }
                    if (host.isEmpty()) continue
                    val ip = grp.optString("ping_ip").trim()
                    val city = grp.optString("city")
                    val nick = grp.optString("nick")
                    val label = listOf(region, city, nick)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(" \u00b7 ")
                        .ifBlank { host.substringBefore('.') }
                    out[host] = WindscribeNode(
                        label = label,
                        hostname = host,
                        ip = ip,
                        country = country,
                        premium = locPro || grp.optInt("pro", 0) == 1
                    )
                }
            }
        }
    }
}