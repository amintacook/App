package com.example.domain

import com.example.core.NetworkProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Logic representation of Warp / WireGuard integration.
 */
class WarpManager {
    private val prober = NetworkProber()

    // 162.159.192.1 is part of Warp's public range
    private val warpIps = listOf(
        "162.159.192.1", "162.159.193.1", "162.159.195.1",
        "188.114.96.1", "188.114.97.1", "162.159.192.5",
        "162.159.192.8"
    )

    suspend fun findBestWarpEndpoint(): String? = withContext(Dispatchers.IO) {
        val results = prober.probeIps(warpIps, port = 2408, timeoutMs = 1500)
        results.firstOrNull { it.isAlive }?.ip
    }

    fun generateWireGuardConfig(
        privateKey: String,
        address: String,
        endpointIp: String,
        reserved: String? = null // For Warp-in-Warp / double hop
    ): String {
        val reservedLine = if (reserved != null) "Reserved = $reserved" else ""
        return """
            [Interface]
            PrivateKey = $privateKey
            Address = $address
            DNS = 1.1.1.1, 1.0.0.1
            MTU = 1280
            
            [Peer]
            PublicKey = bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo=
            AllowedIPs = 0.0.0.0/0, ::/0
            Endpoint = $endpointIp:2408
            $reservedLine
        """.trimIndent()
    }
}
