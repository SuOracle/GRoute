package net.gozar.app

import gozarcore.Gozarcore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
object StabilityTest {

    enum class Phase { PING, DOWNLOAD, UPLOAD, DONE }

    data class Result(
        val downloadMbps: Double,
        val uploadMbps: Double,
        val idleLatency: Double,
        val jitter: Double,
        val downloadLatency: Double,
        val uploadLatency: Double
    )

    private fun phaseOf(s: String): Phase = when (s) {
        "download" -> Phase.DOWNLOAD
        "upload" -> Phase.UPLOAD
        "ping" -> Phase.PING
        else -> Phase.DONE
    }
    suspend fun run(testConfigJson: String, onProgress: (Phase, Double) -> Unit): Result? =
        coroutineScope {
            val poller = launch(Dispatchers.IO) {
                try {
                    while (isActive) {
                        val ph = phaseOf(Gozarcore.speedTestPhase())
                        if (ph != Phase.DONE) onProgress(ph, Gozarcore.speedTestLive())
                        delay(100)
                    }
                } catch (_: Exception) {}
            }
            val json = withContext(Dispatchers.IO) {
                try { Gozarcore.runSpeedTest(testConfigJson) } catch (e: Exception) { "" }
            }
            poller.cancel()
            parse(json)
        }

    private fun parse(json: String): Result? {
        if (json.isBlank()) return null
        return try {
            val o = JSONObject(json)
            if (o.has("error")) return null
            Result(
                downloadMbps = o.optDouble("download", 0.0),
                uploadMbps = o.optDouble("upload", 0.0),
                idleLatency = o.optDouble("idle", 0.0),
                jitter = o.optDouble("jitter", 0.0),
                downloadLatency = o.optDouble("dlLatency", 0.0),
                uploadLatency = o.optDouble("ulLatency", 0.0)
            )
        } catch (e: Exception) { null }
    }
    fun toJson(r: Result): String = JSONObject()
        .put("download", r.downloadMbps).put("upload", r.uploadMbps)
        .put("idle", r.idleLatency).put("jitter", r.jitter)
        .put("dlLatency", r.downloadLatency).put("ulLatency", r.uploadLatency)
        .toString()

    fun fromJson(json: String): Result? = parse(json)
}