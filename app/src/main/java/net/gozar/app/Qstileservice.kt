package net.gozar.app

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class QsTileService : TileService() {

    private var scope: CoroutineScope? = null
    private var collectJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        VpnBridge.register(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        val s = CoroutineScope(Dispatchers.Main)
        scope = s
        collectJob = s.launch {
            VpnState.state.collect { render() }
        }
        s.launch {
            ConfigStore.get(applicationContext).awaitReady()
            render()
        }
        render()
    }

    override fun onStopListening() {
        collectJob?.cancel()
        collectJob = null
        scope = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        if (isLocked) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        if (activeNow()) {
            stopTunnel()
            render()
            return
        }
        val s = scope ?: CoroutineScope(Dispatchers.Main).also { scope = it }
        s.launch {
            withTimeoutOrNull(3000) { ConfigStore.get(applicationContext).awaitReady() }
            startTunnel()
            render()
        }
    }

    private fun startTunnel() {
        val store = ConfigStore.get(applicationContext)
        val selectedId = store.selectedId.value
        val config = store.configs.value.firstOrNull { it.id == selectedId }
        if (config == null) { openApp(); return }
        if (VpnService.prepare(this) != null) { openApp(); return }
        val json = ConfigBuilder.build(
            config, store.fragment.value, store.splitRouting.value,
            store.sniffing.value, store.sniffTypes.value
        )
        VpnState.setConnecting(config.id)
        val intent = Intent(this, GozarVpnService::class.java)
            .putExtra(GozarVpnService.EXTRA_CONFIG, json)
            .putExtra(GozarVpnService.EXTRA_NAME, config.name)
            .putExtra(GozarVpnService.EXTRA_STOP_LABEL, Strings.get(store.lang.value, "disconnect"))
        runCatching { ContextCompat.startForegroundService(this, intent) }
            .onFailure { VpnState.setDisconnected(); openApp() }
    }

    private fun stopTunnel() {
        runCatching {
            startService(Intent(this, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_STOP))
        }
        VpnState.setDisconnected()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (Build.VERSION.SDK_INT >= 34) {
            val pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun activeNow(): Boolean {
        val s = VpnState.state.value
        if (s == Connection.CONNECTED || s == Connection.CONNECTING) return true
        return isServiceRunning()
    }

    private fun isServiceRunning(): Boolean = runCatching {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        am.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == GozarVpnService::class.java.name
        }
    }.getOrDefault(false)

    private fun render() {
        val tile = qsTile ?: return
        val active = activeNow()
        val store = runCatching { ConfigStore.get(applicationContext) }.getOrNull()
        val lang = store?.lang?.value ?: Lang.EN
        val selectedName = store?.let { st ->
            val id = st.selectedId.value
            st.configs.value.firstOrNull { it.id == id }?.name
        }
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = Strings.get(lang, "app_title")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when {
                active -> Strings.get(lang, "status_connected")
                selectedName != null -> selectedName
                else -> Strings.get(lang, "tap_choose")
            }
        }
        runCatching { tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_tile) }
        tile.updateTile()
    }
}