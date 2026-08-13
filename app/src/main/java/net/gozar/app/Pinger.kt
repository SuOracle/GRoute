package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

sealed interface PingResult {
    data object Testing : PingResult
    data class Ok(val ms: Int) : PingResult
    data object Failed : PingResult
}

object Pinger {
    suspend fun pingIke(address: String, timeoutMs: Int = 3000): PingResult =
        withContext(Dispatchers.IO) {
            var socket: java.net.DatagramSocket? = null
            try {
                val target = java.net.InetAddress.getByName(address)
                val spi = ByteArray(8)
                java.security.SecureRandom().nextBytes(spi)
                val packet = ikeSaInit(spi)
                val s = java.net.DatagramSocket()
                s.soTimeout = timeoutMs
                socket = s
                val start = System.currentTimeMillis()
                s.send(java.net.DatagramPacket(packet, packet.size, target, 500))
                val buf = ByteArray(1024)
                val reply = java.net.DatagramPacket(buf, buf.size)
                s.receive(reply)
                if (reply.length >= 28 && buf.copyOfRange(0, 8).contentEquals(spi))
                    PingResult.Ok((System.currentTimeMillis() - start).toInt())
                else PingResult.Failed
            } catch (e: Exception) {
                PingResult.Failed
            } finally {
                runCatching { socket?.close() }
            }
        }

    private fun ikeSaInit(spi: ByteArray): ByteArray {
        val rnd = java.security.SecureRandom()

        val transforms = java.io.ByteArrayOutputStream()
        fun transform(last: Boolean, type: Int, id: Int, keyLen: Int?) {
            val attr = if (keyLen == null) ByteArray(0)
            else byteArrayOf(0x80.toByte(), 0x0E, (keyLen shr 8).toByte(), keyLen.toByte())
            val len = 8 + attr.size
            transforms.write(if (last) 0 else 3)
            transforms.write(0)
            transforms.write(len shr 8)
            transforms.write(len and 0xFF)
            transforms.write(type)
            transforms.write(0)
            transforms.write(id shr 8)
            transforms.write(id and 0xFF)
            transforms.write(attr)
        }
        transform(false, 1, 20, 128)
        transform(false, 2, 5, null)
        transform(false, 4, 19, null)
        transform(true, 4, 14, null)
        val tf = transforms.toByteArray()

        val proposalLen = 8 + tf.size
        val proposal = java.io.ByteArrayOutputStream()
        proposal.write(0)
        proposal.write(0)
        proposal.write(proposalLen shr 8)
        proposal.write(proposalLen and 0xFF)
        proposal.write(1)
        proposal.write(1)
        proposal.write(0)
        proposal.write(4)
        proposal.write(tf)
        val pr = proposal.toByteArray()

        val saLen = 4 + pr.size
        val sa = java.io.ByteArrayOutputStream()
        sa.write(34)
        sa.write(0)
        sa.write(saLen shr 8)
        sa.write(saLen and 0xFF)
        sa.write(pr)

        val keyData = ByteArray(256)
        rnd.nextBytes(keyData)
        val keLen = 8 + keyData.size
        val ke = java.io.ByteArrayOutputStream()
        ke.write(40)
        ke.write(0)
        ke.write(keLen shr 8)
        ke.write(keLen and 0xFF)
        ke.write(0)
        ke.write(14)
        ke.write(0)
        ke.write(0)
        ke.write(keyData)

        val nonceData = ByteArray(32)
        rnd.nextBytes(nonceData)
        val nLen = 4 + nonceData.size
        val nonce = java.io.ByteArrayOutputStream()
        nonce.write(0)
        nonce.write(0)
        nonce.write(nLen shr 8)
        nonce.write(nLen and 0xFF)
        nonce.write(nonceData)

        val body = sa.toByteArray() + ke.toByteArray() + nonce.toByteArray()
        val total = 28 + body.size
        val out = java.io.ByteArrayOutputStream()
        out.write(spi)
        out.write(ByteArray(8))
        out.write(33)
        out.write(0x20)
        out.write(34)
        out.write(0x08)
        out.write(ByteArray(4))
        out.write(total ushr 24)
        out.write((total shr 16) and 0xFF)
        out.write((total shr 8) and 0xFF)
        out.write(total and 0xFF)
        out.write(body)
        return out.toByteArray()
    }

    suspend fun ping(address: String, port: Int, timeoutMs: Int = 3000): PingResult =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    val start = System.currentTimeMillis()
                    socket.connect(InetSocketAddress(address, port), timeoutMs)
                    PingResult.Ok((System.currentTimeMillis() - start).toInt())
                }
            } catch (e: Exception) {
                PingResult.Failed
            }
        }
}