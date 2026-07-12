package net.gozar.app

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object ConfigFile {

    const val EXTENSION = "grt"
    const val MIME = "application/octet-stream"

    private const val MAGIC = "GRT1"
    private const val TRANSFORM = "AES/GCM/NoPadding"
    private const val KEY_BITS = 256
    private const val IV_LEN = 12
    private const val TAG_BITS = 128
    private const val SALT_LEN = 16
    private const val PBKDF2_ITERS = 210_000

    private const val FLAG_PW = 0x01
    private const val FLAG_CERT = 0x02

    private val P1 = intArrayOf(0x2A, 0x3F, 0x02, 0x18, 0x19, 0x08, 0x40, 0x0A)
    private val P2 = intArrayOf(0x1F, 0x19, 0x40, 0x1B, 0x5C, 0x40, 0x06, 0x08)
    private val P3 = intArrayOf(0x14, 0x40, 0x09, 0x02, 0x00, 0x0C, 0x04, 0x03)
    private val P4 = intArrayOf(0x40, 0x1E, 0x08, 0x15, 0x0C, 0x1F, 0x0C, 0x19)
    private const val MASK = 0x6D

    private val staticSecret: String by lazy {
        val sb = StringBuilder(32)
        listOf(P1, P2, P3, P4).forEach { part ->
            part.forEach { sb.append((it xor MASK).toChar()) }
        }
        sb.toString()
    }

    @Volatile private var certHash: String? = null

    class WrongPassword : Exception()
    class NeedsPassword : Exception()
    class BadFile : Exception()
    class ForeignApp : Exception()

    fun isPasswordProtected(bytes: ByteArray): Boolean {
        if (bytes.size < 5) throw BadFile()
        if (String(bytes, 0, 4, Charsets.US_ASCII) != MAGIC) throw BadFile()
        return (bytes[4].toInt() and FLAG_PW) != 0
    }

    fun encode(context: Context, configs: List<ProxyConfig>, password: String?): ByteArray {
        val arr = JSONArray()
        configs.forEach { arr.put(it.toJson()) }
        val plain = JSONObject().put("v", 1).put("configs", arr).toString().toByteArray(Charsets.UTF_8)

        val rnd = SecureRandom()
        val salt = ByteArray(SALT_LEN).also { rnd.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { rnd.nextBytes(it) }
        val hasPw = !password.isNullOrEmpty()

        val cert = signingHash(context)
        val key = deriveKey(password, salt, cert)

        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val ct = cipher.doFinal(plain)

        var flags = 0
        if (hasPw) flags = flags or FLAG_PW
        if (cert != null) flags = flags or FLAG_CERT

        val header = ByteArray(5)
        MAGIC.toByteArray(Charsets.US_ASCII).copyInto(header, 0)
        header[4] = flags.toByte()

        return header + salt + iv + ct
    }

    fun decode(context: Context, bytes: ByteArray, password: String?): List<ProxyConfig> {
        if (bytes.size < 5 + SALT_LEN + IV_LEN + 16) throw BadFile()
        if (String(bytes, 0, 4, Charsets.US_ASCII) != MAGIC) throw BadFile()

        val flags = bytes[4].toInt()
        val hasPw = (flags and FLAG_PW) != 0
        val hasCert = (flags and FLAG_CERT) != 0
        if (hasPw && password.isNullOrEmpty()) throw NeedsPassword()

        val cert = if (hasCert) (signingHash(context) ?: throw ForeignApp()) else null

        var off = 5
        val salt = bytes.copyOfRange(off, off + SALT_LEN); off += SALT_LEN
        val iv = bytes.copyOfRange(off, off + IV_LEN); off += IV_LEN
        val ct = bytes.copyOfRange(off, bytes.size)

        val key = deriveKey(if (hasPw) password else null, salt, cert)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val plain = try {
            cipher.doFinal(ct)
        } catch (e: Exception) {
            if (hasPw) throw WrongPassword() else throw BadFile()
        }

        val root = try {
            JSONObject(String(plain, Charsets.UTF_8))
        } catch (e: Exception) {
            throw BadFile()
        }
        val arr = root.optJSONArray("configs") ?: throw BadFile()
        return (0 until arr.length()).map { i ->
            ProxyConfig.fromJson(arr.getJSONObject(i)).copy(
                id = java.util.UUID.randomUUID().toString(),
                subId = "",
                locked = true,
                source = ConfigSource.COMMUNITY
            )
        }
    }

    private fun deriveKey(password: String?, salt: ByteArray, cert: String?): SecretKeySpec {
        val material = (password ?: "") + "\u0000" + staticSecret + "\u0000" + (cert ?: "")
        val spec = PBEKeySpec(material.toCharArray(), salt, PBKDF2_ITERS, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun signingHash(context: Context): String? {
        certHash?.let { return it }
        val sigs: Array<Signature>? = runCatching {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= 28) {
                val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            }
        }.getOrNull()

        if (sigs.isNullOrEmpty()) return null
        val md = MessageDigest.getInstance("SHA-256")
        sigs.map { it.toByteArray() }
            .sortedWith(compareBy({ it.size }, { it.joinToString("") { b -> b.toString() } }))
            .forEach { md.update(it) }
        val h = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        certHash = h
        return h
    }

    fun writeToCache(context: Context, fileName: String, data: ByteArray): File {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val safe = sanitize(fileName)
        val name = if (safe.endsWith(".$EXTENSION")) safe else "$safe.$EXTENSION"
        val out = File(dir, name)
        out.writeBytes(data)
        return out
    }

    private fun sanitize(name: String): String {
        val cleaned = name.trim().replace(Regex("[^\\p{L}\\p{N} ._-]"), "_")
        return cleaned.ifBlank { "configs" }.take(60)
    }
}