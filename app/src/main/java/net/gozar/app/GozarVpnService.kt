package net.gozar.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import gozarcore.Gozarcore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class GozarVpnService : VpnService() {

    private var tunFd: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var pollJob: Job? = null
    private var configName: String = "VPN"
    private var stopLabel: String = "Disconnect"
    @Volatile private var tearingDown = false

    override fun onCreate() {
        super.onCreate()
        Gozarcore.setLogger(object : gozarcore.Logger {
            override fun log(line: String?) {
                Log.i("XrayCore", line ?: "")
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                die(null)
                return START_NOT_STICKY
            }
            ACTION_WARM -> {
                if (tunFd != null) {
                    VpnBridge.sendConnected(applicationContext)
                    return START_STICKY
                }
                return START_NOT_STICKY
            }
            else -> {
                val configJson = intent?.getStringExtra(EXTRA_CONFIG)
                configName = intent?.getStringExtra(EXTRA_NAME) ?: "VPN"
                stopLabel = intent?.getStringExtra(EXTRA_STOP_LABEL) ?: "Disconnect"
                if (configJson.isNullOrEmpty()) {
                    die("No config provided")
                    return START_NOT_STICKY
                }
                startTunnel(configJson)
            }
        }
        return START_STICKY
    }

    private fun startTunnel(configJson: String) {
        if (tunFd != null) return
        tearingDown = false
        startForeground(NOTIF_ID, buildNotification())

        scope.launch {
            val builder = Builder()
                .setSession("GozarNet")
                .setMtu(1500)
                .addAddress("10.10.0.2", 32)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)

            applyPerApp(builder)

            val pfd = builder.establish()
            if (pfd == null) {
                die("VPN permission not granted")
                return@launch
            }
            tunFd = pfd

            try {
                setupGeoAssets()
                runCatching { Gozarcore.stop() }
                Gozarcore.start(configJson, pfd.detachFd().toLong())
                Log.i(TAG, "Xray core started, tunnel up")
                VpnBridge.sendConnected(applicationContext)
                startPolling()
            } catch (e: Exception) {
                Log.e(TAG, "Xray core failed to start", e)
                die(e.message ?: "Engine failed to start")
            }
        }
    }

    private fun setupGeoAssets() {
        val dir = filesDir
        runCatching {
            listOf("geoip.dat", "geosite.dat").forEach { name ->
                val out = File(dir, name)
                if (!out.exists() || out.length() == 0L) {
                    assets.open(name).use { input ->
                        out.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }.onFailure { Log.w(TAG, "geo assets not bundled: ${it.message}") }
        Gozarcore.setAssetPath(dir.absolutePath)
    }

    private fun applyPerApp(builder: Builder) {
        val store = ConfigStore.get(applicationContext)
        val mode = store.perAppMode.value
        val list = store.perAppList.value

        when (mode) {
            PerAppMode.ALLOWLIST -> {
                if (list.isEmpty()) {
                    runCatching { builder.addDisallowedApplication(packageName) }
                } else {
                    list.forEach { pkg ->
                        runCatching { builder.addAllowedApplication(pkg) }
                    }
                }
            }
            PerAppMode.BLOCKLIST -> {
                (list + packageName).forEach { pkg ->
                    runCatching { builder.addDisallowedApplication(pkg) }
                }
            }
            PerAppMode.OFF -> {
                runCatching { builder.addDisallowedApplication(packageName) }
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            var lastUp = 0L
            var lastDown = 0L
            while (isActive && !tearingDown) {
                val up = Gozarcore.queryUplink()
                val down = Gozarcore.queryDownlink()
                val upSpeed = (up - lastUp).coerceAtLeast(0L)
                val downSpeed = (down - lastDown).coerceAtLeast(0L)
                lastUp = up; lastDown = down

                VpnBridge.sendCounters(applicationContext, up, down, upSpeed, downSpeed)

                if (!tearingDown) {
                    getSystemService(NotificationManager::class.java)
                        ?.notify(NOTIF_ID, buildNotification(downSpeed, upSpeed))
                }

                delay(1000)
            }
        }
    }

    private fun die(error: String?) {
        if (tearingDown) return
        tearingDown = true
        pollJob?.cancel()
        pollJob = null
        runCatching { Gozarcore.stop() }
        if (error != null) VpnBridge.sendError(applicationContext, error)
        else VpnBridge.sendDisconnected(applicationContext)
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        runCatching { getSystemService(NotificationManager::class.java)?.cancel(NOTIF_ID) }
        stopSelf()
        scope.launch {
            delay(60)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    override fun onDestroy() {
        runCatching { getSystemService(NotificationManager::class.java)?.cancel(NOTIF_ID) }
        super.onDestroy()
    }

    private fun buildNotification(downSpeed: Long = 0, upSpeed: Long = 0): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "GozarNet", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 1, Intent(this, GozarVpnService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val speedLine = "↓ ${fmt(downSpeed)}/s   ↑ ${fmt(upSpeed)}/s"
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(configName)
            .setContentText(speedLine)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pi)
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel, stopLabel, stopPi
                ).build()
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun fmt(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        return "%.2f GB".format(mb / 1024.0)
    }

    companion object {
        private const val TAG = "GozarVpnService"
        private const val CHANNEL_ID = "gozarnet_vpn"
        private const val NOTIF_ID = 1
        const val ACTION_STOP = "net.gozar.app.STOP"
        const val ACTION_WARM = "net.gozar.app.WARM"
        const val EXTRA_CONFIG = "net.gozar.app.CONFIG"
        const val EXTRA_NAME = "net.gozar.app.NAME"
        const val EXTRA_STOP_LABEL = "net.gozar.app.STOP_LABEL"
    }
}