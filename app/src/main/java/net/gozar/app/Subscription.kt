package net.gozar.app

import org.json.JSONObject
import java.util.UUID

data class Subscription(
    val name: String,
    val url: String,
    val used: Long = 0,
    val total: Long = 0,
    val expire: Long = 0,
    val lastUpdated: Long = 0,
    val id: String = UUID.randomUUID().toString()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("name", name).put("url", url)
        .put("used", used).put("total", total).put("expire", expire)
        .put("lastUpdated", lastUpdated)

    companion object {
        fun fromJson(o: JSONObject) = Subscription(
            name = o.optString("name"),
            url = o.optString("url"),
            used = o.optLong("used", 0),
            total = o.optLong("total", 0),
            expire = o.optLong("expire", 0),
            lastUpdated = o.optLong("lastUpdated", 0),
            id = o.optString("id", UUID.randomUUID().toString())
        )
    }
}