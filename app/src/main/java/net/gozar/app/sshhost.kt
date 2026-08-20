package net.gozar.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SshHost(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val address: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String = "",
    val direct: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) {
    val title: String
        get() = label.trim().ifEmpty {
            if (username.isBlank()) address else username.trim() + "@" + address.trim()
        }

    val endpoint: String
        get() = address.trim() + (if (port != 22) ":" + port else "")
}

class SshStore private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _hosts = MutableStateFlow<List<SshHost>>(emptyList())
    val hosts: StateFlow<List<SshHost>> = _hosts.asStateFlow()

    init {
        _hosts.value = load()
    }

    fun add(host: SshHost) {
        _hosts.value = _hosts.value + host
        persist()
    }

    fun update(host: SshHost) {
        _hosts.value = _hosts.value.map { if (it.id == host.id) host else it }
        persist()
    }

    fun remove(id: String) {
        SshManager.forget(id)
        _hosts.value = _hosts.value.filterNot { it.id == id }
        persist()
    }

    fun find(id: String?): SshHost? = _hosts.value.find { it.id == id }

    fun linkedHostId(configId: String): String? =
        prefs.getString(KEY_LINK + configId, null)

    fun link(configId: String, hostId: String) {
        runCatching { prefs.edit().putString(KEY_LINK + configId, hostId).apply() }
    }

    fun panelKind(configId: String): String = prefs.getString(KEY_PANEL_KIND + configId, "").orEmpty()
    fun panelUrl(configId: String): String = prefs.getString(KEY_PANEL_URL + configId, "").orEmpty()
    fun panelUser(configId: String): String = prefs.getString(KEY_PANEL_USER + configId, "").orEmpty()
    fun panelPass(configId: String): String =
        prefs.getString(KEY_PANEL_PASS + configId, null)?.let { Crypto.decrypt(it) }.orEmpty()

    fun savePanel(configId: String, kind: String, url: String, user: String, pass: String) {
        runCatching {
            prefs.edit()
                .putString(KEY_PANEL_KIND + configId, kind)
                .putString(KEY_PANEL_URL + configId, url)
                .putString(KEY_PANEL_USER + configId, user)
                .putString(KEY_PANEL_PASS + configId, if (pass.isEmpty()) "" else Crypto.encrypt(pass).orEmpty())
                .apply()
        }
    }

    private fun persist() {
        val snapshot = _hosts.value
        scope.launch {
            val arr = JSONArray()
            snapshot.forEach { h ->
                arr.put(JSONObject().apply {
                    put("id", h.id)
                    put("label", h.label)
                    put("address", h.address)
                    put("port", h.port)
                    put("username", h.username)
                    put("password", if (h.password.isEmpty()) "" else Crypto.encrypt(h.password).orEmpty())
                    put("direct", h.direct)
                    put("addedAt", h.addedAt)
                })
            }
            runCatching { prefs.edit().putString(KEY_HOSTS, arr.toString()).apply() }
        }
    }

    private fun load(): List<SshHost> = runCatching {
        val raw = prefs.getString(KEY_HOSTS, null) ?: return emptyList()
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val blob = o.optString("password")
            SshHost(
                id = o.optString("id").ifEmpty { UUID.randomUUID().toString() },
                label = o.optString("label"),
                address = o.optString("address"),
                port = o.optInt("port", 22),
                username = o.optString("username"),
                password = if (blob.isEmpty()) "" else Crypto.decrypt(blob).orEmpty(),
                direct = o.optBoolean("direct", false),
                addedAt = o.optLong("addedAt", 0L)
            )
        }
    }.getOrDefault(emptyList())

    companion object {
        private const val PREFS = "gozar-ssh"
        private const val KEY_HOSTS = "hosts"
        private const val KEY_LINK = "link:"
        private const val KEY_PANEL_KIND = "pkind:"
        private const val KEY_PANEL_URL = "purl:"
        private const val KEY_PANEL_USER = "puser:"
        private const val KEY_PANEL_PASS = "ppass:"

        @Volatile
        private var instance: SshStore? = null

        fun get(context: Context): SshStore = instance ?: synchronized(this) {
            instance ?: SshStore(context.applicationContext).also { instance = it }
        }
    }
}
