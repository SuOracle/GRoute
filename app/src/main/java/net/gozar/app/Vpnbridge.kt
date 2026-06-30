package net.gozar.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VpnCounters(
    val totalUp: Long = 0L,
    val totalDown: Long = 0L,
    val upSpeed: Long = 0L,
    val downSpeed: Long = 0L
)

object VpnBridge {
    private const val ACTION = "net.gozar.app.VPN_UPDATE"
    private const val EX_STATE = "state"
    private const val EX_ERROR = "error"
    private const val EX_TOTAL_UP = "tup"
    private const val EX_TOTAL_DOWN = "tdown"
    private const val EX_DELTA_UP = "dup"
    private const val EX_DELTA_DOWN = "ddown"

    private const val S_CONNECTED = "connected"
    private const val S_ERROR = "error"
    private const val S_DISCONNECTED = "disconnected"
    private const val S_COUNTERS = "counters"

    private val _counters = MutableStateFlow(VpnCounters())
    val counters: StateFlow<VpnCounters> = _counters.asStateFlow()

    @Volatile private var registered = false

    fun register(context: Context) {
        if (registered) return
        registered = true
        val app = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.getStringExtra(EX_STATE)) {
                    S_CONNECTED -> VpnState.setConnected()
                    S_ERROR -> {
                        VpnState.setError(intent.getStringExtra(EX_ERROR) ?: "Connection failed")
                        _counters.value = VpnCounters()
                    }
                    S_DISCONNECTED -> {
                        VpnState.setDisconnected()
                        _counters.value = VpnCounters()
                        UsageStore.flush()
                    }
                    S_COUNTERS -> {
                        val tup = intent.getLongExtra(EX_TOTAL_UP, 0L)
                        val tdown = intent.getLongExtra(EX_TOTAL_DOWN, 0L)
                        val dup = intent.getLongExtra(EX_DELTA_UP, 0L)
                        val ddown = intent.getLongExtra(EX_DELTA_DOWN, 0L)
                        _counters.value = VpnCounters(tup, tdown, dup, ddown)
                        UsageStore.add(dup, ddown)
                    }
                }
            }
        }
        ContextCompat.registerReceiver(app, receiver, IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun send(ctx: Context, state: String, error: String?, tup: Long, tdown: Long, dup: Long, ddown: Long) {
        val i = Intent(ACTION).setPackage(ctx.packageName)
            .putExtra(EX_STATE, state)
            .putExtra(EX_ERROR, error)
            .putExtra(EX_TOTAL_UP, tup)
            .putExtra(EX_TOTAL_DOWN, tdown)
            .putExtra(EX_DELTA_UP, dup)
            .putExtra(EX_DELTA_DOWN, ddown)
        ctx.sendBroadcast(i)
    }

    fun sendConnected(ctx: Context) = send(ctx, S_CONNECTED, null, 0L, 0L, 0L, 0L)
    fun sendError(ctx: Context, message: String) = send(ctx, S_ERROR, message, 0L, 0L, 0L, 0L)
    fun sendDisconnected(ctx: Context) = send(ctx, S_DISCONNECTED, null, 0L, 0L, 0L, 0L)
    fun sendCounters(ctx: Context, totalUp: Long, totalDown: Long, upSpeed: Long, downSpeed: Long) =
        send(ctx, S_COUNTERS, null, totalUp, totalDown, upSpeed, downSpeed)
}