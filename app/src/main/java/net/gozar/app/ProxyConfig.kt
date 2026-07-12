package net.gozar.app

import org.json.JSONObject
import java.util.UUID

enum class ConfigSource { PERSONAL, COMMUNITY, PREMIUM }

data class ProxyConfig(
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val uuid: String = "",
    val password: String = "",
    val method: String = "",
    val alterId: Int = 0,
    val encryption: String = "none",
    val flow: String = "",
    val network: String = "tcp",
    val security: String = "none",
    val sni: String = "",
    val publicKey: String = "",
    val shortId: String = "",
    val fingerprint: String = "chrome",
    val path: String = "",
    val host: String = "",
    val serviceName: String = "",
    val mode: String = "",
    val alpn: String = "",
    val headerType: String = "",
    val subId: String = "",
    val privateKey: String = "",
    val localAddress: String = "",
    val mtu: Int = 0,
    val reserved: String = "",
    val source: ConfigSource = ConfigSource.PERSONAL,
    val id: String = UUID.randomUUID().toString()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("name", name).put("protocol", protocol)
        .put("address", address).put("port", port).put("uuid", uuid)
        .put("password", password).put("method", method).put("alterId", alterId)
        .put("encryption", encryption).put("flow", flow).put("network", network)
        .put("security", security).put("sni", sni).put("publicKey", publicKey)
        .put("shortId", shortId).put("fingerprint", fingerprint)
        .put("path", path).put("host", host)
        .put("serviceName", serviceName).put("mode", mode).put("alpn", alpn).put("source", source.name)
        .put("headerType", headerType)
        .put("subId", subId)
        .put("privateKey", privateKey).put("localAddress", localAddress)
        .put("mtu", mtu).put("reserved", reserved)

    companion object {
        fun fromJson(o: JSONObject) = ProxyConfig(
            name = o.optString("name"),
            protocol = o.optString("protocol"),
            address = o.optString("address"),
            port = o.optInt("port"),
            uuid = o.optString("uuid", ""),
            password = o.optString("password", ""),
            method = o.optString("method", ""),
            alterId = o.optInt("alterId", 0),
            encryption = o.optString("encryption", "none"),
            flow = o.optString("flow", ""),
            network = o.optString("network", "tcp"),
            security = o.optString("security", "none"),
            sni = o.optString("sni", ""),
            publicKey = o.optString("publicKey", ""),
            shortId = o.optString("shortId", ""),
            fingerprint = o.optString("fingerprint", "chrome"),
            path = o.optString("path", ""),
            host = o.optString("host", ""),
            serviceName = o.optString("serviceName", ""),
            mode = o.optString("mode", ""),
            alpn = o.optString("alpn", ""),
            headerType = o.optString("headerType", ""),
            subId = o.optString("subId", ""),
            privateKey = o.optString("privateKey", ""),
            localAddress = o.optString("localAddress", ""),
            mtu = o.optInt("mtu", 0),
            reserved = o.optString("reserved", ""),
            source = runCatching { ConfigSource.valueOf(o.optString("source", "PERSONAL")) }.getOrDefault(ConfigSource.PERSONAL),
            id = o.optString("id", UUID.randomUUID().toString())
        )
    }
}