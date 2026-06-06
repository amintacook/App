package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Route("dashboard", "Dashboard", Icons.Filled.Home)
    data object Configs : Route("configs", "Configs", Icons.Filled.List)
    data object Subscriptions : Route("subs", "Subs", Icons.Filled.Info)
    data object Settings : Route("settings", "Settings", Icons.Filled.Settings)
}
