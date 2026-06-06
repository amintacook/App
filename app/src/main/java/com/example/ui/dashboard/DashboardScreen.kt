package com.example.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.ScanResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexRay Shield", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.toggleVpn(context) },
                containerColor = if (state.vpnIsActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (state.vpnIsActive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(if (state.vpnIsActive) Icons.Filled.Warning else Icons.Filled.PlayArrow, contentDescription = "VPN")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.vpnIsActive) "Stop VPN" else "Start VPN", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header / Status
            StatusCard(state.vpnIsActive)

            // Input section
            OutlinedTextField(
                value = state.inputUri,
                onValueChange = { viewModel.updateInput(it); viewModel.parseConfig() },
                label = { Text("Paste Vmess / Vless / Trojan URI") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            // Current Config Summary
            state.activeConfig?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Parsed Configuration", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Protocol: ${it.protocol.uppercase()}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Destination: ${it.address}:${it.port}", color = MaterialTheme.colorScheme.onSurface)
                        Text("SNI: ${it.sni}", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            // Fragment configure
            OutlinedTextField(
                value = state.fragmentLength,
                onValueChange = { viewModel.updateFragmentLength(it) },
                label = { Text("Fragment Params (e.g. 10-20,10-20,tlshello)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Scan Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Native TCP Prober", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Button(onClick = { viewModel.startLiveScan() }, enabled = !state.isScanning) {
                    if (state.isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp).padding(end = 8.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Text("Scanning...")
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Live Scan")
                    }
                }
            }

            // Results List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(state.scanResults) { result ->
                    ScanResultItem(result = result) {
                        viewModel.selectIp(result.ip)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                }
            }
        }
    }
}

@Composable
fun StatusCard(isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isActive) "Shield Active" else "Shield Disconnected",
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isActive) "Routing all traffic via Xray Engine" else "Ready to connect",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ScanResultItem(result: ScanResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(result.ip, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(if (result.isAlive) "Status: Alive" else "Status: Dead", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (result.isAlive) {
                Text("${result.latencyMs} ms", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                Text("Timeout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}
