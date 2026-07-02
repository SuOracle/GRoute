package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    private const val API = "https://api.github.com/repos/SuOracle/GRoute/releases/latest"
    private const val RELEASES = "https://github.com/SuOracle/GRoute/releases/latest"

    sealed interface Result {
        data class Available(val version: String, val url: String) : Result
        data object UpToDate : Result
        data object Failed : Result
    }

    suspend fun check(currentVersion: String): Result = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(API).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "GRoute")
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            val body = try {
                conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            } finally {
                conn.disconnect()
            }
            val o = JSONObject(body)
            val tag = o.optString("tag_name").removePrefix("v").removePrefix("V").trim()
            val url = o.optString("html_url").ifEmpty { RELEASES }
            when {
                tag.isEmpty() -> Result.Failed
                isNewer(tag, currentVersion) -> Result.Available(tag, url)
                else -> Result.UpToDate
            }
        } catch (e: Exception) {
            Result.Failed
        }
    }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = parts(remote)
        val l = parts(local)
        val n = maxOf(r.size, l.size)
        for (i in 0 until n) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv != lv) return rv > lv
        }
        return false
    }

    private fun parts(v: String): List<Int> =
        v.split('.').map { seg -> seg.filter { it.isDigit() }.toIntOrNull() ?: 0 }
}