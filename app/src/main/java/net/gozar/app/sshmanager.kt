package net.gozar.app

import com.jcraft.jsch.JSch
import com.jcraft.jsch.ProxySOCKS5
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

sealed class SshStatus {
    object Idle : SshStatus()
    object Connecting : SshStatus()
    data class Up(val viaTunnel: Boolean) : SshStatus()
    data class Failed(val messageKey: String, val detail: String) : SshStatus()
}

object SshManager {

    private const val TAG = "GRouteSsh"
    private const val CONNECT_TIMEOUT_MS = 20_000

    private val sessions = ConcurrentHashMap<String, Session>()
    private val hosts = ConcurrentHashMap<String, SshHost>()

    private val _status = MutableStateFlow<Map<String, SshStatus>>(emptyMap())
    val status: StateFlow<Map<String, SshStatus>> = _status.asStateFlow()

    fun statusOf(id: String): SshStatus = _status.value[id] ?: SshStatus.Idle

    private fun set(id: String, s: SshStatus) {
        _status.value = _status.value.toMutableMap().apply { put(id, s) }
    }

    fun tunnelAvailable(): Boolean =
        VpnState.state.value == Connection.CONNECTED && !IkeController.active

    fun willUseTunnel(host: SshHost): Boolean = !host.direct && tunnelAvailable()

    suspend fun connect(host: SshHost): SshStatus = withContext(Dispatchers.IO) {
        hosts[host.id] = host
        disconnect(host.id)
        set(host.id, SshStatus.Connecting)
        val viaTunnel = willUseTunnel(host)
        try {
            val session = JSch().getSession(
                host.username.trim(),
                host.address.trim(),
                host.port.coerceIn(1, 65535)
            )
            session.setPassword(host.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setConfig("PreferredAuthentications", "password,keyboard-interactive")
            if (viaTunnel) session.setProxy(ProxySOCKS5("127.0.0.1", MixedPort.value))
            session.timeout = CONNECT_TIMEOUT_MS
            session.connect(CONNECT_TIMEOUT_MS)
            sessions[host.id] = session
            android.util.Log.i(TAG, "connected ${host.endpoint} viaTunnel=$viaTunnel")
            SshStatus.Up(viaTunnel).also { set(host.id, it) }
        } catch (e: Throwable) {
            android.util.Log.w(TAG, "connect failed for ${host.endpoint}", e)
            SshStatus.Failed(keyFor(e), e.message.orEmpty()).also { set(host.id, it) }
        }
    }

    fun forget(id: String) {
        hosts.remove(id)
        disconnect(id)
    }

    fun disconnect(id: String) {
        SftpBrowser.close(id)
        shells.remove(id)?.close()
        sessions.remove(id)?.let { runCatching { it.disconnect() } }
        val current = _status.value[id]
        if (current is SshStatus.Up || current is SshStatus.Connecting) set(id, SshStatus.Idle)
    }

    fun disconnectAll() {
        sessions.keys.toList().forEach { disconnect(it) }
    }

    fun session(id: String): Session? = sessions[id]?.takeIf { it.isConnected }

    fun knownHost(id: String): SshHost? = hosts[id]

    suspend fun revive(id: String): Boolean {
        session(id)?.let { return true }
        val host = hosts[id] ?: return false
        return connect(host) is SshStatus.Up
    }

    data class ExecResult(val code: Int, val out: String, val err: String) {
        val ok: Boolean get() = code == 0
        val text: String get() = if (err.isBlank()) out else (out + "\n" + err).trim()
    }

    suspend fun exec(id: String, command: String, timeoutMs: Int = 15_000): ExecResult =
        withContext(Dispatchers.IO) {
            val s = session(id) ?: return@withContext ExecResult(-1, "", "not connected")
            var ch: com.jcraft.jsch.ChannelExec? = null
            try {
                ch = s.openChannel("exec") as com.jcraft.jsch.ChannelExec
                ch.setCommand(command)
                val errBuf = java.io.ByteArrayOutputStream()
                ch.setErrStream(errBuf)
                val stream = ch.inputStream
                ch.connect(timeoutMs)
                val stdout = stream.bufferedReader().readText()
                var waited = 0
                while (!ch.isClosed && waited < timeoutMs) {
                    kotlinx.coroutines.delay(40)
                    waited += 40
                }
                ExecResult(ch.exitStatus, stdout.trim(), errBuf.toString("UTF-8").trim())
            } catch (e: Throwable) {
                android.util.Log.w(TAG, "exec failed: $command", e)
                ExecResult(-1, "", e.message.orEmpty())
            } finally {
                runCatching { ch?.disconnect() }
            }
        }

    private val shells = ConcurrentHashMap<String, SshShell>()

    fun shell(id: String): SshShell? {
        val existing = shells[id]
        if (existing != null) {
            existing.open()
            return existing
        }
        if (session(id) == null && !hosts.containsKey(id)) return null
        val created = SshShell(id)
        shells[id] = created
        created.open()
        return created
    }

    fun closeShell(id: String) {
        shells.remove(id)?.close()
    }

    fun closeSftp(id: String) = SftpBrowser.close(id)

    fun isUp(id: String): Boolean = sessions[id]?.isConnected == true

    private fun keyFor(e: Throwable): String {
        val m = (e.message ?: "").lowercase()
        return when {
            m.contains("auth fail") || m.contains("auth cancel") -> "ssh_err_auth"
            m.contains("unknownhost") || m.contains("unknown host") ||
                m.contains("no such host") || m.contains("unable to resolve") -> "ssh_err_host"
            m.contains("timeout") || m.contains("timed out") -> "ssh_err_timeout"
            m.contains("connection refused") || m.contains("econnrefused") -> "ssh_err_refused"
            m.contains("proxy") || m.contains("socks") -> "ssh_err_proxy"
            else -> "ssh_err_generic"
        }
    }
}
