package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.configs.ConfigsScreen
import com.example.ui.subscriptions.SubscriptionsScreen
import com.example.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val items = listOf(Route.Dashboard, Route.Configs, Route.Subscriptions, Route.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { route ->
                    NavigationBarItem(
                        icon = { Icon(route.icon, contentDescription = route.title) },
                        label = { Text(route.title) },
                        selected = navBackStackEntry?.destination?.route == route.route,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Dashboard.route) { DashboardScreen() }
            composable(Route.Configs.route) { ConfigsScreen() }
            composable(Route.Subscriptions.route) { SubscriptionsScreen() }
            composable(Route.Settings.route) { SettingsScreen() }
        }
    }
}
