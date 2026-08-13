package net.gozar.app

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
object UsageStore {

    const val RANGE_ALL = Int.MAX_VALUE
    private const val HOURLY_RETENTION_HOURS = 24L * 31   // ~30 days
    private val HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")

    private lateinit var prefs: SharedPreferences
    @Volatile private var initialized = false
    private val lock = Any()
    private var ticksSincePersist = 0

    private val _usage = MutableStateFlow<Map<String, LongArray>>(emptyMap())
    val usage: StateFlow<Map<String, LongArray>> = _usage.asStateFlow()      // daily

    private val _hourly = MutableStateFlow<Map<String, LongArray>>(emptyMap())
    val hourly: StateFlow<Map<String, LongArray>> = _hourly.asStateFlow()    // hourly

    private val _byConfig = MutableStateFlow<Map<String, LongArray>>(emptyMap())
    val byConfig: StateFlow<Map<String, LongArray>> = _byConfig.asStateFlow()

    private val _dailyCfg = MutableStateFlow<Map<String, Map<String, LongArray>>>(emptyMap())
    val dailyCfg: StateFlow<Map<String, Map<String, LongArray>>> = _dailyCfg.asStateFlow()

    private val _hourlyCfg = MutableStateFlow<Map<String, Map<String, LongArray>>>(emptyMap())
    val hourlyCfg: StateFlow<Map<String, Map<String, LongArray>>> = _hourlyCfg.asStateFlow()

    const val DIRECT_KEY = "__direct__"
    @Volatile var currentConfigKey: String? = null

    fun init(context: Context) {
        synchronized(lock) {
            if (initialized) return
            prefs = context.applicationContext.getSharedPreferences("gozarnet_usage", Context.MODE_PRIVATE)
            initialized = true
        }
        Thread({
            val daily = load(KEY_DAILY)
            val hourly = load(KEY_HOURLY)
            val byCfg = load(KEY_BY_CONFIG)
            val dailyCfg = loadNested(KEY_DAILY_CFG)
            val hourlyCfg = loadNested(KEY_HOURLY_CFG)
            synchronized(lock) {
                _usage.value = mergeCounts(daily, _usage.value)
                _hourly.value = trimHourly(mergeCounts(hourly, _hourly.value))
                _byConfig.value = mergeCounts(byCfg, _byConfig.value)
                _dailyCfg.value = mergeNested(dailyCfg, _dailyCfg.value)
                _hourlyCfg.value = trimHourlyCfg(mergeNested(hourlyCfg, _hourlyCfg.value))
            }
        }, "usage-load").start()
    }

    private fun mergeCounts(
        disk: Map<String, LongArray>,
        mem: Map<String, LongArray>
    ): Map<String, LongArray> {
        if (mem.isEmpty()) return disk
        val out = HashMap(disk)
        mem.forEach { (k, v) ->
            val cur = out[k]
            out[k] = if (cur == null) v
            else longArrayOf(cur[0] + v[0], cur[1] + v[1])
        }
        return out
    }


    // Called once per second while connected.
    fun add(up: Long, down: Long) = add(up, down, currentConfigKey)

    fun add(up: Long, down: Long, configKey: String?) {
        if (!initialized || (up <= 0 && down <= 0)) return
        synchronized(lock) {
            val now = LocalDateTime.now()
            val dayKey = now.toLocalDate().toString()
            val hourKey = now.format(HOUR_FMT)

            val daily = HashMap(_usage.value)
            val dcur = daily[dayKey] ?: longArrayOf(0L, 0L)
            daily[dayKey] = longArrayOf(dcur[0] + up, dcur[1] + down)
            _usage.value = daily

            val hourly = HashMap(_hourly.value)
            val hcur = hourly[hourKey] ?: longArrayOf(0L, 0L)
            hourly[hourKey] = longArrayOf(hcur[0] + up, hcur[1] + down)
            _hourly.value = trimHourly(hourly)

            if (!configKey.isNullOrEmpty()) {
                val byCfg = HashMap(_byConfig.value)
                val ccur = byCfg[configKey] ?: longArrayOf(0L, 0L)
                byCfg[configKey] = longArrayOf(ccur[0] + up, ccur[1] + down)
                _byConfig.value = byCfg

                _dailyCfg.value = bump(_dailyCfg.value, dayKey, configKey, up, down)
                _hourlyCfg.value = trimHourlyCfg(bump(_hourlyCfg.value, hourKey, configKey, up, down))
            }

            if (++ticksSincePersist >= 5) {
                persist(KEY_DAILY, _usage.value)
                persist(KEY_HOURLY, _hourly.value)
                persist(KEY_BY_CONFIG, _byConfig.value)
                persistNested(KEY_DAILY_CFG, _dailyCfg.value)
                persistNested(KEY_HOURLY_CFG, _hourlyCfg.value)
                ticksSincePersist = 0
            }
        }
    }

    private fun bump(
        src: Map<String, Map<String, LongArray>>,
        bucket: String,
        cfg: String,
        up: Long,
        down: Long
    ): Map<String, Map<String, LongArray>> {
        val out = HashMap(src)
        val inner = HashMap(out[bucket] ?: emptyMap())
        val cur = inner[cfg] ?: longArrayOf(0L, 0L)
        inner[cfg] = longArrayOf(cur[0] + up, cur[1] + down)
        out[bucket] = inner
        return out
    }

    private fun mergeNested(
        disk: Map<String, Map<String, LongArray>>,
        mem: Map<String, Map<String, LongArray>>
    ): Map<String, Map<String, LongArray>> {
        if (mem.isEmpty()) return disk
        val out = HashMap(disk)
        mem.forEach { (bucket, inner) ->
            val merged = HashMap(out[bucket] ?: emptyMap())
            inner.forEach { (cfg, v) ->
                val cur = merged[cfg]
                merged[cfg] = if (cur == null) v else longArrayOf(cur[0] + v[0], cur[1] + v[1])
            }
            out[bucket] = merged
        }
        return out
    }

    private fun trimHourlyCfg(
        map: Map<String, Map<String, LongArray>>
    ): Map<String, Map<String, LongArray>> {
        if (map.size <= HOURLY_RETENTION_HOURS) return map
        val cutoff = LocalDateTime.now().minusHours(HOURLY_RETENTION_HOURS).format(HOUR_FMT)
        return map.filterKeys { it >= cutoff }
    }

    fun configTotalsRange(
        dailyCfg: Map<String, Map<String, LongArray>>,
        hourlyCfg: Map<String, Map<String, LongArray>>,
        bars: List<Bar>,
        hourlyMode: Boolean
    ): List<Pair<String, LongArray>> {
        val src = if (hourlyMode) hourlyCfg else dailyCfg
        val acc = HashMap<String, LongArray>()
        bars.forEach { bar ->
            src[bar.key]?.forEach { (cfg, v) ->
                val cur = acc[cfg] ?: longArrayOf(0L, 0L)
                acc[cfg] = longArrayOf(cur[0] + v[0], cur[1] + v[1])
            }
        }
        return acc.entries
            .map { it.key to it.value }
            .sortedByDescending { it.second[0] + it.second[1] }
    }

    private fun loadNested(key: String): Map<String, Map<String, LongArray>> {
        if (!initialized) return emptyMap()
        val raw = prefs.getString(key, null) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val out = HashMap<String, Map<String, LongArray>>()
            root.keys().forEach { bucket ->
                val innerObj = root.getJSONObject(bucket)
                val inner = HashMap<String, LongArray>()
                innerObj.keys().forEach { cfg ->
                    val arr = innerObj.getJSONArray(cfg)
                    inner[cfg] = longArrayOf(arr.optLong(0), arr.optLong(1))
                }
                out[bucket] = inner
            }
            out as Map<String, Map<String, LongArray>>
        }.getOrDefault(emptyMap())
    }

    private fun persistNested(key: String, map: Map<String, Map<String, LongArray>>) {
        if (!initialized) return
        val root = JSONObject()
        map.forEach { (bucket, inner) ->
            val innerObj = JSONObject()
            inner.forEach { (cfg, v) ->
                innerObj.put(cfg, JSONArray().put(v[0]).put(v[1]))
            }
            root.put(bucket, innerObj)
        }
        prefs.edit().putString(key, root.toString()).apply()
    }

    fun syncDirect(rx: Long, tx: Long, vpnOff: Boolean) {
        if (!initialized) return
        val lastRx = prefs.getLong(KEY_TS_RX, -1L)
        val lastTx = prefs.getLong(KEY_TS_TX, -1L)
        val wasOff = prefs.getBoolean(KEY_TS_OFF, false)
        prefs.edit()
            .putLong(KEY_TS_RX, rx)
            .putLong(KEY_TS_TX, tx)
            .putBoolean(KEY_TS_OFF, vpnOff)
            .apply()
        if (!wasOff || lastRx < 0L || lastTx < 0L) return
        if (rx < lastRx || tx < lastTx) return
        val drx = rx - lastRx
        val dtx = tx - lastTx
        if (drx > 0L || dtx > 0L) add(dtx, drx, DIRECT_KEY)
    }

    fun flush() {
        synchronized(lock) {
            if (initialized) {
                persist(KEY_DAILY, _usage.value)
                persist(KEY_HOURLY, _hourly.value)
                persist(KEY_BY_CONFIG, _byConfig.value)
                persistNested(KEY_DAILY_CFG, _dailyCfg.value)
                persistNested(KEY_HOURLY_CFG, _hourlyCfg.value)
                ticksSincePersist = 0
            }
        }
    }

    data class Bar(val label: String, val short: String, val up: Long, val down: Long, val key: String = "") {
        val total: Long get() = up + down
    }

    fun hourlyBars(map: Map<String, LongArray>, hours: Int): List<Bar> {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (hours - 1 downTo 0).map { back ->
            val slot = now.minusHours(back.toLong())
            val k = slot.format(HOUR_FMT)
            val v = map[k] ?: longArrayOf(0L, 0L)
            val next = (slot.hour + 1) % 24
            Bar(
                label = "%02d:00-%02d:00".format(slot.hour, next),
                short = "%02d".format(slot.hour),
                up = v[0], down = v[1], key = k
            )
        }
    }

    fun hourlyBarsRange(map: Map<String, LongArray>, from: LocalDate, to: LocalDate): List<Bar> {
        val lo = if (from.isAfter(to)) to else from
        val hi = if (from.isAfter(to)) from else to
        var cursor = lo.atStartOfDay()
        val end = hi.atTime(23, 0)
        val out = ArrayList<Bar>()
        while (!cursor.isAfter(end)) {
            val k = cursor.format(HOUR_FMT)
            val v = map[k] ?: longArrayOf(0L, 0L)
            val next = (cursor.hour + 1) % 24
            out.add(Bar(
                label = "%02d:00-%02d:00".format(cursor.hour, next),
                short = "%02d".format(cursor.hour),
                up = v[0], down = v[1], key = k
            ))
            cursor = cursor.plusHours(1)
        }
        return out
    }

    fun hourlyToday(map: Map<String, LongArray>): List<Bar> {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        var cursor = now.toLocalDate().atStartOfDay()   // today 00:00
        val out = ArrayList<Bar>()
        while (!cursor.isAfter(now)) {
            val k = cursor.format(HOUR_FMT)
            val v = map[k] ?: longArrayOf(0L, 0L)
            val next = (cursor.hour + 1) % 24
            out.add(Bar(
                label = "%02d:00-%02d:00".format(cursor.hour, next),
                short = "%02d".format(cursor.hour),
                up = v[0], down = v[1], key = k
            ))
            cursor = cursor.plusHours(1)
        }
        return out
    }

    fun dailyBars(map: Map<String, LongArray>, days: Int): List<Bar> {
        val today = LocalDate.now()
        return (days - 1 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            val v = map[d.toString()] ?: longArrayOf(0L, 0L)
            val lbl = "${d.monthValue}/${d.dayOfMonth}"
            Bar(label = lbl, short = lbl, up = v[0], down = v[1], key = d.toString())
        }
    }

    fun dailyBarsRange(map: Map<String, LongArray>, from: LocalDate, to: LocalDate): List<Bar> {
        val lo = if (from.isAfter(to)) to else from
        val hi = if (from.isAfter(to)) from else to
        var cursor = lo
        val out = ArrayList<Bar>()
        while (!cursor.isAfter(hi)) {
            val v = map[cursor.toString()] ?: longArrayOf(0L, 0L)
            val lbl = "${cursor.monthValue}/${cursor.dayOfMonth}"
            out.add(Bar(label = lbl, short = lbl, up = v[0], down = v[1], key = cursor.toString()))
            cursor = cursor.plusDays(1)
        }
        return out
    }

    fun sum(bars: List<Bar>): LongArray {
        var up = 0L; var down = 0L
        bars.forEach { up += it.up; down += it.down }
        return longArrayOf(up, down)
    }

    fun totalDirect(map: Map<String, LongArray>): LongArray =
        map[DIRECT_KEY]?.let { longArrayOf(it[0], it[1]) } ?: longArrayOf(0L, 0L)

    fun configTotals(map: Map<String, LongArray>): List<Pair<String, LongArray>> =
        map.entries
            .filter { it.key != DIRECT_KEY }
            .map { it.key to it.value }
            .sortedByDescending { it.second[0] + it.second[1] }

    fun totalAll(map: Map<String, LongArray>): LongArray {
        var up = 0L; var down = 0L
        for ((_, v) in map) { up += v[0]; down += v[1] }
        return longArrayOf(up, down)
    }

    private fun trimHourly(map: Map<String, LongArray>): Map<String, LongArray> {
        val cutoff = LocalDateTime.now().minusHours(HOURLY_RETENTION_HOURS)
        return map.filter { (k, _) ->
            val t = runCatching { LocalDateTime.parse(k, HOUR_FMT) }.getOrNull()
            t == null || !t.isBefore(cutoff)
        }
    }

    private fun load(key: String): Map<String, LongArray> {
        val raw = prefs.getString(key, null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            val map = HashMap<String, LongArray>()
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = o.getJSONArray(k)
                map[k] = longArrayOf(arr.getLong(0), arr.getLong(1))
            }
            map
        } catch (e: Exception) { emptyMap() }
    }

    private fun persist(key: String, map: Map<String, LongArray>) {
        val o = JSONObject()
        for ((k, v) in map) o.put(k, JSONArray().put(v[0]).put(v[1]))
        prefs.edit().putString(key, o.toString()).apply()
    }



    private const val KEY_DAILY = "daily_usage"
    private const val KEY_HOURLY = "hourly_usage"
    private const val KEY_BY_CONFIG = "by_config_usage"
    private const val KEY_DAILY_CFG = "daily_cfg_usage"
    private const val KEY_HOURLY_CFG = "hourly_cfg_usage"
    private const val KEY_TS_RX = "ts_total_rx"
    private const val KEY_TS_TX = "ts_total_tx"
    private const val KEY_TS_OFF = "ts_vpn_off"
}