package com.openmacro.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openmacro.feature.logs.LogsScreen
import com.openmacro.feature.macros.MacrosScreen
import com.openmacro.feature.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable data object MacrosRoute
@Serializable data object LogsRoute
@Serializable data object SettingsRoute

data class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(MacrosRoute, Icons.Default.PlayArrow, "Macros"),
    TopLevelDestination(LogsRoute, Icons.AutoMirrored.Filled.List, "Logs"),
    TopLevelDestination(SettingsRoute, Icons.Default.Settings, "Settings"),
)

@Composable
fun OpenMacroApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(destination.route::class)
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MacrosRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<MacrosRoute> { MacrosScreen() }
            composable<LogsRoute> { LogsScreen() }
            composable<SettingsRoute> { SettingsScreen() }
        }
    }
}
