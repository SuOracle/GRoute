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

    fun init(context: Context) {
        synchronized(lock) {
            if (initialized) return
            prefs = context.applicationContext.getSharedPreferences("gozarnet_usage", Context.MODE_PRIVATE)
            initialized = true
        }
        Thread({
            val daily = load(KEY_DAILY)
            val hourly = load(KEY_HOURLY)
            synchronized(lock) {
                _usage.value = mergeCounts(daily, _usage.value)
                _hourly.value = trimHourly(mergeCounts(hourly, _hourly.value))
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
    fun add(up: Long, down: Long) {
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

            if (++ticksSincePersist >= 5) {
                persist(KEY_DAILY, _usage.value)
                persist(KEY_HOURLY, _hourly.value)
                ticksSincePersist = 0
            }
        }
    }

    fun flush() {
        synchronized(lock) {
            if (initialized) {
                persist(KEY_DAILY, _usage.value)
                persist(KEY_HOURLY, _hourly.value)
                ticksSincePersist = 0
            }
        }
    }

    data class Bar(val label: String, val short: String, val up: Long, val down: Long) {
        val total: Long get() = up + down
    }

    fun hourlyBars(map: Map<String, LongArray>, hours: Int): List<Bar> {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (hours - 1 downTo 0).map { back ->
            val slot = now.minusHours(back.toLong())
            val v = map[slot.format(HOUR_FMT)] ?: longArrayOf(0L, 0L)
            val next = (slot.hour + 1) % 24
            Bar(
                label = "%02d:00-%02d:00".format(slot.hour, next),
                short = "%02d".format(slot.hour),
                up = v[0], down = v[1]
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
            val v = map[cursor.format(HOUR_FMT)] ?: longArrayOf(0L, 0L)
            val next = (cursor.hour + 1) % 24
            out.add(Bar(
                label = "%02d:00-%02d:00".format(cursor.hour, next),
                short = "%02d".format(cursor.hour),
                up = v[0], down = v[1]
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
            val v = map[cursor.format(HOUR_FMT)] ?: longArrayOf(0L, 0L)
            val next = (cursor.hour + 1) % 24
            out.add(Bar(
                label = "%02d:00-%02d:00".format(cursor.hour, next),
                short = "%02d".format(cursor.hour),
                up = v[0], down = v[1]
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
            Bar(label = lbl, short = lbl, up = v[0], down = v[1])
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
            out.add(Bar(label = lbl, short = lbl, up = v[0], down = v[1]))
            cursor = cursor.plusDays(1)
        }
        return out
    }

    fun sum(bars: List<Bar>): LongArray {
        var up = 0L; var down = 0L
        bars.forEach { up += it.up; down += it.down }
        return longArrayOf(up, down)
    }

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
}