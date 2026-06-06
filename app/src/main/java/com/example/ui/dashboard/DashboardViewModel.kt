package com.example.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ConfigParser
import com.example.core.NetworkProber
import com.example.core.ProxyConfig
import com.example.core.ScanResult
import com.example.vpn.VpnServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val vpnIsActive: Boolean = false,
    val inputUri: String = "",
    val activeConfig: ProxyConfig? = null,
    val isScanning: Boolean = false,
    val scanResults: List<ScanResult> = emptyList(),
    val fragmentLength: String = "10-20,10-20,tlshello"
)

class DashboardViewModel : ViewModel() {

    private val prober = NetworkProber()
    private val parser = ConfigParser()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun updateInput(uri: String) {
        _state.update { it.copy(inputUri = uri) }
    }
    
    fun updateFragmentLength(fragment: String) {
        _state.update { it.copy(fragmentLength = fragment) }
    }

    fun parseConfig() {
        val uriStr = _state.value.inputUri
        val config = parser.parse(uriStr)
        if (config != null) {
            _state.update { it.copy(activeConfig = config) }
        }
    }

    fun startLiveScan() {
        _state.update { it.copy(isScanning = true) }
        viewModelScope.launch {
            // Using placeholder IPs (Standard CDN / Probing targets)
            val ipList = listOf("1.1.1.1", "1.0.0.1", "8.8.8.8", "8.8.4.4", "104.16.248.249")
            val results = prober.probeIps(ipList)
            _state.update { it.copy(scanResults = results, isScanning = false) }
        }
    }

    fun selectIp(ip: String) {
        val config = _state.value.activeConfig ?: return
        val fragment = _state.value.fragmentLength.takeIf { it.isNotBlank() }
        val modifiedUri = parser.mutateConfig(config, newAddress = ip, fragmentLength = fragment)
        _state.update { it.copy(inputUri = modifiedUri) }
        parseConfig()
    }

    fun toggleVpn(context: Context) {
        val isActive = _state.value.vpnIsActive
        
        val intent = Intent(context, VpnServiceManager::class.java)
        if (isActive) {
            intent.action = VpnServiceManager.ACTION_STOP
            context.startService(intent)
            _state.update { it.copy(vpnIsActive = false) }
        } else {
            intent.action = VpnServiceManager.ACTION_START
            context.startService(intent)
            _state.update { it.copy(vpnIsActive = true) }
        }
    }
}
