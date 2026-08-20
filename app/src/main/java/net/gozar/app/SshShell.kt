package net.gozar.app

import com.jcraft.jsch.ChannelShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class SshShell(private val hostId: String) : ShellSession {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var channel: ChannelShell? = null
    private var out: OutputStream? = null
    private val readers = mutableListOf<Job>()

    private val _lines = MutableStateFlow<List<ShellLine>>(emptyList())
    override val lines: StateFlow<List<ShellLine>> = _lines.asStateFlow()

    private val _running = MutableStateFlow(false)
    override val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _busy = MutableStateFlow(false)
    override val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    override val history: StateFlow<List<String>> = _history.asStateFlow()

    private val _partial = MutableStateFlow("")
    override val partial: StateFlow<String> = _partial.asStateFlow()

    private val pendingOut = StringBuilder()

    @Volatile
    private var pendingEcho: String? = null

    override val echoesInput = true

    @Synchronized
    private fun append(text: String, kind: ShellLineKind) {
        val clean = Ansi.strip(text)
        val next = _lines.value + ShellLine(clean, kind)
        _lines.value = if (next.size > MAX_LINES) next.takeLast(MAX_LINES) else next
    }

    @Volatile
    private var opening = false

    fun open() {
        if (_running.value || opening) return
        opening = true
        _busy.value = true
        scope.launch {
            runCatching {
                if (SshManager.session(hostId) == null) SshManager.revive(hostId)
                val session = SshManager.session(hostId)
                    ?: throw IllegalStateException("session is not connected")
                val ch = session.openChannel("shell") as ChannelShell
                ch.setPtyType("xterm-256color")
                ch.setPtySize(120, 40, 960, 640)
                val stdout = ch.inputStream
                val stderr = ch.extInputStream
                out = ch.outputStream
                ch.connect(15000)
                channel = ch
                _running.value = true
                readers += scope.launch { pump(stdout, ShellLineKind.OUTPUT) }
                readers += scope.launch { pump(stderr, ShellLineKind.ERROR) }
                readers += scope.launch {
                    while (_running.value) {
                        kotlinx.coroutines.delay(400)
                        if (channel?.isConnected != true) {
                            _running.value = false
                            flushPartial()
                            append("session closed", ShellLineKind.SYSTEM)
                        }
                    }
                }
            }.onFailure {
                _running.value = false
                android.util.Log.w(TAG, "open failed", it)
                append(it.message ?: "could not open shell", ShellLineKind.ERROR)
            }
            opening = false
            _busy.value = false
        }
    }

    @Synchronized
    private fun flushPartial() {
        val text = Ansi.strip(pendingOut.toString())
        pendingOut.setLength(0)
        _partial.value = ""
        if (text.isNotEmpty()) append(text, ShellLineKind.OUTPUT)
    }

    @Synchronized
    private fun consumePrompt(): String {
        val prompt = Ansi.strip(pendingOut.toString())
        pendingOut.setLength(0)
        _partial.value = ""
        return prompt
    }

    @Synchronized
    private fun drainOut(chunk: String) {
        pendingOut.append(chunk)
        var wipe = -1
        for (seq in CLEAR_SEQS) {
            val at = pendingOut.lastIndexOf(seq)
            if (at >= 0 && at + seq.length > wipe) wipe = at + seq.length
        }
        if (wipe >= 0) {
            _lines.value = emptyList()
            pendingOut.delete(0, wipe)
        }
        while (true) {
            val idx = pendingOut.indexOf("\n")
            if (idx < 0) break
            val raw = pendingOut.substring(0, idx).trimEnd('\r')
            pendingOut.delete(0, idx + 1)
            val echo = pendingEcho
            if (echo != null && Ansi.strip(raw).trim() == echo) {
                pendingEcho = null
                continue
            }
            append(raw, ShellLineKind.OUTPUT)
        }
        _partial.value = Ansi.strip(pendingOut.toString())
    }

    private suspend fun pump(stream: InputStream, kind: ShellLineKind) {
        val buf = ByteArray(4096)
        val errBuf = StringBuilder()
        runCatching {
            while (true) {
                val n = stream.read(buf)
                if (n < 0) break
                if (n == 0) continue
                val chunk = String(buf, 0, n, Charsets.UTF_8)
                android.util.Log.d(TAG, "read $n bytes")
                if (kind == ShellLineKind.OUTPUT) {
                    drainOut(chunk)
                } else {
                    errBuf.append(chunk)
                    while (true) {
                        val idx = errBuf.indexOf("\n")
                        if (idx < 0) break
                        append(errBuf.substring(0, idx).trimEnd('\r'), kind)
                        errBuf.delete(0, idx + 1)
                    }
                }
            }
        }.onFailure { android.util.Log.w(TAG, "pump ended", it) }
        if (errBuf.isNotEmpty()) append(errBuf.toString(), kind)
    }

    override fun send(command: String) {
        val cmd = command.trim()
        if (cmd.isNotEmpty()) {
            _history.value = (_history.value.filterNot { it == cmd } + cmd).takeLast(MAX_HISTORY)
        }
        val o = out ?: return
        val prompt = consumePrompt()
        pendingEcho = cmd.ifEmpty { null }
        append(if (prompt.isEmpty()) cmd else "$prompt $cmd", ShellLineKind.INPUT)
        scope.launch {
            runCatching {
                o.write((command + "\n").toByteArray(Charsets.UTF_8))
                o.flush()
            }.onFailure { append(it.message ?: "write failed", ShellLineKind.ERROR) }
        }
    }

    override fun interrupt() {
        val o = out ?: return
        scope.launch {
            runCatching {
                o.write(byteArrayOf(3))
                o.flush()
            }
        }
    }

    override fun reconnect() {
        readers.forEach { it.cancel() }
        readers.clear()
        runCatching { out?.close() }
        out = null
        runCatching { channel?.disconnect() }
        channel = null
        _running.value = false
        _partial.value = ""
        opening = false
        open()
    }

    override fun clear() {
        _lines.value = emptyList()
        synchronized(this) { pendingOut.setLength(0) }
        _partial.value = ""
    }

    override fun close() {
        readers.forEach { it.cancel() }
        readers.clear()
        runCatching { out?.close() }
        out = null
        runCatching { channel?.disconnect() }
        channel = null
        _running.value = false
        _partial.value = ""
    }

    private companion object {
        const val TAG = "GRouteSshShell"
        const val MAX_LINES = 3000
        const val MAX_HISTORY = 100
        val CLEAR_SEQS = listOf("\u001B[2J", "\u001B[3J", "\u001BC")
    }
}
