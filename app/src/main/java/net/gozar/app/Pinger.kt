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