package net.gozar.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.data.VpnProfileSource
import org.strongswan.android.data.VpnType
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import java.util.UUID

object IkeController {

    private val _state = MutableStateFlow(VpnStateService.State.DISABLED)
    val state: StateFlow<VpnStateService.State> = _state

    private val _error = MutableStateFlow(VpnStateService.ErrorState.NO_ERROR)
    val error: StateFlow<VpnStateService.ErrorState> = _error

    @Volatile
    var active = false
        private set

    private var service: VpnStateService? = null
    private var listener: VpnStateService.VpnStateListener? = null

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
    )

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val bound = (binder as? VpnStateService.LocalBinder)?.getService() ?: return
            service = bound
            val l = object : VpnStateService.VpnStateListener {
                override fun stateChanged() = pushState(bound)
            }
            listener = l
            bound.registerListener(l)
            pushState(bound)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            listener = null
        }
    }

    private var statsJob: kotlinx.coroutines.Job? = null

    private fun startStats(context: Context) {
        statsJob?.cancel()
        val app = context.applicationContext
        statsJob = scope.launch {
            val baseUp = android.net.TrafficStats.getTotalTxBytes()
            val baseDown = android.net.TrafficStats.getTotalRxBytes()
            var lastUp = 0L
            var lastDown = 0L
            var lastAt = System.currentTimeMillis()
            while (active) {
                delay(1000)
                val up = (android.net.TrafficStats.getTotalTxBytes() - baseUp).coerceAtLeast(0)
                val down = (android.net.TrafficStats.getTotalRxBytes() - baseDown).coerceAtLeast(0)
                val now = System.currentTimeMillis()
                val secs = ((now - lastAt) / 1000.0).coerceAtLeast(0.001)
                VpnBridge.sendCounters(
                    app, up, down,
                    ((up - lastUp) / secs).toLong().coerceAtLeast(0),
                    ((down - lastDown) / secs).toLong().coerceAtLeast(0)
                )
                lastUp = up
                lastDown = down
                lastAt = now
            }
        }
    }

    private fun pushState(bound: VpnStateService) {
        android.util.Log.w(TAG, "state=" + bound.getState() + " err=" + bound.getErrorState())
        _state.value = bound.getState()
        _error.value = bound.getErrorState()
        when (bound.getState()) {
            VpnStateService.State.CONNECTED -> {
                VpnState.setConnected()
                startStats(bound.applicationContext)
            }
            VpnStateService.State.CONNECTING -> Unit
            else -> if (active && bound.getErrorState() != VpnStateService.ErrorState.NO_ERROR) {
                active = false
                VpnState.setError(errorKey())
            }
        }
    }

    fun bind(context: Context) {
        if (service != null) return
        runCatching {
            android.util.Log.w(TAG, "binding VpnStateService")
            context.applicationContext.bindService(
                Intent(context.applicationContext, VpnStateService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private const val TAG = "GRouteIke"

    private val IP_LITERAL = Regex("^[0-9.]+$|^[0-9a-fA-F:]+$")

    private fun profileFor(context: Context, config: ProxyConfig): UUID? = runCatching {
        val source = VpnProfileSource(context.applicationContext)
        source.open()
        val existing = source.getAllVpnProfiles().firstOrNull { it.getName() == config.id }
        val profile = existing ?: VpnProfile().apply { setUUID(UUID.randomUUID()) }
        profile.setName(config.id)
        profile.setGateway(config.address)
        profile.setVpnType(VpnType.IKEV2_EAP)
        profile.setUsername(config.uuid)
        profile.setPassword(config.password)
        val identity = config.sni.trim()
        if (identity.isNotEmpty()) profile.setRemoteId(identity)
        else if (!IP_LITERAL.matches(config.address)) profile.setRemoteId(config.address)
        else profile.setRemoteId(null)
        profile.setMTU(if (config.mtu in 576..1500) config.mtu else 1400)
        profile.setFlags(
            VpnProfile.FLAGS_SUPPRESS_CERT_REQS or
                    VpnProfile.FLAGS_DISABLE_CRL or
                    VpnProfile.FLAGS_DISABLE_OCSP
        )
        profile.setSplitTunneling(0)
        profile.setSelectedAppsHandling(VpnProfile.SelectedAppsHandling.SELECTED_APPS_ONLY)
        profile.setSelectedApps(java.util.TreeSet<String>())
        if (existing == null) source.insertProfile(profile) else source.updateVpnProfile(profile)
        val id = profile.getUUID()
        source.close()
        android.util.Log.w(TAG, "profile ready uuid=" + id)
        id
    }.onFailure { android.util.Log.e(TAG, "profileFor failed", it) }.getOrNull()

    @Volatile
    private var claimedId: String = ""

    fun claim(config: ProxyConfig) {
        active = true
        claimedId = config.id
        VpnState.setConnecting(config.id)
        scope.launch {
            repeat(12) {
                delay(250)
                if (!active || claimedId != config.id) return@launch
                if (_state.value == VpnStateService.State.CONNECTED) return@launch
                if (VpnState.state.value == Connection.DISCONNECTED)
                    VpnState.setConnecting(config.id)
            }
        }
    }

    fun connect(context: Context, config: ProxyConfig): Boolean {
        active = true
        val uuid = profileFor(context, config)
        if (uuid == null) {
            active = false
            return false
        }
        VpnState.setConnecting(config.id)
        val intent = Intent(context.applicationContext, CharonVpnService::class.java)
        intent.putExtras(Bundle().apply {
            putString(VpnProfileDataSource.KEY_UUID, uuid.toString())
            putString(VpnProfileDataSource.KEY_PASSWORD, config.password)
        })
        android.util.Log.w(TAG, "starting CharonVpnService")
        return runCatching {
            context.applicationContext.startService(intent)
            bind(context)
            android.util.Log.w(TAG, "startService returned")
            true
        }.onFailure {
            active = false
            android.util.Log.e(TAG, "startService failed", it)
        }.getOrDefault(false)
    }

    fun disconnect(context: Context) {
        active = false
        claimedId = ""
        statsJob?.cancel()
        runCatching {
            val intent = Intent(context.applicationContext, CharonVpnService::class.java)
            intent.action = CharonVpnService.DISCONNECT_ACTION
            context.applicationContext.startService(intent)
        }
        service?.disconnect()
        _state.value = VpnStateService.State.DISABLED
    }

    fun errorKey(): String = when (_error.value) {
        VpnStateService.ErrorState.AUTH_FAILED -> "ike_err_auth"
        VpnStateService.ErrorState.PEER_AUTH_FAILED -> "ike_err_peer"
        VpnStateService.ErrorState.LOOKUP_FAILED -> "ike_err_lookup"
        VpnStateService.ErrorState.UNREACHABLE -> "ike_err_unreachable"
        VpnStateService.ErrorState.PASSWORD_MISSING -> "ike_err_password"
        VpnStateService.ErrorState.CERTIFICATE_UNAVAILABLE -> "ike_err_cert"
        else -> "ike_err_generic"
    }
}