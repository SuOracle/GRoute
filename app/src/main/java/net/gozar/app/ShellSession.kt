package net.gozar.app

import kotlinx.coroutines.flow.StateFlow

data class ShellLine(val text: String, val kind: ShellLineKind)

enum class ShellLineKind { INPUT, OUTPUT, ERROR, SYSTEM }

interface ShellSession {
    val lines: StateFlow<List<ShellLine>>
    val running: StateFlow<Boolean>
    val busy: StateFlow<Boolean>
    val history: StateFlow<List<String>>
    val partial: StateFlow<String>
    val echoesInput: Boolean
    fun send(command: String)
    fun interrupt()
    fun reconnect()
    fun clear()
    fun close()
}

internal object Ansi {
    private val CSI = Regex("\u001B\\[[0-?]*[ -/]*[@-~]")
    private val OSC = Regex("\u001B][^\u0007\u001B]*(?:\u0007|\u001B\\\\)")
    private val ESC = Regex("\u001B[@-_]")
    private val CTRL = Regex("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]")

    fun strip(raw: String): String {
        var out = CSI.replace(raw, "")
        out = OSC.replace(out, "")
        out = ESC.replace(out, "")
        out = CTRL.replace(out, "")
        return out.trimEnd()
    }
}