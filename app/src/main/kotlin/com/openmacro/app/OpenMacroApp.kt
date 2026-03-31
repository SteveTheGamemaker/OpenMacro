package com.openmacro.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
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
import com.openmacro.feature.macros.editor.MacroEditorScreen
import com.openmacro.feature.actionblocks.ActionBlockEditorScreen
import com.openmacro.feature.actionblocks.ActionBlockListScreen
import com.openmacro.feature.settings.SettingsScreen
import com.openmacro.feature.variables.VariableManagerScreen
import kotlinx.serialization.Serializable

@Serializable data object MacrosRoute
@Serializable data class MacroEditorRoute(val macroId: Long = -1L)
@Serializable data object LogsRoute
@Serializable data object VariablesRoute
@Serializable data object ActionBlocksRoute
@Serializable data class ActionBlockEditorRoute(val blockId: Long = -1L)
@Serializable data object SettingsRoute

data class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(MacrosRoute, Icons.Default.PlayArrow, "Macros"),
    TopLevelDestination(LogsRoute, Icons.AutoMirrored.Filled.List, "Logs"),
    TopLevelDestination(VariablesRoute, Icons.Default.Code, "Variables"),
    TopLevelDestination(ActionBlocksRoute, Icons.Default.Functions, "Blocks"),
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
            composable<MacrosRoute> {
                MacrosScreen(
                    onCreateMacro = { navController.navigate(MacroEditorRoute()) },
                    onEditMacro = { macroId -> navController.navigate(MacroEditorRoute(macroId)) },
                )
            }
            composable<MacroEditorRoute> {
                MacroEditorScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<LogsRoute> { LogsScreen() }
            composable<VariablesRoute> { VariableManagerScreen() }
            composable<ActionBlocksRoute> {
                ActionBlockListScreen(
                    onCreateBlock = { navController.navigate(ActionBlockEditorRoute()) },
                    onEditBlock = { blockId -> navController.navigate(ActionBlockEditorRoute(blockId)) },
                )
            }
            composable<ActionBlockEditorRoute> {
                ActionBlockEditorScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<SettingsRoute> { SettingsScreen() }
        }
    }
}
