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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.openmacro.core.model.ConstraintType
import com.openmacro.core.model.LogicOperator
import com.openmacro.core.model.TriggerType
import com.openmacro.core.ui.components.ConfigCard
import com.openmacro.feature.macros.editor.action.AirplaneModeConfigEditor
import com.openmacro.feature.macros.editor.action.BluetoothConfigureConfigEditor
import com.openmacro.feature.macros.editor.action.DisplayNotificationConfigEditor
import com.openmacro.feature.macros.editor.action.FillClipboardConfigEditor
import com.openmacro.feature.macros.editor.action.HttpRequestConfigEditor
import com.openmacro.feature.macros.editor.action.LaunchApplicationConfigEditor
import com.openmacro.feature.macros.editor.action.MakeCallConfigEditor
import com.openmacro.feature.macros.editor.action.OpenWebsiteConfigEditor
import com.openmacro.feature.macros.editor.action.SendSmsConfigEditor
import com.openmacro.feature.macros.editor.action.SetVariableConfigEditor
import com.openmacro.feature.macros.editor.action.DeleteVariableConfigEditor
import com.openmacro.feature.macros.editor.action.SetVolumeConfigEditor
import com.openmacro.feature.macros.editor.action.SpeakTextConfigEditor
import com.openmacro.feature.macros.editor.action.VibrateConfigEditor
import com.openmacro.feature.macros.editor.action.WaitConfigEditor
import com.openmacro.feature.macros.editor.action.WifiConfigureConfigEditor
import com.openmacro.feature.macros.editor.constraint.AirplaneModeConstraintEditor
import com.openmacro.feature.macros.editor.constraint.AppRunningConstraintEditor
import com.openmacro.feature.macros.editor.constraint.BatteryLevelConstraintEditor
import com.openmacro.feature.macros.editor.constraint.BluetoothConnectedConstraintEditor
import com.openmacro.feature.macros.editor.constraint.CallStateConstraintEditor
import com.openmacro.feature.macros.editor.constraint.DayOfWeekConstraintEditor
import com.openmacro.feature.macros.editor.constraint.PowerConnectedConstraintEditor
import com.openmacro.feature.macros.editor.constraint.ScreenStateConstraintEditor
import com.openmacro.feature.macros.editor.constraint.TimeOfDayConstraintEditor
import com.openmacro.feature.macros.editor.constraint.VariableValueConstraintEditor
import com.openmacro.feature.macros.editor.constraint.WifiConnectedConstraintEditor
import com.openmacro.feature.macros.editor.constraint.WifiEnabledConstraintEditor
import com.openmacro.feature.macros.editor.trigger.AirplaneModeChangedConfigEditor
import com.openmacro.feature.macros.editor.trigger.AppLaunchConfigEditor
import com.openmacro.feature.macros.editor.trigger.BatteryLevelConfigEditor
import com.openmacro.feature.macros.editor.trigger.BluetoothEventConfigEditor
import com.openmacro.feature.macros.editor.trigger.CallEndedConfigEditor
import com.openmacro.feature.macros.editor.trigger.CallIncomingConfigEditor
import com.openmacro.feature.macros.editor.trigger.CallMissedConfigEditor
import com.openmacro.feature.macros.editor.trigger.DataConnectivityChangeConfigEditor
import com.openmacro.feature.macros.editor.trigger.DayTimeConfigEditor
import com.openmacro.feature.macros.editor.trigger.PowerConnectedConfigEditor
import com.openmacro.feature.macros.editor.trigger.RegularIntervalConfigEditor
import com.openmacro.feature.macros.editor.trigger.ScreenOnOffConfigEditor
import com.openmacro.feature.macros.editor.trigger.SmsReceivedConfigEditor
import com.openmacro.feature.macros.editor.trigger.WifiSsidTransitionConfigEditor
import com.openmacro.feature.macros.editor.trigger.WifiStateChangeConfigEditor

// M2 + M5 triggers
private val AVAILABLE_TRIGGERS = listOf(
    TypeItem(TriggerType.SCREEN_ON_OFF.typeId, TriggerType.SCREEN_ON_OFF.displayName, Icons.Default.PhoneAndroid),
    TypeItem(TriggerType.BATTERY_LEVEL.typeId, TriggerType.BATTERY_LEVEL.displayName, Icons.Default.BatteryChargingFull),
    TypeItem(TriggerType.POWER_CONNECTED.typeId, TriggerType.POWER_CONNECTED.displayName, Icons.Default.PowerSettingsNew),
    TypeItem(TriggerType.DAY_TIME.typeId, TriggerType.DAY_TIME.displayName, Icons.Default.Schedule),
    TypeItem(TriggerType.APP_LAUNCH.typeId, TriggerType.APP_LAUNCH.displayName, Icons.Default.RocketLaunch),
    TypeItem(TriggerType.WIFI_STATE_CHANGE.typeId, TriggerType.WIFI_STATE_CHANGE.displayName, Icons.Default.Wifi),
    TypeItem(TriggerType.WIFI_SSID_TRANSITION.typeId, TriggerType.WIFI_SSID_TRANSITION.displayName, Icons.Default.WifiFind),
    TypeItem(TriggerType.BLUETOOTH_EVENT.typeId, TriggerType.BLUETOOTH_EVENT.displayName, Icons.Default.Bluetooth),
    TypeItem(TriggerType.DATA_CONNECTIVITY_CHANGE.typeId, TriggerType.DATA_CONNECTIVITY_CHANGE.displayName, Icons.Default.NetworkCell),
    TypeItem(TriggerType.AIRPLANE_MODE_CHANGED.typeId, TriggerType.AIRPLANE_MODE_CHANGED.displayName, Icons.Default.AirplanemodeActive),
    TypeItem(TriggerType.SMS_RECEIVED.typeId, TriggerType.SMS_RECEIVED.displayName, Icons.Default.Sms),
    TypeItem(TriggerType.CALL_INCOMING.typeId, TriggerType.CALL_INCOMING.displayName, Icons.Default.Call),
    TypeItem(TriggerType.CALL_ENDED.typeId, TriggerType.CALL_ENDED.displayName, Icons.Default.CallEnd),
    TypeItem(TriggerType.CALL_MISSED.typeId, TriggerType.CALL_MISSED.displayName, Icons.AutoMirrored.Filled.CallMissed),
    TypeItem(TriggerType.REGULAR_INTERVAL.typeId, TriggerType.REGULAR_INTERVAL.displayName, Icons.Default.Repeat),
)

// M2 + M4 + M5 actions
private val AVAILABLE_ACTIONS = listOf(
    TypeItem(ActionType.DISPLAY_NOTIFICATION.typeId, ActionType.DISPLAY_NOTIFICATION.displayName, Icons.Default.Notifications),
    TypeItem(ActionType.LAUNCH_APPLICATION.typeId, ActionType.LAUNCH_APPLICATION.displayName, Icons.Default.PlayArrow),
    TypeItem(ActionType.SET_VOLUME.typeId, ActionType.SET_VOLUME.displayName, Icons.Default.MusicNote),
    TypeItem(ActionType.VIBRATE.typeId, ActionType.VIBRATE.displayName, Icons.Default.Vibration),
    TypeItem(ActionType.WAIT.typeId, ActionType.WAIT.displayName, Icons.Default.Timer),
    TypeItem(ActionType.SET_VARIABLE.typeId, ActionType.SET_VARIABLE.displayName, Icons.Default.Code),
    TypeItem(ActionType.DELETE_VARIABLE.typeId, ActionType.DELETE_VARIABLE.displayName, Icons.Default.DeleteSweep),
    TypeItem(ActionType.CLEAR_VARIABLES.typeId, ActionType.CLEAR_VARIABLES.displayName, Icons.Default.ClearAll),
    TypeItem(ActionType.WIFI_CONFIGURE.typeId, ActionType.WIFI_CONFIGURE.displayName, Icons.Default.Wifi),
    TypeItem(ActionType.BLUETOOTH_CONFIGURE.typeId, ActionType.BLUETOOTH_CONFIGURE.displayName, Icons.Default.Bluetooth),
    TypeItem(ActionType.AIRPLANE_MODE.typeId, ActionType.AIRPLANE_MODE.displayName, Icons.Default.AirplanemodeActive),
    TypeItem(ActionType.SEND_SMS.typeId, ActionType.SEND_SMS.displayName, Icons.Default.Sms),
    TypeItem(ActionType.MAKE_CALL.typeId, ActionType.MAKE_CALL.displayName, Icons.Default.Call),
    TypeItem(ActionType.LAUNCH_HOME_SCREEN.typeId, ActionType.LAUNCH_HOME_SCREEN.displayName, Icons.Default.Home),
    TypeItem(ActionType.OPEN_WEBSITE.typeId, ActionType.OPEN_WEBSITE.displayName, Icons.Default.Language),
    TypeItem(ActionType.HTTP_REQUEST.typeId, ActionType.HTTP_REQUEST.displayName, Icons.Default.Http),
    TypeItem(ActionType.SPEAK_TEXT.typeId, ActionType.SPEAK_TEXT.displayName, Icons.Default.RecordVoiceOver),
    TypeItem(ActionType.FILL_CLIPBOARD.typeId, ActionType.FILL_CLIPBOARD.displayName, Icons.Default.ContentPaste),
)

// M4 + M5 constraints
private val AVAILABLE_CONSTRAINTS = listOf(
    TypeItem(ConstraintType.BATTERY_LEVEL.typeId, ConstraintType.BATTERY_LEVEL.displayName, Icons.Default.BatteryChargingFull),
    TypeItem(ConstraintType.TIME_OF_DAY.typeId, ConstraintType.TIME_OF_DAY.displayName, Icons.Default.Schedule),
    TypeItem(ConstraintType.DAY_OF_WEEK.typeId, ConstraintType.DAY_OF_WEEK.displayName, Icons.Default.Today),
    TypeItem(ConstraintType.WIFI_CONNECTED.typeId, ConstraintType.WIFI_CONNECTED.displayName, Icons.Default.Wifi),
    TypeItem(ConstraintType.SCREEN_STATE.typeId, ConstraintType.SCREEN_STATE.displayName, Icons.Default.ScreenLockPortrait),
    TypeItem(ConstraintType.POWER_CONNECTED.typeId, ConstraintType.POWER_CONNECTED.displayName, Icons.Default.PowerSettingsNew),
    TypeItem(ConstraintType.APP_RUNNING.typeId, ConstraintType.APP_RUNNING.displayName, Icons.Default.RocketLaunch),
    TypeItem(ConstraintType.VARIABLE_VALUE.typeId, ConstraintType.VARIABLE_VALUE.displayName, Icons.Default.Code),
    TypeItem(ConstraintType.BLUETOOTH_CONNECTED.typeId, ConstraintType.BLUETOOTH_CONNECTED.displayName, Icons.Default.Bluetooth),
    TypeItem(ConstraintType.WIFI_ENABLED.typeId, ConstraintType.WIFI_ENABLED.displayName, Icons.Default.SignalWifi4Bar),
    TypeItem(ConstraintType.AIRPLANE_MODE.typeId, ConstraintType.AIRPLANE_MODE.displayName, Icons.Default.AirplanemodeActive),
    TypeItem(ConstraintType.CALL_STATE.typeId, ConstraintType.CALL_STATE.displayName, Icons.Default.PhoneInTalk),
)

private fun triggerIcon(typeId: String): ImageVector =
    AVAILABLE_TRIGGERS.firstOrNull { it.id == typeId }?.icon ?: Icons.Default.FlashOn

private fun actionIcon(typeId: String): ImageVector =
    AVAILABLE_ACTIONS.firstOrNull { it.id == typeId }?.icon ?: Icons.Default.FlashOn

private fun constraintIcon(typeId: String): ImageVector =
    AVAILABLE_CONSTRAINTS.firstOrNull { it.id == typeId }?.icon ?: Icons.Default.FlashOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: MacroEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTriggerPicker by remember { mutableStateOf(false) }
    var showActionPicker by remember { mutableStateOf(false) }
    var showConstraintPicker by remember { mutableStateOf(false) }

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

            // Constraints section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Constraints", uiState.constraints.size)
            }
            itemsIndexed(uiState.constraints) { index, constraint ->
                Column {
                    if (index > 0) {
                        LogicOperatorSelector(
                            operator = constraint.logicOperator,
                            onOperatorChanged = { viewModel.updateConstraintLogicOperator(index, it) },
                        )
                    }
                    ConfigCard(
                        title = constraint.type?.displayName ?: constraint.typeId,
                        icon = constraintIcon(constraint.typeId),
                        onRemove = { viewModel.removeConstraint(index) },
                    ) {
                        ConstraintConfigContent(
                            typeId = constraint.typeId,
                            configJson = constraint.configJson,
                            onConfigChanged = { viewModel.updateConstraintConfig(index, it) },
                        )
                    }
                }
            }
            item {
                AddButton("Add Constraint") { showConstraintPicker = true }
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

    if (showConstraintPicker) {
        TypePickerSheet(
            title = "Add Constraint",
            types = AVAILABLE_CONSTRAINTS,
            onTypeSelected = { typeId ->
                showConstraintPicker = false
                ConstraintType.fromTypeId(typeId)?.let { viewModel.addConstraint(it) }
            },
            onDismiss = { showConstraintPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogicOperatorSelector(
    operator: LogicOperator,
    onOperatorChanged: (LogicOperator) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = operator.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(0.4f),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                LogicOperator.entries.forEach { op ->
                    DropdownMenuItem(
                        text = { Text(op.name) },
                        onClick = {
                            expanded = false
                            onOperatorChanged(op)
                        },
                    )
                }
            }
        }
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
        TriggerType.WIFI_STATE_CHANGE.typeId -> WifiStateChangeConfigEditor(configJson, onConfigChanged)
        TriggerType.WIFI_SSID_TRANSITION.typeId -> WifiSsidTransitionConfigEditor(configJson, onConfigChanged)
        TriggerType.BLUETOOTH_EVENT.typeId -> BluetoothEventConfigEditor(configJson, onConfigChanged)
        TriggerType.DATA_CONNECTIVITY_CHANGE.typeId -> DataConnectivityChangeConfigEditor(configJson, onConfigChanged)
        TriggerType.AIRPLANE_MODE_CHANGED.typeId -> AirplaneModeChangedConfigEditor(configJson, onConfigChanged)
        TriggerType.SMS_RECEIVED.typeId -> SmsReceivedConfigEditor(configJson, onConfigChanged)
        TriggerType.CALL_INCOMING.typeId -> CallIncomingConfigEditor(configJson, onConfigChanged)
        TriggerType.CALL_ENDED.typeId -> CallEndedConfigEditor(configJson, onConfigChanged)
        TriggerType.CALL_MISSED.typeId -> CallMissedConfigEditor(configJson, onConfigChanged)
        TriggerType.REGULAR_INTERVAL.typeId -> RegularIntervalConfigEditor(configJson, onConfigChanged)
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
        ActionType.SET_VARIABLE.typeId -> SetVariableConfigEditor(configJson, onConfigChanged)
        ActionType.DELETE_VARIABLE.typeId -> DeleteVariableConfigEditor(configJson, onConfigChanged)
        ActionType.CLEAR_VARIABLES.typeId -> Text("Clears all variables", style = MaterialTheme.typography.bodySmall)
        ActionType.WIFI_CONFIGURE.typeId -> WifiConfigureConfigEditor(configJson, onConfigChanged)
        ActionType.BLUETOOTH_CONFIGURE.typeId -> BluetoothConfigureConfigEditor(configJson, onConfigChanged)
        ActionType.AIRPLANE_MODE.typeId -> AirplaneModeConfigEditor(configJson, onConfigChanged)
        ActionType.SEND_SMS.typeId -> SendSmsConfigEditor(configJson, onConfigChanged)
        ActionType.MAKE_CALL.typeId -> MakeCallConfigEditor(configJson, onConfigChanged)
        ActionType.LAUNCH_HOME_SCREEN.typeId -> Text("Launches the home screen", style = MaterialTheme.typography.bodySmall)
        ActionType.OPEN_WEBSITE.typeId -> OpenWebsiteConfigEditor(configJson, onConfigChanged)
        ActionType.HTTP_REQUEST.typeId -> HttpRequestConfigEditor(configJson, onConfigChanged)
        ActionType.SPEAK_TEXT.typeId -> SpeakTextConfigEditor(configJson, onConfigChanged)
        ActionType.FILL_CLIPBOARD.typeId -> FillClipboardConfigEditor(configJson, onConfigChanged)
        else -> Text("Unknown action type: $typeId", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ConstraintConfigContent(
    typeId: String,
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    when (typeId) {
        ConstraintType.BATTERY_LEVEL.typeId -> BatteryLevelConstraintEditor(configJson, onConfigChanged)
        ConstraintType.TIME_OF_DAY.typeId -> TimeOfDayConstraintEditor(configJson, onConfigChanged)
        ConstraintType.DAY_OF_WEEK.typeId -> DayOfWeekConstraintEditor(configJson, onConfigChanged)
        ConstraintType.WIFI_CONNECTED.typeId -> WifiConnectedConstraintEditor(configJson, onConfigChanged)
        ConstraintType.SCREEN_STATE.typeId -> ScreenStateConstraintEditor(configJson, onConfigChanged)
        ConstraintType.POWER_CONNECTED.typeId -> PowerConnectedConstraintEditor(configJson, onConfigChanged)
        ConstraintType.APP_RUNNING.typeId -> AppRunningConstraintEditor(configJson, onConfigChanged)
        ConstraintType.VARIABLE_VALUE.typeId -> VariableValueConstraintEditor(configJson, onConfigChanged)
        ConstraintType.BLUETOOTH_CONNECTED.typeId -> BluetoothConnectedConstraintEditor(configJson, onConfigChanged)
        ConstraintType.WIFI_ENABLED.typeId -> WifiEnabledConstraintEditor(configJson, onConfigChanged)
        ConstraintType.AIRPLANE_MODE.typeId -> AirplaneModeConstraintEditor(configJson, onConfigChanged)
        ConstraintType.CALL_STATE.typeId -> CallStateConstraintEditor(configJson, onConfigChanged)
        else -> Text("Unknown constraint type: $typeId", style = MaterialTheme.typography.bodySmall)
    }
}
