package net.gozar.app

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import gozarcore.Gozarcore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AutoSelector(
    private val appContext: Context,
    private val store: ConfigStore,
    private val onSwitch: ((ProxyConfig) -> Unit)? = null
) {

    private var loopJob: Job? = null

    private val _results = MutableStateFlow<Map<String, PingResult>>(emptyMap())
    val results: StateFlow<Map<String, PingResult>> = _results.asStateFlow()

    fun start(scope: CoroutineScope) {
        if (loopJob?.isActive == true) {
            android.util.Log.d(TAG, "start() ignored, already running")
            return
        }
        android.util.Log.d(TAG, "start()")
        loopJob = scope.launch {
            while (isActive) {
                runCatching { runOnce() }
                delay(INTERVAL_MS)
            }
        }
    }

    fun stop() {
        android.util.Log.d(TAG, "stop()")
        loopJob?.cancel()
        loopJob = null
    }

    private suspend fun runOnce() = coroutineScope {
        store.awaitReady()
        val configs = store.configs.value
        android.util.Log.d(TAG, "runOnce: ${configs.size} configs")
        if (configs.isEmpty()) return@coroutineScope

        val marking = _results.value.toMutableMap()
        configs.forEach { marking[it.id] = PingResult.Testing }
        _results.value = marking.toMap()

        val sem = Semaphore(MAX_CONCURRENCY)
        configs.map { cfg ->
            launch {
                sem.withPermit {
                    val r = if (cfg.protocol.trim().lowercase() == "ikev2") {
                        Pinger.pingIke(cfg.address)
                    } else {
                        val ms = withContext(Dispatchers.IO) {
                            runCatching { Gozarcore.measureDelay(ConfigBuilder.buildForTest(cfg)) }
                                .getOrDefault(-1L)
                        }
                        if (ms >= 0) PingResult.Ok(ms.toInt()) else PingResult.Failed
                    }
                    _results.value = _results.value.toMutableMap().apply { put(cfg.id, r) }
                }
            }
        }.joinAll()

        val snapshot = _results.value
        val best = configs
            .mapNotNull { c -> (snapshot[c.id] as? PingResult.Ok)?.let { c to it.ms } }
            .minByOrNull { it.second }
        if (best == null) {
            android.util.Log.w(TAG, "no config responded, nothing to switch to")
            return@coroutineScope
        }
        android.util.Log.d(TAG, "best=${best.first.name} ${best.second}ms")

        val selectedId = store.selectedId.value
        if (selectedId == null) {
            store.setSelectedId(best.first.id)
            return@coroutineScope
        }
        if (best.first.id == selectedId) {
            android.util.Log.d(TAG, "best is already selected")
            return@coroutineScope
        }

        val currentPing = (snapshot[selectedId] as? PingResult.Ok)?.ms
        val shouldSwitch = currentPing == null || currentPing - best.second >= SWITCH_MARGIN_MS
        if (!shouldSwitch) {
            android.util.Log.d(TAG, "margin too small: current=${currentPing}ms best=${best.second}ms")
            return@coroutineScope
        }

        android.util.Log.d(TAG, "switching to ${best.first.name}")
        store.setSelectedId(best.first.id)
        val handler = onSwitch
        if (handler != null) {
            withContext(Dispatchers.Main) { handler(best.first) }
        } else {
            reconnectIfConnected(best.first)
        }
    }

    private suspend fun reconnectIfConnected(config: ProxyConfig) {
        if (VpnState.state.value != Connection.CONNECTED) return
        if (VpnService.prepare(appContext) != null) return

        runCatching {
            appContext.startService(
                Intent(appContext, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_STOP)
            )
        }
        withTimeoutOrNull(6000) {
            VpnState.state.first { it == Connection.DISCONNECTED || it == Connection.ERROR }
        }
        delay(400)

        val json = ConfigBuilder.build(
            config, store.fragment.value, store.splitRouting.value,
            store.sniffing.value, store.sniffTypes.value,
            adBlock = store.adBlock.value,
            fakeDns = store.fakeDns.value,
            encryptedDns = store.encryptedDns.value,
            onionRouting = store.onionRouting.value
        )
        VpnState.setConnecting(config.id)
        val intent = Intent(appContext, GozarVpnService::class.java)
            .putExtra(GozarVpnService.EXTRA_CONFIG, json)
            .putExtra(GozarVpnService.EXTRA_AETHER, AetherSpec.from(config)?.toJson())
            .putExtra(
                GozarVpnService.EXTRA_TOR,
                if (config.protocol == "tor")
                    config.torCountry + "|" + (if (config.torThroughVpn) "1" else "0") else null
            )
            .putExtra(GozarVpnService.EXTRA_NAME, config.name)
            .putExtra(GozarVpnService.EXTRA_STOP_LABEL, Strings.get(store.lang.value, "disconnect"))
        runCatching { ContextCompat.startForegroundService(appContext, intent) }
            .onFailure { VpnState.setDisconnected() }
    }

    private companion object {
        const val TAG = "GRouteAuto"
        const val INTERVAL_MS = 60_000L
        const val MAX_CONCURRENCY = 4
        const val SWITCH_MARGIN_MS = 40
    }
}