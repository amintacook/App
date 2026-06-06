package com.example.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.pow
import kotlin.math.sqrt

data class SpeedTestResult(
    val downloadSpeedMbps: Double,
    val pingMs: Double,
    val jitterMs: Double
)

class SpeedTracker {
    private val client = OkHttpClient()

    suspend fun performSpeedTest(testUrl: String = "http://speedtest.tele2.net/1MB.zip"): SpeedTestResult = withContext(Dispatchers.IO) {
        // 1. Measure Ping & Jitter
        val pings = mutableListOf<Long>()
        val address = "1.1.1.1" // Cloudflare for ping baseline
        for (i in 0..4) {
            val start = System.currentTimeMillis()
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.connect(InetSocketAddress(address, 443), 2000)
                pings.add(System.currentTimeMillis() - start)
            } catch (e: Exception) {
            } finally {
                socket?.close()
            }
            delay(100)
        }

        val avgPing = if (pings.isNotEmpty()) pings.average() else 0.0
        val jitter = if (pings.size > 1) {
            val variance = pings.map { (it - avgPing).pow(2) }.average()
            sqrt(variance)
        } else 0.0

        // 2. Measure Download Speed
        var speedMbps = 0.0
        try {
            val startDl = System.currentTimeMillis()
            val request = Request.Builder().url(testUrl).build()
            val response = client.newCall(request).execute()
            val bytes = response.body?.bytes()?.size ?: 0
            val dlTimeMs = System.currentTimeMillis() - startDl
            
            if (dlTimeMs > 0 && bytes > 0) {
                // bytes to megabits
                val megabits = (bytes * 8) / 1_000_000.0
                speedMbps = megabits / (dlTimeMs / 1000.0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        SpeedTestResult(
            downloadSpeedMbps = speedMbps,
            pingMs = avgPing,
            jitterMs = jitter
        )
    }
}
