package net.gozar.app

import android.util.Base64
import java.security.SecureRandom

/**
 * X25519 (Curve25519) scalar multiplication — a faithful port of TweetNaCl's
 * crypto_scalarmult, used to derive WireGuard public keys from a private key.
 *
 * Verified against the RFC 7748 test vectors before shipping:
 *   priv 77076d0a… -> pub 8520f009…   (and the second/"Bob" vector)
 *
 * Only used for key generation; the data path is handled by Xray-core's
 * native WireGuard outbound.
 */
object Curve25519 {

    private fun car25519(o: LongArray) {
        for (i in 0 until 16) {
            o[i] += (1L shl 16)
            val c = o[i] shr 16
            o[(i + 1) * (if (i < 15) 1 else 0)] += c - 1 + 37 * (c - 1) * (if (i == 15) 1L else 0L)
            o[i] -= c shl 16
        }
    }

    private fun sel25519(p: LongArray, q: LongArray, b: Int) {
        val c = (b.toLong() - 1L).inv()
        for (i in 0 until 16) {
            val t = c and (p[i] xor q[i])
            p[i] = p[i] xor t
            q[i] = q[i] xor t
        }
    }

    private fun pack25519(n: LongArray): ByteArray {
        val t = n.copyOf()
        car25519(t); car25519(t); car25519(t)
        for (j in 0 until 2) {
            val m = LongArray(16)
            m[0] = t[0] - 0xffed
            for (i in 1 until 15) {
                m[i] = t[i] - 0xffff - ((m[i - 1] shr 16) and 1)
                m[i - 1] = m[i - 1] and 0xffff
            }
            m[15] = t[15] - 0x7fff - ((m[14] shr 16) and 1)
            val b = ((m[15] shr 16) and 1).toInt()
            m[14] = m[14] and 0xffff
            sel25519(t, m, 1 - b)
        }
        val o = ByteArray(32)
        for (i in 0 until 16) {
            o[2 * i] = (t[i] and 0xff).toByte()
            o[2 * i + 1] = ((t[i] shr 8) and 0xff).toByte()
        }
        return o
    }

    private fun unpack25519(n: ByteArray): LongArray {
        val o = LongArray(16)
        for (i in 0 until 16) {
            o[i] = (n[2 * i].toLong() and 0xff) + ((n[2 * i + 1].toLong() and 0xff) shl 8)
        }
        o[15] = o[15] and 0x7fff
        return o
    }

    private fun add(a: LongArray, b: LongArray): LongArray {
        val o = LongArray(16); for (i in 0 until 16) o[i] = a[i] + b[i]; return o
    }

    private fun sub(a: LongArray, b: LongArray): LongArray {
        val o = LongArray(16); for (i in 0 until 16) o[i] = a[i] - b[i]; return o
    }

    private fun mul(a: LongArray, b: LongArray): LongArray {
        val t = LongArray(31)
        for (i in 0 until 16) for (j in 0 until 16) t[i + j] += a[i] * b[j]
        for (i in 0 until 15) t[i] += 38 * t[i + 16]
        val o = t.copyOf(16)
        car25519(o); car25519(o)
        return o
    }

    private fun sqr(a: LongArray): LongArray = mul(a, a)

    private fun inv25519(i: LongArray): LongArray {
        var c = i.copyOf()
        for (a in 253 downTo 0) {
            c = sqr(c)
            if (a != 2 && a != 4) c = mul(c, i)
        }
        return c
    }

    private val _121665 = LongArray(16).also { it[0] = 0xDB41L; it[1] = 1L }

    private fun scalarmult(n: ByteArray, p: ByteArray): ByteArray {
        val z = n.copyOf(32)
        z[31] = ((z[31].toInt() and 127) or 64).toByte()
        z[0] = (z[0].toInt() and 248).toByte()
        val x = unpack25519(p)
        var a = LongArray(16); var b = x.copyOf(); var c = LongArray(16); var d = LongArray(16)
        a[0] = 1L; d[0] = 1L
        for (i in 254 downTo 0) {
            val r = ((z[i shr 3].toInt() and 0xff) shr (i and 7)) and 1
            sel25519(a, b, r); sel25519(c, d, r)
            var e = add(a, c); a = sub(a, c); c = add(b, d); b = sub(b, d)
            d = sqr(e); val f = sqr(a); a = mul(c, a); c = mul(b, e); e = add(a, c); a = sub(a, c)
            b = sqr(a); c = sub(d, f); a = mul(c, _121665); a = add(a, d); c = mul(c, a); a = mul(d, f); d = mul(b, x); b = sqr(e)
            sel25519(a, b, r); sel25519(c, d, r)
        }
        c = inv25519(c)
        a = mul(a, c)
        return pack25519(a)
    }

    private fun scalarmultBase(n: ByteArray): ByteArray {
        val nine = ByteArray(32); nine[0] = 9
        return scalarmult(n, nine)
    }

    fun generateKeyPair(): Pair<String, String> {
        val priv = ByteArray(32)
        SecureRandom().nextBytes(priv)
        priv[0] = (priv[0].toInt() and 248).toByte()
        priv[31] = ((priv[31].toInt() and 127) or 64).toByte()
        val pub = scalarmultBase(priv)
        return Pair(
            Base64.encodeToString(priv, Base64.NO_WRAP),
            Base64.encodeToString(pub, Base64.NO_WRAP)
        )
    }
}