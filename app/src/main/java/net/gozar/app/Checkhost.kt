package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object CheckHost {

    private const val BASE = "https://check-host.net"

    data class Node(
        val id: String,
        val country: String,
        val countryCode: String,
        val city: String
    )

    sealed class NodeResult {
        object Pending : NodeResult()
        data class Ok(val avgMs: Double, val loss: Int, val ip: String) : NodeResult()
        object Failed : NodeResult()
    }

    data class Session(val requestId: String, val nodes: List<Node>, val link: String)

    private fun get(url: String): String? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "GRoute")
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        text
    } catch (e: Exception) {
        null
    }

    data class IpInfo(
        val ip: String,
        val host: String,
        val asn: String,
        val org: String,
        val country: String,
        val countryCode: String,
        val region: String,
        val city: String,
        val timezone: String
    )

    suspend fun ipInfo(host: String): IpInfo? = withContext(Dispatchers.IO) {
        val raw = host.trim().removePrefix("http://").removePrefix("https://")
            .substringBefore('/').substringBefore(':')
        val resolved = runCatching {
            java.net.InetAddress.getByName(raw).hostAddress ?: raw
        }.getOrDefault(raw)
        val encoded = URLEncoder.encode(resolved, "UTF-8")
        val body = get("https://ipwho.is/$encoded") ?: return@withContext null
        try {
            val o = JSONObject(body)
            if (!o.optBoolean("success", true)) return@withContext null
            val conn = o.optJSONObject("connection") ?: JSONObject()
            val tz = o.optJSONObject("timezone") ?: JSONObject()
            IpInfo(
                ip = o.optString("ip", ""),
                host = raw,
                asn = conn.optInt("asn", 0).let { if (it == 0) "" else "AS$it" },
                org = conn.optString("isp", conn.optString("org", "")),
                country = o.optString("country", ""),
                countryCode = o.optString("country_code", ""),
                region = o.optString("region", ""),
                city = o.optString("city", ""),
                timezone = tz.optString("id", "") + ", " + tz.optString("utc", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun countryName(cc: String): String {
        if (cc.length != 2) return cc.uppercase()
        val name = java.util.Locale("", cc.uppercase()).getDisplayCountry(java.util.Locale.ENGLISH)
        return if (name.isBlank() || name.equals(cc, true)) cc.uppercase() else name
    }

    suspend fun start(host: String, kind: String = "ping", maxNodes: Int = 40): Session? =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(host.trim(), "UTF-8")
            val body = get("$BASE/check-$kind?host=$encoded&max_nodes=$maxNodes")
                ?: return@withContext null
            try {
                val root = JSONObject(body)
                val id = root.optString("request_id", "")
                if (id.isEmpty()) return@withContext null
                val nodesObj = root.optJSONObject("nodes") ?: JSONObject()
                val list = ArrayList<Node>()
                nodesObj.keys().forEach { key ->
                    val arr = nodesObj.optJSONArray(key)
                    val cc = (arr?.optString(1, "") ?: "").trim()
                    val city = (arr?.optString(2, "") ?: "").trim()
                    list.add(Node(key, countryName(cc), cc, city))
                }
                val seen = HashSet<String>()
                val unique = list.filter { seen.add(it.countryCode.ifEmpty { it.country }) }
                    .sortedBy { it.country }
                Session(id, unique, root.optString("permanent_link", ""))
            } catch (e: Exception) {
                null
            }
        }

    suspend fun poll(requestId: String): Map<String, NodeResult>? =
        withContext(Dispatchers.IO) {
            val body = get("$BASE/check-result/$requestId") ?: return@withContext null
            try {
                val root = JSONObject(body)
                val out = HashMap<String, NodeResult>()
                root.keys().forEach { key ->
                    out[key] = parseNode(root.opt(key))
                }
                out
            } catch (e: Exception) {
                null
            }
        }

    private fun parseNode(value: Any?): NodeResult {
        if (value == null || value == JSONObject.NULL) return NodeResult.Pending
        val outer = value as? JSONArray ?: return NodeResult.Failed
        val inner = outer.opt(0)
        if (inner == null || inner == JSONObject.NULL) return NodeResult.Failed

        if (inner is JSONObject) {
            val time = inner.optDouble("time", -1.0)
            val addr = inner.optString("address", "")
            return if (time >= 0.0) NodeResult.Ok(time * 1000.0, 0, addr) else NodeResult.Failed
        }

        val arr = inner as? JSONArray ?: return NodeResult.Failed
        val first = arr.opt(0)

        if (first is JSONArray) {
            var total = 0.0
            var ok = 0
            var fail = 0
            var ip = ""
            for (i in 0 until arr.length()) {
                val t = arr.optJSONArray(i) ?: continue
                if (t.optString(0, "").equals("OK", true)) {
                    total += t.optDouble(1, 0.0)
                    if (ip.isEmpty()) ip = t.optString(2, "")
                    ok++
                } else fail++
            }
            if (ok == 0) return NodeResult.Failed
            val loss = if (ok + fail == 0) 0 else fail * 100 / (ok + fail)
            return NodeResult.Ok(total / ok * 1000.0, loss, ip)
        }

        val success = arr.optInt(0, 0)
        if (success != 1) return NodeResult.Failed
        val time = arr.optDouble(1, -1.0)
        if (time < 0.0) return NodeResult.Failed
        val ip = arr.optString(4, "")
        return NodeResult.Ok(time * 1000.0, 0, ip)
    }

    suspend fun run(
        host: String,
        kind: String = "ping",
        maxNodes: Int = 40,
        onNodes: (List<Node>) -> Unit,
        onResults: (Map<String, NodeResult>) -> Unit
    ): Boolean {
        val session = start(host, kind, maxNodes) ?: return false
        onNodes(session.nodes)
        repeat(12) {
            delay(1500)
            val res = poll(session.requestId) ?: return@repeat
            onResults(res)
            if (res.isNotEmpty() && res.values.none { it is NodeResult.Pending }) return true
        }
        return true
    }
}