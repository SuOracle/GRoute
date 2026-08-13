package net.gozar.app

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object TorLog {
    @Volatile
    var sink: ((String) -> Unit)? = null

    fun emit(line: String) {
        runCatching { sink?.invoke("[Tor] " + line) }
    }
}

object TorController {

    const val SOCKS_PORT = 9150
    const val CONTROL_PORT = 9151
    const val BRIDGE_PORT = 10627

    private const val TAG = "Tor"
    private const val READY_TIMEOUT_MS = 120_000L

    @Volatile
    private var process: Process? = null

    @Volatile
    private var stopping = false

    @Volatile
    private var bootstrapped = false

    @Volatile
    var bootstrapPercent = 0
        private set

    val Countries = listOf(
        "" to "Automatic",
        "us" to "United States",
        "de" to "Germany",
        "nl" to "Netherlands",
        "fr" to "France",
        "gb" to "United Kingdom",
        "se" to "Sweden",
        "ch" to "Switzerland",
        "fi" to "Finland",
        "ro" to "Romania",
        "at" to "Austria",
        "ca" to "Canada",
        "jp" to "Japan",
        "sg" to "Singapore",
        "au" to "Australia",
        "in" to "India",
        "br" to "Brazil",
        "id" to "Indonesia",
        "th" to "Thailand",
        "ua" to "Ukraine",
        "es" to "Spain",
        "it" to "Italy",
        "pl" to "Poland",
        "cz" to "Czechia",
        "no" to "Norway",
        "dk" to "Denmark",
        "be" to "Belgium",
        "ie" to "Ireland",
        "tr" to "Turkey",
        "za" to "South Africa",
        "ru" to "Russia",
        "kr" to "South Korea",
        "az" to "Azerbaijan",
        "mx" to "Mexico",
        "cn" to "China",
        "eg" to "Egypt",
        "il" to "Israel"
    )

    fun binary(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, "libtor.so")

    fun available(context: Context): Boolean = binary(context).exists()

    fun dataDir(context: Context): File = File(context.filesDir, "tor").apply { mkdirs() }

    fun isRunning(): Boolean = process?.isAlive == true

    private fun geoFiles(context: Context): Pair<File, File> {
        val dir = dataDir(context)
        val geo = File(dir, "geoip")
        val geo6 = File(dir, "geoip6")
        listOf("geoip" to geo, "geoip6" to geo6).forEach { (name, out) ->
            if (!out.exists() || out.length() == 0L) {
                runCatching {
                    context.assets.open(name).use { input ->
                        out.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
        return geo to geo6
    }

    private fun writeTorrc(context: Context, exitCountry: String, throughVpn: Boolean): File {
        val dir = dataDir(context)
        val (geo, geo6) = geoFiles(context)
        val sb = StringBuilder()
        sb.appendLine("SocksPort 127.0.0.1:" + SOCKS_PORT)
        sb.appendLine("ControlPort 127.0.0.1:" + CONTROL_PORT)
        sb.appendLine("DataDirectory " + dir.absolutePath)
        sb.appendLine("CacheDirectory " + File(dir, "cache").absolutePath)
        sb.appendLine("AvoidDiskWrites 1")
        sb.appendLine("Log notice stdout")
        sb.appendLine("ClientOnly 1")
        val cc = exitCountry.trim().lowercase()
        if (cc.length == 2 && geo.exists() && geo6.exists()) {
            sb.appendLine("GeoIPFile " + geo.absolutePath)
            sb.appendLine("GeoIPv6File " + geo6.absolutePath)
            sb.appendLine("ExitNodes {" + cc + "}")
            sb.appendLine("StrictNodes 0")
        } else if (cc.length == 2) {
            Log.w(TAG, "geoip assets missing, exit country ignored")
        }
        if (throughVpn) {
            sb.appendLine("Socks5Proxy 127.0.0.1:" + BRIDGE_PORT)
        }
        val torrc = File(dir, "torrc")
        torrc.writeText(sb.toString())
        return torrc
    }

    fun start(context: Context, exitCountry: String, throughVpn: Boolean): Boolean {
        stop()
        stopping = false
        bootstrapped = false
        bootstrapPercent = 0

        val bin = binary(context)
        if (!bin.exists()) {
            Log.e(TAG, "binary missing at " + bin.absolutePath)
            return false
        }

        val dir = dataDir(context)
        val torrc = writeTorrc(context, exitCountry, throughVpn)

        val p = try {
            ProcessBuilder(listOf(bin.absolutePath, "-f", torrc.absolutePath))
                .directory(dir)
                .redirectErrorStream(true)
                .apply { environment()["HOME"] = dir.absolutePath }
                .start()
        } catch (e: Exception) {
            Log.e(TAG, "spawn failed", e)
            return false
        }
        process = p

        thread(isDaemon = true, name = "tor-log") {
            runCatching {
                BufferedReader(InputStreamReader(p.inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        if (stopping) return@forEach
                        Log.i(TAG, line)
                        TorLog.emit(line)
                        val idx = line.indexOf("Bootstrapped ")
                        if (idx >= 0) {
                            val pct = line.substring(idx + 13)
                                .takeWhile { c -> c.isDigit() }
                                .toIntOrNull()
                            if (pct != null) {
                                bootstrapPercent = pct
                                if (pct >= 100) bootstrapped = true
                            }
                        }
                    }
                }
            }
        }

        return waitForPort()
    }

    private fun waitForPort(): Boolean {
        val deadline = System.currentTimeMillis() + READY_TIMEOUT_MS
        var portOpen = false
        while (System.currentTimeMillis() < deadline) {
            if (stopping) return false
            val p = process
            if (p == null || !p.isAlive) {
                Log.e(TAG, "process exited before bootstrap completed")
                return false
            }
            if (!portOpen) {
                portOpen = runCatching {
                    Socket().use {
                        it.connect(InetSocketAddress("127.0.0.1", SOCKS_PORT), 400)
                        true
                    }
                }.getOrDefault(false)
            }
            if (portOpen && bootstrapped) {
                Log.i(TAG, "bootstrapped, socks ready on 127.0.0.1:" + SOCKS_PORT)
                return true
            }
            Thread.sleep(500)
        }
        Log.e(TAG, "timed out at bootstrap " + bootstrapPercent + "%")
        return false
    }

    fun stop() {
        stopping = true
        val p = process ?: return
        process = null
        runCatching {
            p.destroy()
            if (!p.waitFor(3000, TimeUnit.MILLISECONDS)) p.destroyForcibly()
        }
    }
}