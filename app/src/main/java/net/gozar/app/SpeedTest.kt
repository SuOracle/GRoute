package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

object SpeedTest {

    private const val PROXY_HOST = "127.0.0.1"
    private const val PROXY_PORT = 10626
    private const val DELAY_URL = "https://www.gstatic.com/generate_204"
    private const val DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=26214400"

    private fun proxy() = Proxy(Proxy.Type.SOCKS, InetSocketAddress(PROXY_HOST, PROXY_PORT))

    private fun measureOnce(timeoutMs: Int): Int? = try {
        val conn = (URL(DELAY_URL).openConnection(proxy()) as HttpURLConnection).apply {
            connectTimeout = timeoutMs; readTimeout = timeoutMs; requestMethod = "GET"
            setRequestProperty("User-Agent", "GozarNet")
        }
        val start = System.currentTimeMillis()
        conn.connect()
        conn.responseCode
        val ms = (System.currentTimeMillis() - start).toInt()
        conn.disconnect()
        ms
    } catch (e: Exception) { null }

    suspend fun delay(): Int? = withContext(Dispatchers.IO) {
        measureOnce(6000) ?: return@withContext null
        val first = measureOnce(5000)
        val second = measureOnce(5000)
        when {
            first != null && second != null -> minOf(first, second)
            else -> first ?: second
        }
    }

    suspend fun download(): Double? = withContext(Dispatchers.IO) {
        try {
            var peakBytesPerSec = 0.0
            var sliceBytes = 0L
            var sliceStart = 0L
            var overallStart = 0L
            val sliceMs = 500L
            val maxDuration = 12000L

            outer@ while (true) {
                val conn = (URL(DOWNLOAD_URL).openConnection(proxy()) as HttpURLConnection).apply {
                    connectTimeout = 10000; readTimeout = 20000; requestMethod = "GET"
                    setRequestProperty("User-Agent", "GozarNet")
                }
                conn.connect()
                conn.inputStream.use { input ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        val now = System.currentTimeMillis()
                        if (overallStart == 0L) { overallStart = now; sliceStart = now }
                        sliceBytes += n

                        val elapsed = now - sliceStart
                        if (elapsed >= sliceMs) {
                            val rate = sliceBytes * 1000.0 / elapsed
                            if (rate > peakBytesPerSec) peakBytesPerSec = rate
                            sliceBytes = 0
                            sliceStart = now
                        }
                        if (now - overallStart > maxDuration) { conn.disconnect(); break@outer }
                    }
                }
                conn.disconnect()
                if (overallStart != 0L && System.currentTimeMillis() - overallStart > maxDuration) break
            }

            if (peakBytesPerSec <= 0) null
            else peakBytesPerSec * 8.0 / 1_000_000.0
        } catch (e: Exception) { null }
    }
}