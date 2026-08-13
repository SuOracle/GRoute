package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class IpIntel(
    val ip: String,
    val org: String,
    val asn: String,
    val countryCode: String,
    val companyType: String,
    val datacenterName: String,
    val abuserScore: String,
    val isDatacenter: Boolean,
    val isVpn: Boolean,
    val isProxy: Boolean,
    val isTor: Boolean,
    val isAbuser: Boolean,
    val isMobile: Boolean,
    val isSatellite: Boolean,
    val isBogon: Boolean
) {
    val kind: String
        get() = when {
            isBogon -> "Bogon"
            isTor -> "Tor exit node"
            isDatacenter -> if (datacenterName.isNotBlank()) "Datacenter ($datacenterName)" else "Datacenter"
            isSatellite -> "Satellite"
            isMobile -> "Mobile"
            companyType.equals("isp", ignoreCase = true) -> "Residential"
            companyType.equals("business", ignoreCase = true) -> "Business"
            companyType.equals("education", ignoreCase = true) -> "Education"
            companyType.equals("hosting", ignoreCase = true) -> "Datacenter"
            else -> "Unknown"
        }

    val flags: String
        get() = buildList {
            if (isVpn) add("vpn")
            if (isProxy) add("proxy")
            if (isTor) add("tor")
            if (isAbuser) add("abuser")
            if (isMobile) add("mobile")
        }.joinToString(", ")

    private val penalty: Int
        get() {
            var s = 0
            if (isBogon) s += 60
            if (isAbuser) s += 35
            if (isTor) s += 25
            if (isProxy) s += 15
            if (isVpn) s += 15
            if (isDatacenter) s += 10
            val f = abuserScore.substringBefore(" ").trim().toDoubleOrNull() ?: 0.0
            s += (f * 100.0).toInt().coerceAtMost(30)
            return s.coerceIn(0, 100)
        }

    val reputation: Int
        get() = (100 - penalty).coerceIn(0, 100)

    val repBand: String
        get() = when {
            reputation >= 80 -> "Excellent"
            reputation >= 60 -> "Good"
            reputation >= 40 -> "Fair"
            else -> "Poor"
        }

    val flagged: Boolean
        get() = isVpn || isProxy || isTor || isAbuser || isBogon
}

object IpIntelligence {

    suspend fun lookup(ip: String): IpIntel? = withContext(Dispatchers.IO) {
        if (ip.isBlank()) return@withContext null
        val body = runCatching {
            val conn = URL("https://api.ipapi.is/?q=$ip")
                .openConnection(Proxy.NO_PROXY) as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")
            val text = if (conn.responseCode in 200..299)
                conn.inputStream.bufferedReader().use { it.readText() } else null
            runCatching { conn.disconnect() }
            text
        }.getOrNull() ?: return@withContext null

        runCatching {
            val root = JSONObject(body)
            if (root.has("error")) return@withContext null
            val company = root.optJSONObject("company") ?: JSONObject()
            val asn = root.optJSONObject("asn") ?: JSONObject()
            val location = root.optJSONObject("location") ?: JSONObject()
            val datacenter = root.optJSONObject("datacenter") ?: JSONObject()
            val number = if (root.has("asn_num")) root.optInt("asn_num", 0)
            else asn.optInt("asn", 0)
            IpIntel(
                ip = root.optString("ip", ip),
                org = root.optString("company_name", "")
                    .ifBlank { company.optString("name", "") }
                    .ifBlank { root.optString("asn_org", "") }
                    .ifBlank { asn.optString("org", "") },
                asn = if (number > 0) "AS$number" else "",
                countryCode = root.optString("cc", "")
                    .ifBlank { location.optString("country_code", "") }
                    .ifBlank { asn.optString("country", "") }
                    .uppercase(),
                companyType = company.optString("type", "").ifBlank { asn.optString("type", "") },
                datacenterName = datacenter.optString("datacenter", ""),
                abuserScore = company.optString("abuser_score", ""),
                isDatacenter = root.optBoolean("is_datacenter", false),
                isVpn = root.optBoolean("is_vpn", false),
                isProxy = root.optBoolean("is_proxy", false),
                isTor = root.optBoolean("is_tor", false),
                isAbuser = root.optBoolean("is_abuser", false),
                isMobile = root.optBoolean("is_mobile", false),
                isSatellite = root.optBoolean("is_satellite", false),
                isBogon = root.optBoolean("is_bogon", false)
            )
        }.getOrNull()
    }
}