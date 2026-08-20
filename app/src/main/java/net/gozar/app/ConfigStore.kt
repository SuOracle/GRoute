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
import org.json.JSONObject
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

    private val _coreLogLevel = MutableStateFlow(prefs.getString(KEY_CORE_LOG, "warning") ?: "warning")
    val coreLogLevel: StateFlow<String> = _coreLogLevel.asStateFlow()
    fun setCoreLogLevel(level: String) {
        _coreLogLevel.value = level
        prefs.edit().putString(KEY_CORE_LOG, level).apply()
    }

    private val _killSwitch = MutableStateFlow(prefs.getBoolean(KEY_KILL_SWITCH, false))
    val killSwitch: StateFlow<Boolean> = _killSwitch.asStateFlow()
    fun setKillSwitch(enabled: Boolean) {
        _killSwitch.value = enabled
        prefs.edit().putBoolean(KEY_KILL_SWITCH, enabled).apply()
    }

    private val _mux = MutableStateFlow(prefs.getBoolean(KEY_MUX, false))
    val mux: StateFlow<Boolean> = _mux.asStateFlow()
    fun setMux(enabled: Boolean) {
        _mux.value = enabled
        prefs.edit().putBoolean(KEY_MUX, enabled).apply()
    }

    private val _muxConcurrency = MutableStateFlow(prefs.getInt(KEY_MUX_CONCURRENCY, 8))
    val muxConcurrency: StateFlow<Int> = _muxConcurrency.asStateFlow()
    fun setMuxConcurrency(value: Int) {
        val v = value.coerceIn(1, 128)
        _muxConcurrency.value = v
        prefs.edit().putInt(KEY_MUX_CONCURRENCY, v).apply()
    }

    private val _globeStyle = MutableStateFlow(prefs.getString(KEY_GLOBE_STYLE, "filled") ?: "filled")
    val globeStyle: StateFlow<String> = _globeStyle.asStateFlow()
    fun setGlobeStyle(style: String) {
        _globeStyle.value = style
        prefs.edit().putString(KEY_GLOBE_STYLE, style).apply()
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

    private val _blockWhenOff = MutableStateFlow(prefs.getBoolean(KEY_BLOCK_WHEN_OFF, false))
    val blockWhenOff: StateFlow<Boolean> = _blockWhenOff.asStateFlow()

    fun setBlockWhenOff(enabled: Boolean) {
        _blockWhenOff.value = enabled
        prefs.edit().putBoolean(KEY_BLOCK_WHEN_OFF, enabled).apply()
    }

    private val _onionRouting = MutableStateFlow(prefs.getBoolean(KEY_ONION, false))
    val onionRouting: StateFlow<Boolean> = _onionRouting.asStateFlow()

    fun setOnionRouting(enabled: Boolean) {
        _onionRouting.value = enabled
        prefs.edit().putBoolean(KEY_ONION, enabled).apply()
    }

    private val _encryptedDns = MutableStateFlow(prefs.getBoolean(KEY_ENC_DNS, false))
    val encryptedDns: StateFlow<Boolean> = _encryptedDns.asStateFlow()

    fun setEncryptedDns(enabled: Boolean) {
        _encryptedDns.value = enabled
        prefs.edit().putBoolean(KEY_ENC_DNS, enabled).apply()
    }

    private val _fakeDns = MutableStateFlow(prefs.getBoolean(KEY_FAKE_DNS, false))
    val fakeDns: StateFlow<Boolean> = _fakeDns.asStateFlow()

    fun setFakeDns(enabled: Boolean) {
        _fakeDns.value = enabled
        prefs.edit().putBoolean(KEY_FAKE_DNS, enabled).apply()
    }

    private val _adBlock = MutableStateFlow(prefs.getBoolean(KEY_AD_BLOCK, false))
    val adBlock: StateFlow<Boolean> = _adBlock.asStateFlow()

    fun setAdBlock(enabled: Boolean) {
        _adBlock.value = enabled
        prefs.edit().putBoolean(KEY_AD_BLOCK, enabled).apply()
    }

    private val _mixedPort = MutableStateFlow(prefs.getInt(KEY_MIXED_PORT, 10626))
    val mixedPort: StateFlow<Int> = _mixedPort.asStateFlow()

    fun setMixedPort(port: Int) {
        val v = port.coerceIn(1024, 65535)
        _mixedPort.value = v
        prefs.edit().putInt(KEY_MIXED_PORT, v).apply()
        MixedPort.value = v
    }

    private val _sortMode = MutableStateFlow(
        prefs.getString(KEY_SORT_MODE, null)
            ?: if (prefs.getBoolean(KEY_SORT_SPEED, false)) SORT_FASTEST else SORT_ADDED
    )
    val sortMode: StateFlow<String> = _sortMode.asStateFlow()

    fun setSortMode(mode: String) {
        _sortMode.value = mode
        prefs.edit().putString(KEY_SORT_MODE, mode).apply()
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

    fun addToLocalSub(name: String, configs: List<ProxyConfig>) {
        if (configs.isEmpty()) return
        val existing = _subscriptions.value.firstOrNull { it.name == name && it.url.isBlank() }
        val sub = existing ?: Subscription(
            name = name,
            url = "",
            lastUpdated = System.currentTimeMillis()
        )
        if (existing == null) _subscriptions.value = _subscriptions.value + sub
        _configs.value = _configs.value + configs.map { it.copy(subId = sub.id) }
        persistConfigs()
        persistSubscriptions()
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

    fun seedDefaultAetherIfNeeded(): ProxyConfig? {
        if (prefs.getBoolean(KEY_AETHER_SEEDED, false)) return null
        prefs.edit().putBoolean(KEY_AETHER_SEEDED, true).apply()
        if (_configs.value.any { it.protocol == "aether" }) return null
        val cfg = ProxyConfig(
            name = "Aether (MASQUE)",
            protocol = "aether",
            address = "127.0.0.1",
            port = AetherController.SOCKS_PORT,
            aetherMode = "masque",
            aetherScan = "balanced",
            aetherHttp2 = true,
            source = ConfigSource.COMMUNITY
        )
        _configs.value = _configs.value + cfg
        persistConfigs()
        if (_selectedId.value.isNullOrEmpty()) setSelectedId(cfg.id)
        return cfg
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

    fun deleteConfigsByIds(ids: Set<String>) {
        if (ids.isEmpty()) return
        _configs.value = _configs.value.filterNot { it.id in ids }
        if (_selectedId.value in ids) setSelectedId(null)
        persistConfigs()
    }

    fun duplicateIds(): Set<String> {
        val seen = HashSet<String>()
        val dupes = LinkedHashSet<String>()
        _configs.value.forEach { c ->
            val key = listOf(
                c.protocol, c.address.trim().lowercase(), c.port.toString(),
                c.uuid, c.password, c.method, c.encryption, c.flow,
                c.alterId.toString(), c.network, c.security, c.sni,
                c.path, c.host, c.publicKey, c.shortId, c.serviceName,
                c.privateKey, c.localAddress, c.torCountry, c.aetherMode
            ).joinToString("\u0000")
            if (!seen.add(key)) dupes.add(c.id)
        }
        return dupes
    }

    fun deleteAllConfigs() {
        _configs.value = emptyList()
        _subscriptions.value = emptyList()
        setSelectedId(null)
        persistConfigs()
        persistSubscriptions()
    }

    fun settingsSnapshot(): JSONObject = JSONObject().apply {
        put("fragment", _fragment.value)
        put("fragmentPackets", _fragmentPackets.value)
        put("fragmentLength", _fragmentLength.value)
        put("fragmentInterval", _fragmentInterval.value)
        put("splitRouting", _splitRouting.value)
        put("sniffing", _sniffing.value)
        put("sniffTypes", JSONArray(_sniffTypes.value.toList()))
        put("killSwitch", _killSwitch.value)
        put("mux", _mux.value)
        put("muxConcurrency", _muxConcurrency.value)
        put("globeStyle", _globeStyle.value)
        put("blockWhenOff", _blockWhenOff.value)
        put("onionRouting", _onionRouting.value)
        put("encryptedDns", _encryptedDns.value)
        put("fakeDns", _fakeDns.value)
        put("adBlock", _adBlock.value)
        put("mixedPort", _mixedPort.value)
        put("sortMode", _sortMode.value)
        put("autoSelect", _autoSelect.value)
        put("autoRefreshHours", _autoRefreshHours.value)
        put("themeMode", _themeMode.value.name)
        put("lang", _lang.value.name)
        put("perAppMode", _perAppMode.value.name)
        put("perAppList", JSONArray(_perAppList.value.toList()))
        put("selectedId", _selectedId.value ?: "")
    }

    fun restoreSettings(o: JSONObject) {
        if (o.has("fragment")) setFragment(o.getBoolean("fragment"))
        if (o.has("fragmentPackets")) setFragmentPackets(o.getString("fragmentPackets"))
        if (o.has("fragmentLength")) setFragmentLength(o.getString("fragmentLength"))
        if (o.has("fragmentInterval")) setFragmentInterval(o.getString("fragmentInterval"))
        if (o.has("splitRouting")) setSplitRouting(o.getBoolean("splitRouting"))
        if (o.has("sniffing")) setSniffing(o.getBoolean("sniffing"))
        o.optJSONArray("sniffTypes")?.let { arr ->
            val set = (0 until arr.length()).map { arr.getString(it) }.toSet()
            _sniffTypes.value = set
            prefs.edit().putStringSet(KEY_SNIFF_TYPES, set).apply()
        }
        if (o.has("killSwitch")) setKillSwitch(o.getBoolean("killSwitch"))
        if (o.has("mux")) setMux(o.getBoolean("mux"))
        if (o.has("muxConcurrency")) setMuxConcurrency(o.getInt("muxConcurrency"))
        if (o.has("globeStyle")) setGlobeStyle(o.getString("globeStyle"))
        if (o.has("blockWhenOff")) setBlockWhenOff(o.getBoolean("blockWhenOff"))
        if (o.has("onionRouting")) setOnionRouting(o.getBoolean("onionRouting"))
        if (o.has("encryptedDns")) setEncryptedDns(o.getBoolean("encryptedDns"))
        if (o.has("fakeDns")) setFakeDns(o.getBoolean("fakeDns"))
        if (o.has("adBlock")) setAdBlock(o.getBoolean("adBlock"))
        if (o.has("mixedPort")) setMixedPort(o.getInt("mixedPort"))
        if (o.has("sortMode")) setSortMode(o.getString("sortMode"))
        if (o.has("autoSelect")) setAutoSelect(o.getBoolean("autoSelect"))
        if (o.has("autoRefreshHours")) setAutoRefreshHours(o.getInt("autoRefreshHours"))
        o.optString("themeMode").takeIf { it.isNotEmpty() }?.let { v ->
            runCatching { setThemeMode(ThemeMode.valueOf(v)) }
        }
        o.optString("lang").takeIf { it.isNotEmpty() }?.let { v ->
            runCatching { setLang(Lang.valueOf(v)) }
        }
        o.optString("perAppMode").takeIf { it.isNotEmpty() }?.let { v ->
            runCatching { setPerAppMode(PerAppMode.valueOf(v)) }
        }
        o.optJSONArray("perAppList")?.let { arr ->
            setPerAppList((0 until arr.length()).map { arr.getString(it) }.toSet())
        }
    }

    fun restoreBackup(configs: List<ProxyConfig>, subs: List<Subscription>, settings: JSONObject?) {
        _configs.value = configs
        _subscriptions.value = subs
        persistConfigs()
        persistSubscriptions()
        settings?.let { restoreSettings(it) }
        val wanted = settings?.optString("selectedId").orEmpty()
        setSelectedId(if (configs.any { it.id == wanted }) wanted else configs.firstOrNull()?.id)
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
        private const val KEY_CORE_LOG = "core_log_level"
        private const val KEY_KILL_SWITCH = "kill_switch_enabled"
        private const val KEY_MUX = "mux_enabled"
        private const val KEY_MUX_CONCURRENCY = "mux_concurrency"
        private const val KEY_GLOBE_STYLE = "globe_style"
        private const val KEY_SNIFF_TYPES = "sniffing_types"
        private const val KEY_AUTOSELECT = "auto_select_fastest"
        private const val KEY_SORT_SPEED = "sort_by_speed"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_MIXED_PORT = "mixed_port"
        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_FAKE_DNS = "fake_dns"
        private const val KEY_ENC_DNS = "encrypted_dns"
        private const val KEY_ONION = "onion_routing"
        private const val KEY_BLOCK_WHEN_OFF = "block_when_off"
        const val SORT_ADDED = "added"
        const val SORT_ALPHA = "alpha"
        const val SORT_FASTEST = "fastest"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_AETHER_SEEDED = "aether_seeded"
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