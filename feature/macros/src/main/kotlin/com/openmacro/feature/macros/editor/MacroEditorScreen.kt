package com.openmacro.feature.macros.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openmacro.core.model.ActionType
import com.openmacro.core.model.TriggerType
import com.openmacro.core.ui.components.ConfigCard
import com.openmacro.feature.macros.editor.action.DisplayNotificationConfigEditor
import com.openmacro.feature.macros.editor.action.LaunchApplicationConfigEditor
import com.openmacro.feature.macros.editor.action.SetVolumeConfigEditor
import com.openmacro.feature.macros.editor.action.VibrateConfigEditor
import com.openmacro.feature.macros.editor.action.WaitConfigEditor
import com.openmacro.feature.macros.editor.trigger.AppLaunchConfigEditor
import com.openmacro.feature.macros.editor.trigger.BatteryLevelConfigEditor
import com.openmacro.feature.macros.editor.trigger.DayTimeConfigEditor
import com.openmacro.feature.macros.editor.trigger.PowerConnectedConfigEditor
import com.openmacro.feature.macros.editor.trigger.ScreenOnOffConfigEditor

// M2-implemented types only
private val AVAILABLE_TRIGGERS = listOf(
    TypeItem(TriggerType.SCREEN_ON_OFF.typeId, TriggerType.SCREEN_ON_OFF.displayName, Icons.Default.PhoneAndroid),
    TypeItem(TriggerType.BATTERY_LEVEL.typeId, TriggerType.BATTERY_LEVEL.displayName, Icons.Default.BatteryChargingFull),
    TypeItem(TriggerType.POWER_CONNECTED.typeId, TriggerType.POWER_CONNECTED.displayName, Icons.Default.PowerSettingsNew),
    TypeItem(TriggerType.DAY_TIME.typeId, TriggerType.DAY_TIME.displayName, Icons.Default.Schedule),
    TypeItem(TriggerType.APP_LAUNCH.typeId, TriggerType.APP_LAUNCH.displayName, Icons.Default.RocketLaunch),
)

private val AVAILABLE_ACTIONS = listOf(
    TypeItem(ActionType.DISPLAY_NOTIFICATION.typeId, ActionType.DISPLAY_NOTIFICATION.displayName, Icons.Default.Notifications),
    TypeItem(ActionType.LAUNCH_APPLICATION.typeId, ActionType.LAUNCH_APPLICATION.displayName, Icons.Default.PlayArrow),
    TypeItem(ActionType.SET_VOLUME.typeId, ActionType.SET_VOLUME.displayName, Icons.Default.MusicNote),
    TypeItem(ActionType.VIBRATE.typeId, ActionType.VIBRATE.displayName, Icons.Default.Vibration),
    TypeItem(ActionType.WAIT.typeId, ActionType.WAIT.displayName, Icons.Default.Timer),
)

private fun triggerIcon(typeId: String): ImageVector =
    AVAILABLE_TRIGGERS.firstOrNull { it.id == typeId }?.icon ?: Icons.Default.FlashOn

private fun actionIcon(typeId: String): ImageVector =
    AVAILABLE_ACTIONS.firstOrNull { it.id == typeId }?.icon ?: Icons.Default.FlashOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: MacroEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTriggerPicker by remember { mutableStateOf(false) }
    var showActionPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.macroId != null) "Edit Macro" else "New Macro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.name.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Name
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::setName,
                    label = { Text("Macro name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Triggers section
            item {
                SectionHeader("Triggers", uiState.triggers.size)
            }
            itemsIndexed(uiState.triggers) { index, trigger ->
                ConfigCard(
                    title = trigger.type?.displayName ?: trigger.typeId,
                    icon = triggerIcon(trigger.typeId),
                    onRemove = { viewModel.removeTrigger(index) },
                ) {
                    TriggerConfigContent(
                        typeId = trigger.typeId,
                        configJson = trigger.configJson,
                        onConfigChanged = { viewModel.updateTriggerConfig(index, it) },
                    )
                }
            }
            item {
                AddButton("Add Trigger") { showTriggerPicker = true }
            }

            // Actions section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Actions", uiState.actions.size)
            }
            itemsIndexed(uiState.actions) { index, action ->
                ConfigCard(
                    title = action.type?.displayName ?: action.typeId,
                    icon = actionIcon(action.typeId),
                    onRemove = { viewModel.removeAction(index) },
                ) {
                    ActionConfigContent(
                        typeId = action.typeId,
                        configJson = action.configJson,
                        onConfigChanged = { viewModel.updateActionConfig(index, it) },
                    )
                }
            }
            item {
                AddButton("Add Action") { showActionPicker = true }
            }

            // Constraints section (stub for M4)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Constraints", uiState.constraints.size)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Constraints will be available in a future update",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Bottom sheets
    if (showTriggerPicker) {
        TypePickerSheet(
            title = "Add Trigger",
            types = AVAILABLE_TRIGGERS,
            onTypeSelected = { typeId ->
                showTriggerPicker = false
                TriggerType.fromTypeId(typeId)?.let { viewModel.addTrigger(it) }
            },
            onDismiss = { showTriggerPicker = false },
        )
    }

    if (showActionPicker) {
        TypePickerSheet(
            title = "Add Action",
            types = AVAILABLE_ACTIONS,
            onTypeSelected = { typeId ->
                showActionPicker = false
                ActionType.fromTypeId(typeId)?.let { viewModel.addAction(it) }
            },
            onDismiss = { showActionPicker = false },
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        if (count > 0) {
            Text(
                text = " ($count)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text(text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun TriggerConfigContent(
    typeId: String,
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    when (typeId) {
        TriggerType.SCREEN_ON_OFF.typeId -> ScreenOnOffConfigEditor(configJson, onConfigChanged)
        TriggerType.BATTERY_LEVEL.typeId -> BatteryLevelConfigEditor(configJson, onConfigChanged)
        TriggerType.POWER_CONNECTED.typeId -> PowerConnectedConfigEditor(configJson, onConfigChanged)
        TriggerType.DAY_TIME.typeId -> DayTimeConfigEditor(configJson, onConfigChanged)
        TriggerType.APP_LAUNCH.typeId -> AppLaunchConfigEditor(configJson, onConfigChanged)
        else -> Text("Unknown trigger type: $typeId", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ActionConfigContent(
    typeId: String,
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    when (typeId) {
        ActionType.DISPLAY_NOTIFICATION.typeId -> DisplayNotificationConfigEditor(configJson, onConfigChanged)
        ActionType.LAUNCH_APPLICATION.typeId -> LaunchApplicationConfigEditor(configJson, onConfigChanged)
        ActionType.SET_VOLUME.typeId -> SetVolumeConfigEditor(configJson, onConfigChanged)
        ActionType.VIBRATE.typeId -> VibrateConfigEditor(configJson, onConfigChanged)
        ActionType.WAIT.typeId -> WaitConfigEditor(configJson, onConfigChanged)
        else -> Text("Unknown action type: $typeId", style = MaterialTheme.typography.bodySmall)
    }
}
