package net.gozar.app

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

data class SftpEntry(
    val name: String,
    val isDir: Boolean,
    val isLink: Boolean,
    val size: Long,
    val modified: Long,
    val permissions: String
)

object SftpBrowser {

    private const val TAG = "GRouteSftp"
    private const val TIMEOUT = 15_000

    private val channels = ConcurrentHashMap<String, ChannelSftp>()

    private suspend fun channel(hostId: String): ChannelSftp? {
        channels[hostId]?.let { if (it.isConnected) return it else channels.remove(hostId) }
        if (SshManager.session(hostId) == null) SshManager.revive(hostId)
        val session = SshManager.session(hostId) ?: return null
        return runCatching {
            val ch = session.openChannel("sftp") as ChannelSftp
            ch.connect(TIMEOUT)
            channels[hostId] = ch
            ch
        }.onFailure { android.util.Log.w(TAG, "open failed", it) }.getOrNull()
    }

    fun close(hostId: String) {
        channels.remove(hostId)?.let { runCatching { it.disconnect() } }
    }

    suspend fun home(hostId: String): String = withContext(Dispatchers.IO) {
        runCatching { channel(hostId)?.home }.getOrNull().orEmpty().ifBlank { "/" }
    }

    suspend fun list(hostId: String, path: String): Result<List<SftpEntry>> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching {
                @Suppress("UNCHECKED_CAST")
                val raw = ch.ls(path) as java.util.Vector<ChannelSftp.LsEntry>
                raw.asSequence()
                    .filter { it.filename != "." && it.filename != ".." }
                    .map { e ->
                        val a: SftpATTRS = e.attrs
                        SftpEntry(
                            name = e.filename,
                            isDir = a.isDir,
                            isLink = a.isLink,
                            size = a.size,
                            modified = a.mTime.toLong() * 1000L,
                            permissions = a.permissionsString.orEmpty()
                        )
                    }
                    .sortedWith(compareByDescending<SftpEntry> { it.isDir }.thenBy { it.name.lowercase() })
                    .toList()
            }
        }

    suspend fun download(hostId: String, remote: String, sink: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching {
                sink.use { out -> ch.get(remote).use { input -> input.copyTo(out) } }
                Unit
            }
        }

    suspend fun upload(hostId: String, source: InputStream, remote: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching { source.use { ch.put(it, remote) } }
        }

    suspend fun delete(hostId: String, path: String, isDir: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching { if (isDir) ch.rmdir(path) else ch.rm(path) }
        }

    suspend fun mkdir(hostId: String, path: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching { ch.mkdir(path) }
        }

    suspend fun rename(hostId: String, from: String, to: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ch = channel(hostId)
                ?: return@withContext Result.failure(IllegalStateException("not connected"))
            runCatching { ch.rename(from, to) }
        }

    fun join(dir: String, name: String): String =
        if (dir.endsWith("/")) dir + name else "$dir/$name"

    fun parent(path: String): String {
        val trimmed = path.trimEnd('/')
        if (trimmed.isEmpty() || trimmed == "/") return "/"
        val cut = trimmed.lastIndexOf('/')
        return if (cut <= 0) "/" else trimmed.substring(0, cut)
    }

    fun humanSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / 1048576.0)
        else -> "%.2f GB".format(bytes / 1073741824.0)
    }
}
