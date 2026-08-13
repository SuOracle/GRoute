package net.gozar.app

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class AetherSpec(
    val mode: String = "masque",
    val scan: String = "balanced",
    val noise: String = "",
    val http2: Boolean = false,
    val ipv6: Boolean = false
) {
    fun toJson(): String = JSONObject()
        .put("mode", mode)
        .put("scan", scan)
        .put("noise", noise)
        .put("http2", http2)
        .put("ipv6", ipv6)
        .toString()

    companion object {
        fun from(config: ProxyConfig): AetherSpec? =
            if (config.protocol != "aether") null
            else AetherSpec(
                mode = config.aetherMode.ifBlank { "masque" },
                scan = config.aetherScan.ifBlank { "balanced" },
                noise = config.aetherNoise,
                http2 = config.aetherHttp2,
                ipv6 = config.aetherIpv6
            )

        fun parse(raw: String?): AetherSpec? {
            if (raw.isNullOrBlank()) return null
            return runCatching {
                val o = JSONObject(raw)
                AetherSpec(
                    mode = o.optString("mode", "masque"),
                    scan = o.optString("scan", "balanced"),
                    noise = o.optString("noise", ""),
                    http2 = o.optBoolean("http2", false),
                    ipv6 = o.optBoolean("ipv6", false)
                )
            }.getOrNull()
        }
    }
}

object AetherController {

    const val SOCKS_PORT = 1819

    private const val TAG = "Aether"
    private const val READY_TIMEOUT_MS = 180_000L

    @Volatile
    private var process: Process? = null

    @Volatile
    private var stopping = false

    fun binary(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, "libaether.so")

    fun available(context: Context): Boolean = binary(context).exists()

    fun workDir(context: Context): File = File(context.filesDir, "aether").apply { mkdirs() }

    fun identity(context: Context): File = File(workDir(context), "aether-masque.toml")

    fun isRunning(): Boolean = process?.isAlive == true

    fun spec(config: ProxyConfig): String = AetherSpec.from(config)?.toJson() ?: ""

    private fun args(spec: AetherSpec): List<String> {
        val out = mutableListOf("--bind", "127.0.0.1:" + AetherController.SOCKS_PORT)
        out += when (spec.mode) {
            "wg" -> "--wg"
            "gool" -> "--gool"
            else -> "--masque"
        }
        if (spec.http2) out += "--h2"
        out += listOf("--scan", spec.scan.ifBlank { "balanced" })
        if (spec.noise.isNotBlank()) out += listOf("--noize", spec.noise)
        out += "--quick-reconnect"
        out += if (spec.ipv6) "-6" else "-4"
        return out
    }

    private fun env(spec: AetherSpec, context: Context): Map<String, String> {
        val dir = workDir(context)
        val out = mutableMapOf(
            "AETHER_SOCKS" to ("127.0.0.1:" + AetherController.SOCKS_PORT),
            "AETHER_PROTOCOL" to spec.mode.ifBlank { "masque" },
            "AETHER_SCAN" to spec.scan.ifBlank { "balanced" },
            "AETHER_IP" to if (spec.ipv6) "both" else "4",
            "AETHER_QUICK_RECONNECT" to "1",
            "AETHER_CONFIG" to File(dir, "aether.toml").absolutePath,
            "AETHER_MASQUE_CONFIG" to identity(context).absolutePath,
            "AETHER_WG_CONFIG" to File(dir, "aether-wg.toml").absolutePath,
            "HOME" to dir.absolutePath,
            "TMPDIR" to context.cacheDir.absolutePath
        )
        if (spec.http2) out["AETHER_MASQUE_HTTP2"] = "1"
        if (spec.noise.isNotBlank()) out["AETHER_NOIZE"] = spec.noise
        return out
    }

    fun start(context: Context, spec: AetherSpec): Boolean {
        stop()
        stopping = false

        val bin = binary(context)
        if (!bin.exists()) {
            Log.e(TAG, "binary missing at " + bin.absolutePath)
            return false
        }

        val dir = workDir(context)
        val cmd = mutableListOf(bin.absolutePath).apply { addAll(args(spec)) }

        val p = try {
            ProcessBuilder(cmd)
                .directory(dir)
                .redirectErrorStream(true)
                .apply { environment().putAll(env(spec, context)) }
                .start()
        } catch (e: Exception) {
            Log.e(TAG, "spawn failed", e)
            return false
        }
        process = p

        thread(isDaemon = true, name = "aether-log") {
            runCatching {
                BufferedReader(InputStreamReader(p.inputStream)).useLines { lines ->
                    lines.forEach { if (!stopping) Log.i(TAG, it) }
                }
            }
        }

        return waitForPort()
    }

    private fun waitForPort(): Boolean {
        val deadline = System.currentTimeMillis() + READY_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (stopping) return false
            val p = process
            if (p == null || !p.isAlive) {
                Log.e(TAG, "process exited before the proxy came up")
                return false
            }
            val ok = runCatching {
                Socket().use {
                    it.connect(InetSocketAddress("127.0.0.1", AetherController.SOCKS_PORT), 400)
                    true
                }
            }.getOrDefault(false)
            if (ok) {
                Log.i(TAG, "socks ready on 127.0.0.1:" + AetherController.SOCKS_PORT)
                return true
            }
            Thread.sleep(400)
        }
        Log.e(TAG, "timed out waiting for the proxy")
        return false
    }

    fun stop() {
        stopping = true
        val p = process ?: return
        process = null
        runCatching {
            p.destroy()
            if (!p.waitFor(2000, TimeUnit.MILLISECONDS)) p.destroyForcibly()
        }
    }
}