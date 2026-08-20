package net.gozar.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStreamWriter

object LocalShell : ShellSession {

    private const val MAX_LINES = 3000
    private const val MAX_HISTORY = 100
    private const val SHELL = "/system/bin/sh"
    private const val MARKER = "__GRT_DONE__"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private val jobs = mutableListOf<Job>()

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

    override val echoesInput = false

    private var appContext: Context? = null

    @Synchronized
    private fun append(text: String, kind: ShellLineKind) {
        val clean = if (kind == ShellLineKind.INPUT) text else Ansi.strip(text)
        if (clean.isEmpty() && kind != ShellLineKind.INPUT) return
        val next = _lines.value + ShellLine(clean, kind)
        _lines.value = if (next.size > MAX_LINES) next.takeLast(MAX_LINES) else next
    }

    @Synchronized
    fun start(context: Context) {
        appContext = context.applicationContext
        if (_running.value) return
        val app = context.applicationContext
        runCatching {
            val builder = ProcessBuilder(SHELL)
            builder.directory(app.filesDir)
            builder.environment().apply {
                put("HOME", app.filesDir.absolutePath)
                put("TMPDIR", app.cacheDir.absolutePath)
                put("PATH", "/system/bin:/system/xbin:/vendor/bin:/product/bin")
                put("TERM", "dumb")
                put("PS1", "")
                put("STDBUF", "0")
            }
            val p = builder.start()
            process = p
            writer = BufferedWriter(OutputStreamWriter(p.outputStream))
            _running.value = true
            _busy.value = false
            jobs += scope.launch { pump(p.inputStream, ShellLineKind.OUTPUT) }
            jobs += scope.launch { pump(p.errorStream, ShellLineKind.ERROR) }
            jobs += scope.launch {
                val code = runCatching { p.waitFor() }.getOrDefault(-1)
                _running.value = false
                _busy.value = false
                append("process exited ($code)", ShellLineKind.SYSTEM)
            }
        }.onFailure {
            _running.value = false
            _busy.value = false
            append(it.message ?: "could not start shell", ShellLineKind.ERROR)
        }
    }

    private suspend fun pump(stream: InputStream, kind: ShellLineKind) {
        runCatching {
            stream.bufferedReader().use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith(MARKER)) {
                        _busy.value = false
                        continue
                    }
                    append(line, kind)
                }
            }
        }
    }

    override fun send(command: String) {
        val cmd = command.trim()
        if (cmd.isEmpty()) return
        if (_busy.value) return
        append(cmd, ShellLineKind.INPUT)
        _history.value = (_history.value.filterNot { it == cmd } + cmd).takeLast(MAX_HISTORY)
        if (cmd == "clear" || cmd == "cls" || cmd == "reset") {
            _lines.value = emptyList()
            return
        }
        val w = writer
        if (w == null || !_running.value) {
            append("shell is not running", ShellLineKind.ERROR)
            return
        }
        _busy.value = true
        scope.launch {
            runCatching {
                w.write(cmd)
                w.write("\n")
                w.write("echo $MARKER\$?")
                w.write("\n")
                w.flush()
            }.onFailure {
                _busy.value = false
                append(it.message ?: "write failed", ShellLineKind.ERROR)
            }
        }
    }

    override fun interrupt() {
        if (!_running.value) return
        append("^C", ShellLineKind.SYSTEM)
        if (!_busy.value) return
        val keep = _lines.value
        val ctx = appContext
        stop()
        _lines.value = keep
        if (ctx != null) start(ctx)
    }

    override fun reconnect() {
        val ctx = appContext ?: return
        val keep = _lines.value
        stop()
        _lines.value = keep
        start(ctx)
    }

    @Synchronized
    fun stop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        runCatching { writer?.close() }
        writer = null
        runCatching { process?.destroy() }
        process = null
        _running.value = false
        _busy.value = false
    }

    override fun close() = stop()

    fun restart(context: Context) {
        stop()
        _lines.value = emptyList()
        start(context)
    }

    override fun clear() {
        _lines.value = emptyList()
    }
}