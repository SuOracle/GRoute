package net.gozar.app

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.concurrent.Executors

enum class PerAppMode { OFF, ALLOWLIST, BLOCKLIST }
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }
class ConfigStore private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("gozarnet", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val writeDispatcher =
        Executors.newSingleThreadExecutor { r -> Thread(r, "gozar-config-io") }.asCoroutineDispatcher()

    private val loadedSignal = CompletableDeferred<Unit>()

    suspend fun awaitReady() = loadedSignal.await()

    private val _configs = MutableStateFlow<List<ProxyConfig>>(emptyList())
    val configs: StateFlow<List<ProxyConfig>> = _configs.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    init {
        scope.launch {
            val cfgs = loadConfigs()
            val subs = loadSubscriptions()
            _configs.value = cfgs
            _subscriptions.value = subs
            loadedSignal.complete(Unit)
        }
    }

    private val _fragment = MutableStateFlow(prefs.getBoolean(KEY_FRAGMENT, false))
    val fragment: StateFlow<Boolean> = _fragment.asStateFlow()

    private val _fragmentPackets = MutableStateFlow(prefs.getString(KEY_FRAG_PACKETS, "tlshello") ?: "tlshello")
    val fragmentPackets: StateFlow<String> = _fragmentPackets.asStateFlow()
    fun setFragmentPackets(v: String) {
        _fragmentPackets.value = v
        prefs.edit().putString(KEY_FRAG_PACKETS, v).apply()
    }

    private val _fragmentLength = MutableStateFlow(prefs.getString(KEY_FRAG_LENGTH, "10-20") ?: "10-20")
    val fragmentLength: StateFlow<String> = _fragmentLength.asStateFlow()
    fun setFragmentLength(v: String) {
        _fragmentLength.value = v
        prefs.edit().putString(KEY_FRAG_LENGTH, v).apply()
    }

    private val _fragmentInterval = MutableStateFlow(prefs.getString(KEY_FRAG_INTERVAL, "10-20") ?: "10-20")
    val fragmentInterval: StateFlow<String> = _fragmentInterval.asStateFlow()
    fun setFragmentInterval(v: String) {
        _fragmentInterval.value = v
        prefs.edit().putString(KEY_FRAG_INTERVAL, v).apply()
    }

    private val _splitRouting = MutableStateFlow(prefs.getBoolean(KEY_SPLIT, false))
    val splitRouting: StateFlow<Boolean> = _splitRouting.asStateFlow()

    private val _sniffing = MutableStateFlow(prefs.getBoolean(KEY_SNIFFING, false))
    val sniffing: StateFlow<Boolean> = _sniffing.asStateFlow()
    fun setSniffing(enabled: Boolean) {
        _sniffing.value = enabled
        prefs.edit().putBoolean(KEY_SNIFFING, enabled).apply()
    }

    private val _sniffTypes = MutableStateFlow(loadSniffTypes())
    val sniffTypes: StateFlow<Set<String>> = _sniffTypes.asStateFlow()

    private fun loadSniffTypes(): Set<String> =
        prefs.getStringSet(KEY_SNIFF_TYPES, null)?.toSet() ?: setOf("http", "tls", "quic")

    fun toggleSniffType(type: String) {
        val cur = _sniffTypes.value.toMutableSet()
        if (!cur.add(type)) cur.remove(type)
        _sniffTypes.value = cur
        prefs.edit().putStringSet(KEY_SNIFF_TYPES, cur).apply()
    }

    private val _sortBySpeed = MutableStateFlow(prefs.getBoolean(KEY_SORT_SPEED, false))
    val sortBySpeed: StateFlow<Boolean> = _sortBySpeed.asStateFlow()

    fun setSortBySpeed(enabled: Boolean) {
        _sortBySpeed.value = enabled
        prefs.edit().putBoolean(KEY_SORT_SPEED, enabled).apply()
    }

    private val _autoSelect = MutableStateFlow(prefs.getBoolean(KEY_AUTOSELECT, false))
    val autoSelect: StateFlow<Boolean> = _autoSelect.asStateFlow()

    fun setAutoSelect(enabled: Boolean) {
        _autoSelect.value = enabled
        prefs.edit().putBoolean(KEY_AUTOSELECT, enabled).apply()
    }

    private val _autoRefreshHours = MutableStateFlow(prefs.getInt(KEY_AUTOREFRESH, DEFAULT_AUTOREFRESH))
    val autoRefreshHours: StateFlow<Int> = _autoRefreshHours.asStateFlow()

    fun setAutoRefreshHours(hours: Int) {
        _autoRefreshHours.value = hours
        prefs.edit().putInt(KEY_AUTOREFRESH, hours).apply()
    }

    private val _lang = MutableStateFlow(loadLang())
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun loadThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "DARK") }
            .getOrDefault(ThemeMode.DARK)

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    private val _selectedId = MutableStateFlow(prefs.getString(KEY_SELECTED, null))
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    fun setSelectedId(id: String?) {
        _selectedId.value = id
        prefs.edit().putString(KEY_SELECTED, id).apply()
    }

    fun setLang(lang: Lang) {
        _lang.value = lang
        prefs.edit().putString(KEY_LANG, lang.name).apply()
    }

    private fun loadLang(): Lang {
        val saved = prefs.getString(KEY_LANG, null)
        return if (saved != null) {
            runCatching { Lang.valueOf(saved) }.getOrDefault(defaultLang())
        } else defaultLang()
    }

    private fun defaultLang(): Lang =
        if (java.util.Locale.getDefault().language == "fa") Lang.FA else Lang.EN

    fun setSplitRouting(enabled: Boolean) {
        _splitRouting.value = enabled
        prefs.edit().putBoolean(KEY_SPLIT, enabled).apply()
    }

    fun setFragment(enabled: Boolean) {
        _fragment.value = enabled
        prefs.edit().putBoolean(KEY_FRAGMENT, enabled).apply()
    }

    fun add(config: ProxyConfig) {
        _configs.value = _configs.value + config
        persistConfigs()
    }

    fun update(config: ProxyConfig) {
        _configs.value = _configs.value.map { existing ->
            when {
                existing.id != config.id -> existing
                existing.locked -> existing.copy(name = config.name)
                else -> config
            }
        }
        persistConfigs()
    }

    fun addImported(imported: List<ProxyConfig>): Int {
        if (imported.isEmpty()) return 0
        _configs.value = _configs.value + imported
        persistConfigs()
        return imported.size
    }

    fun delete(id: String) {
        _configs.value = _configs.value.filterNot { it.id == id }
        if (_selectedId.value == id) setSelectedId(null)
        persistConfigs()
    }

    fun seedDefaultSubscriptionIfNeeded(): Subscription? {
        if (DEFAULT_SUB_URL.isBlank()) return null
        if (prefs.getBoolean(KEY_DEFAULT_SEEDED, false)) return null
        prefs.edit().putBoolean(KEY_DEFAULT_SEEDED, true).apply()
        if (_subscriptions.value.any { it.id == DEFAULT_SUB_ID }) return null
        val sub = Subscription(
            name = DEFAULT_SUB_NAME,
            url = DEFAULT_SUB_URL,
            lastUpdated = 0L,
            id = DEFAULT_SUB_ID
        )
        _subscriptions.value = _subscriptions.value + sub
        persistSubscriptions()
        return sub
    }

    fun defaultSubPendingFirstFetch(): Subscription? =
        _subscriptions.value.firstOrNull { it.id == DEFAULT_SUB_ID && it.lastUpdated == 0L }

    fun migrateDefaultSubUrlIfNeeded(): Subscription? {
        if (DEFAULT_SUB_URL.isBlank()) return null
        val existing = _subscriptions.value.firstOrNull { it.id == DEFAULT_SUB_ID } ?: return null
        if (existing.url == DEFAULT_SUB_URL) return null
        val updated = existing.copy(url = DEFAULT_SUB_URL, lastUpdated = 0L)
        _subscriptions.value = _subscriptions.value.map { if (it.id == DEFAULT_SUB_ID) updated else it }
        persistSubscriptions()
        return updated
    }

    fun upsertSubscription(sub: Subscription, fetched: List<ProxyConfig>) {
        val oldBySig = _configs.value.filter { it.subId == sub.id }
            .associateBy { sigOf(it) }.toMutableMap()
        val tagged = fetched.map { f ->
            val kept = oldBySig.remove(sigOf(f))
            f.copy(subId = sub.id, id = kept?.id ?: f.id)
        }
        _configs.value = _configs.value.filterNot { it.subId == sub.id } + tagged
        _subscriptions.value = _subscriptions.value.filterNot { it.id == sub.id } + sub
        persistConfigs()
        persistSubscriptions()
    }

    private fun sigOf(c: ProxyConfig): String =
        "${c.protocol}|${c.address}|${c.port}|${c.uuid}|${c.password}"

    fun renameSubscription(id: String, newName: String) {
        _subscriptions.value = _subscriptions.value.map { if (it.id == id) it.copy(name = newName) else it }
        persistSubscriptions()
    }

    fun deleteSubscription(id: String) {
        _configs.value = _configs.value.filterNot { it.subId == id }
        _subscriptions.value = _subscriptions.value.filterNot { it.id == id }
        persistConfigs()
        persistSubscriptions()
    }

    private fun persistConfigs() {
        val arr = JSONArray()
        _configs.value.forEach { arr.put(it.toJson()) }
        putSecret(KEY_CONFIGS, arr.toString())
    }

    private fun persistSubscriptions() {
        val arr = JSONArray()
        _subscriptions.value.forEach { arr.put(it.toJson()) }
        putSecret(KEY_SUBS, arr.toString())
    }

    private fun putSecret(key: String, json: String) {
        scope.launch(writeDispatcher) {
            prefs.edit().putString(key, Crypto.encrypt(json) ?: json).apply()
        }
    }

    private fun readSecret(key: String): String? {
        val raw = prefs.getString(key, null) ?: return null
        Crypto.decrypt(raw)?.let { return it }
        val trimmed = raw.trimStart()
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            Crypto.encrypt(raw)?.let { prefs.edit().putString(key, it).apply() }
            return raw
        }
        return null
    }

    private fun loadConfigs(): List<ProxyConfig> {
        val raw = readSecret(KEY_CONFIGS) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { ProxyConfig.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
    }

    private fun loadSubscriptions(): List<Subscription> {
        val raw = readSecret(KEY_SUBS) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Subscription.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
    }

    private val _perAppMode = MutableStateFlow(loadPerAppMode())
    val perAppMode: StateFlow<PerAppMode> = _perAppMode

    private val _perAppList = MutableStateFlow(loadPerAppList())
    val perAppList: StateFlow<Set<String>> = _perAppList

    private fun loadPerAppMode(): PerAppMode =
        runCatching { PerAppMode.valueOf(prefs.getString(KEY_PERAPP_MODE, null) ?: "OFF") }
            .getOrDefault(PerAppMode.OFF)

    private fun loadPerAppList(): Set<String> =
        prefs.getStringSet(KEY_PERAPP_LIST, emptySet())?.toSet() ?: emptySet()

    fun setPerAppMode(mode: PerAppMode) {
        _perAppMode.value = mode
        prefs.edit().putString(KEY_PERAPP_MODE, mode.name).apply()
    }

    fun setPerAppList(pkgs: Set<String>) {
        _perAppList.value = pkgs
        prefs.edit().putStringSet(KEY_PERAPP_LIST, pkgs).apply()
    }

    fun togglePerApp(pkg: String) {
        val cur = _perAppList.value.toMutableSet()
        if (!cur.add(pkg)) cur.remove(pkg)
        setPerAppList(cur)
    }

    private val _expandedSubs = MutableStateFlow(loadExpandedSubs())
    val expandedSubs: StateFlow<Set<String>> = _expandedSubs

    private fun loadExpandedSubs(): Set<String> =
        prefs.getStringSet(KEY_EXPANDED_SUBS, emptySet())?.toSet() ?: emptySet()

    fun toggleSubExpanded(id: String) {
        val cur = _expandedSubs.value.toMutableSet()
        if (!cur.add(id)) cur.remove(id)
        _expandedSubs.value = cur
        prefs.edit().putStringSet(KEY_EXPANDED_SUBS, cur).apply()
    }

    fun lastUpdateCheck(): Long = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0L)

    fun markUpdateChecked() {
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, System.currentTimeMillis()).apply()
    }

    fun saveLastTest(json: String, timeMillis: Long) {
        prefs.edit().putString(KEY_LAST_TEST, json).putLong(KEY_LAST_TEST_TIME, timeMillis).apply()
    }

    fun lastTestJson(): String? = prefs.getString(KEY_LAST_TEST, null)

    fun lastTestTime(): Long = prefs.getLong(KEY_LAST_TEST_TIME, 0L)

    companion object {
        @Volatile private var instance: ConfigStore? = null

        fun get(context: Context): ConfigStore =
            instance ?: synchronized(this) {
                instance ?: ConfigStore(context.applicationContext).also { instance = it }
            }

        private const val KEY_CONFIGS = "configs"
        private const val KEY_SUBS = "subscriptions"
        private const val KEY_FRAGMENT = "fragment_enabled"
        private const val KEY_FRAG_PACKETS = "fragment_packets"
        private const val KEY_FRAG_LENGTH = "fragment_length"
        private const val KEY_FRAG_INTERVAL = "fragment_interval"
        private const val KEY_SPLIT = "split_routing_enabled"
        private const val KEY_SNIFFING = "sniffing_enabled"
        private const val KEY_SNIFF_TYPES = "sniffing_types"
        private const val KEY_AUTOSELECT = "auto_select_fastest"
        private const val KEY_SORT_SPEED = "sort_by_speed"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DEFAULT_SEEDED = "default_sub_seeded"
        private const val DEFAULT_SUB_ID = "default-sub"
        private const val DEFAULT_SUB_NAME = "Default Sub"
        private val DEFAULT_SUB_URL = BuildConfig.DEFAULT_SUB_URL
        private const val KEY_AUTOREFRESH = "auto_refresh_hours"
        private const val DEFAULT_AUTOREFRESH = 1
        private const val KEY_LANG = "app_lang"
        private const val KEY_SELECTED = "selected_config_id"
        private const val KEY_PERAPP_MODE = "perapp_mode"
        private const val KEY_PERAPP_LIST = "perapp_list"
        private const val KEY_EXPANDED_SUBS = "expanded_subs"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_LAST_TEST = "last_test_json"
        private const val KEY_LAST_TEST_TIME = "last_test_time"
    }
}