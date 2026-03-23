package com.openmacro.feature.macros.editor.constraint

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.AppRunningConfig
import com.openmacro.core.model.config.BatteryLevelConstraintConfig
import com.openmacro.core.model.config.DayOfWeekConfig
import com.openmacro.core.model.config.PowerConnectedConstraintConfig
import com.openmacro.core.model.config.ScreenStateConfig
import com.openmacro.core.model.config.TimeOfDayConfig
import com.openmacro.core.model.config.VariableValueConfig
import com.openmacro.core.model.config.WifiConnectedConfig
import com.openmacro.core.ui.components.AppPickerSheet
import com.openmacro.core.ui.components.DayOfWeekSelector
import com.openmacro.core.ui.components.SliderWithLabel
import com.openmacro.core.ui.components.TimePickerDialog
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun BatteryLevelConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BatteryLevelConstraintConfig>(configJson) }
            .getOrDefault(BatteryLevelConstraintConfig())
    }

    Column {
        SliderWithLabel(
            label = "Min battery level",
            value = config.minLevel.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(
                        BatteryLevelConstraintConfig.serializer(),
                        config.copy(minLevel = it.toInt()),
                    ),
                )
            },
            valueRange = 0f..100f,
            valueText = "${config.minLevel}%",
        )
        SliderWithLabel(
            label = "Max battery level",
            value = config.maxLevel.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(
                        BatteryLevelConstraintConfig.serializer(),
                        config.copy(maxLevel = it.toInt()),
                    ),
                )
            },
            valueRange = 0f..100f,
            valueText = "${config.maxLevel}%",
        )
    }
}

@Composable
fun TimeOfDayConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<TimeOfDayConfig>(configJson) }
            .getOrDefault(TimeOfDayConfig())
    }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showStartPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start: %02d:%02d".format(config.startHour, config.startMinute))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showEndPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("End: %02d:%02d".format(config.endHour, config.endMinute))
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initialHour = config.startHour,
            initialMinute = config.startMinute,
            onConfirm = { h, m ->
                showStartPicker = false
                onConfigChanged(
                    json.encodeToString(TimeOfDayConfig.serializer(), config.copy(startHour = h, startMinute = m)),
                )
            },
            onDismiss = { showStartPicker = false },
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            initialHour = config.endHour,
            initialMinute = config.endMinute,
            onConfirm = { h, m ->
                showEndPicker = false
                onConfigChanged(
                    json.encodeToString(TimeOfDayConfig.serializer(), config.copy(endHour = h, endMinute = m)),
                )
            },
            onDismiss = { showEndPicker = false },
        )
    }
}

@Composable
fun DayOfWeekConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DayOfWeekConfig>(configJson) }
            .getOrDefault(DayOfWeekConfig())
    }

    Column {
        Text("Active days", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        DayOfWeekSelector(
            selectedDays = config.days,
            onDaysChanged = {
                onConfigChanged(json.encodeToString(DayOfWeekConfig.serializer(), config.copy(days = it)))
            },
        )
    }
}

@Composable
fun WifiConnectedConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WifiConnectedConfig>(configJson) }
            .getOrDefault(WifiConnectedConfig())
    }

    Column {
        OutlinedTextField(
            value = config.ssid ?: "",
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(
                        WifiConnectedConfig.serializer(),
                        config.copy(ssid = it.ifBlank { null }),
                    ),
                )
            },
            label = { Text("SSID (optional, blank = any)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ScreenStateConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ScreenStateConfig>(configJson) }
            .getOrDefault(ScreenStateConfig())
    }

    Column {
        SwitchRow("Screen is on", config.screenOn) {
            onConfigChanged(json.encodeToString(ScreenStateConfig.serializer(), config.copy(screenOn = it)))
        }
    }
}

@Composable
fun PowerConnectedConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<PowerConnectedConstraintConfig>(configJson) }
            .getOrDefault(PowerConnectedConstraintConfig())
    }

    Column {
        SwitchRow("Power is connected", config.connected) {
            onConfigChanged(
                json.encodeToString(
                    PowerConnectedConstraintConfig.serializer(),
                    config.copy(connected = it),
                ),
            )
        }
    }
}

@Composable
fun AppRunningConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AppRunningConfig>(configJson) }
            .getOrDefault(AppRunningConfig())
    }
    var showAppPicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showAppPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (config.packageName.isBlank()) "Select Application" else config.packageName)
        }
    }

    if (showAppPicker) {
        AppPickerSheet(
            onAppSelected = { pkg ->
                showAppPicker = false
                onConfigChanged(json.encodeToString(AppRunningConfig.serializer(), config.copy(packageName = pkg)))
            },
            onDismiss = { showAppPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariableValueConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<VariableValueConfig>(configJson) }
            .getOrDefault(VariableValueConfig())
    }
    var operatorExpanded by remember { mutableStateOf(false) }
    val operators = listOf("==", "!=", ">", "<", ">=", "<=", "contains")

    Column {
        OutlinedTextField(
            value = config.variableName,
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(VariableValueConfig.serializer(), config.copy(variableName = it)),
                )
            },
            label = { Text("Variable name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = operatorExpanded,
            onExpandedChange = { operatorExpanded = it },
        ) {
            OutlinedTextField(
                value = config.operator,
                onValueChange = {},
                readOnly = true,
                label = { Text("Operator") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operatorExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = operatorExpanded,
                onDismissRequest = { operatorExpanded = false },
            ) {
                operators.forEach { op ->
                    DropdownMenuItem(
                        text = { Text(op) },
                        onClick = {
                            operatorExpanded = false
                            onConfigChanged(
                                json.encodeToString(VariableValueConfig.serializer(), config.copy(operator = op)),
                            )
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.value,
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(VariableValueConfig.serializer(), config.copy(value = it)),
                )
            },
            label = { Text("Value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
