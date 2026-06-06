package com.example.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class ScanResult(
    val ip: String,
    val latencyMs: Long,
    val isAlive: Boolean
)

class NetworkProber {

    /**
     * Pings a list of IPs concurrently on the given port (default 443).
     * Returns a list of ScanResult sorted by latency.
     */
    suspend fun probeIps(ips: List<String>, port: Int = 443, timeoutMs: Int = 1500): List<ScanResult> =
        withContext(Dispatchers.IO) {
            val deferredResults = ips.map { ip ->
                async {
                    probeSingleIp(ip, port, timeoutMs)
                }
            }
            deferredResults.awaitAll().sortedBy { if (it.isAlive) it.latencyMs else Long.MAX_VALUE }
        }

    private fun probeSingleIp(ip: String, port: Int, timeoutMs: Int): ScanResult {
        var socket: Socket? = null
        val startTime = System.currentTimeMillis()
        return try {
            socket = Socket()
            val socketAddress = InetSocketAddress(ip, port)
            socket.connect(socketAddress, timeoutMs)
            val endTime = System.currentTimeMillis()
            ScanResult(ip, endTime - startTime, true)
        } catch (e: SocketTimeoutException) {
            ScanResult(ip, -1, false)
        } catch (e: UnknownHostException) {
            ScanResult(ip, -1, false)
        } catch (e: Exception) {
            ScanResult(ip, -1, false)
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
                // Ignore close exceptions
            }
        }
    }
}
