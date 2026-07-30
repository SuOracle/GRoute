package net.cubevpn.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AuthUser(val id: String, val identifier: String, val displayName: String)

data class AccountService(
    val id: String,
    val name: String,
    val subscriptionUrl: String,
    val expire: Long,
    val totalBytes: Long,
    val usedBytes: Long
)

sealed interface AuthResult {
    data class RequestCodeOk(val cooldownSeconds: Int) : AuthResult
    data class VerifyOk(val token: String, val user: AuthUser) : AuthResult
    data class AccountOk(val user: AuthUser, val services: List<AccountService>) : AuthResult
    data class Error(val code: String, val message: String) : AuthResult
}

/**
 * Talks to the CubeVPN account API (see docs/api-contract.md) for OTP login
 * via @cubevvpn_bot and fetching the user's purchased-service subscription links.
 */
object AuthApi {

    private val BASE = BuildConfig.API_BASE_URL.trimEnd('/')

    suspend fun requestCode(identifier: String): AuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject().put("identifier", identifier)
        val res = postJson("/api/requestcode.php", body, token = null)
        if (res == null) return@withContext AuthResult.Error("network", "Network error")
        if (res.optBoolean("ok", false)) {
            AuthResult.RequestCodeOk(res.optInt("cooldown_seconds", 60))
        } else {
            errorFrom(res)
        }
    }

    suspend fun verifyCode(identifier: String, code: String): AuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject().put("identifier", identifier).put("code", code)
        val res = postJson("/api/verifycode.php", body, token = null)
        if (res == null) return@withContext AuthResult.Error("network", "Network error")
        if (res.optBoolean("ok", false)) {
            val token = res.optString("token")
            val u = res.optJSONObject("user")
            if (token.isBlank() || u == null) {
                AuthResult.Error("bad_response", "Malformed server response")
            } else {
                AuthResult.VerifyOk(
                    token,
                    AuthUser(
                        id = u.optString("id"),
                        identifier = u.optString("identifier", identifier),
                        displayName = u.optString("display_name")
                    )
                )
            }
        } else {
            errorFrom(res)
        }
    }

    suspend fun fetchAccount(token: String): AuthResult = withContext(Dispatchers.IO) {
        val res = getJson("/api/accountme.php", token)
        if (res == null) return@withContext AuthResult.Error("network", "Network error")
        if (res.optBoolean("ok", false)) {
            val u = res.optJSONObject("user")
            val user = AuthUser(
                id = u?.optString("id") ?: "",
                identifier = u?.optString("identifier") ?: "",
                displayName = u?.optString("display_name") ?: ""
            )
            val services = mutableListOf<AccountService>()
            val arr = res.optJSONArray("services")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val s = arr.optJSONObject(i) ?: continue
                    services += AccountService(
                        id = s.optString("id"),
                        name = s.optString("name"),
                        subscriptionUrl = s.optString("subscription_url"),
                        expire = s.optLong("expire", 0),
                        totalBytes = s.optLong("total_bytes", 0),
                        usedBytes = s.optLong("used_bytes", 0)
                    )
                }
            }
            AuthResult.AccountOk(user, services)
        } else {
            errorFrom(res)
        }
    }

    suspend fun logout(token: String) = withContext(Dispatchers.IO) {
        runCatching { postJson("/api/logout.php", JSONObject(), token) }
    }

    private fun errorFrom(res: JSONObject): AuthResult.Error =
        AuthResult.Error(res.optString("error", "unknown"), res.optString("message", "Request failed"))

    private fun postJson(path: String, body: JSONObject, token: String?): JSONObject? =
        request("POST", path, body, token)

    private fun getJson(path: String, token: String?): JSONObject? =
        request("GET", path, null, token)

    private fun request(method: String, path: String, body: JSONObject?, token: String?): JSONObject? {
        if (BASE.isBlank()) return null
        return try {
            val conn = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                if (token != null) setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    doOutput = true
                    outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
            }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return null
            conn.disconnect()
            JSONObject(text)
        } catch (e: Exception) {
            null
        }
    }
}
