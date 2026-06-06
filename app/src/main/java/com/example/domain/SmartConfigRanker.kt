package com.example.domain

import com.example.core.NetworkProber
import com.example.data.local.ConfigDao
import com.example.data.local.ProxyProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class SmartConfigRanker(
    private val configDao: ConfigDao
) {
    private val prober = NetworkProber()

    /**
     * Probes all given configs, updates their latency and health scores in the DB,
     * and returns the best config (lowest latency).
     */
    suspend fun rankConfigs(configs: List<ProxyProfile>): ProxyProfile? = withContext(Dispatchers.IO) {
        if (configs.isEmpty()) return@withContext null

        val deferredResults = configs.map { config ->
            async {
                val ipToProbe = config.address
                val results = prober.probeIps(listOf(ipToProbe), config.port, 2000)
                val result = results.firstOrNull()
                
                val latency = if (result?.isAlive == true) result.latencyMs else -1L
                val health = calculateHealthScore(latency, config.protocol)
                
                // Update in DB
                configDao.updateLatencyAndHealth(config.id, latency, health)
                
                config.copy(lastLatencyMs = latency, healthScore = health)
            }
        }

        val updatedConfigs = deferredResults.awaitAll()
        
        // Return the one with the highest health and lowest latency
        updatedConfigs
            .filter { it.lastLatencyMs != -1L }
            .minByOrNull { it.lastLatencyMs }
    }

    private fun calculateHealthScore(latencyMs: Long, protocol: String): Int {
        if (latencyMs == -1L) return 0
        var score = 100
        
        // Deduct based on latency
        if (latencyMs > 1000) score -= 40
        else if (latencyMs > 500) score -= 20
        else if (latencyMs > 200) score -= 10
        
        // Tiny modifier based on advanced protocols typically being stronger against DPI
        when (protocol.lowercase()) {
            "vless" -> score += 5
            "trojan" -> score += 5
        }

        return score.coerceIn(0, 100)
    }
}
