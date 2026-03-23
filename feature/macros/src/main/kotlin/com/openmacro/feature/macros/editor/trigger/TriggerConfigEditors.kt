package com.openmacro.feature.macros.editor.trigger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.openmacro.core.model.config.AppLaunchConfig
import com.openmacro.core.model.config.BatteryLevelTriggerConfig
import com.openmacro.core.model.config.DayTimeConfig
import com.openmacro.core.model.config.PowerConnectedConfig
import com.openmacro.core.model.config.ScreenOnOffConfig
import com.openmacro.core.ui.components.AppPickerSheet
import com.openmacro.core.ui.components.DayOfWeekSelector
import com.openmacro.core.ui.components.SliderWithLabel
import com.openmacro.core.ui.components.TimePickerDialog
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun ScreenOnOffConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ScreenOnOffConfig>(configJson) }
            .getOrDefault(ScreenOnOffConfig())
    }

    Column {
        SwitchRow("Trigger on Screen On", config.onScreenOn) {
            onConfigChanged(json.encodeToString(ScreenOnOffConfig.serializer(), config.copy(onScreenOn = it)))
        }
        SwitchRow("Trigger on Screen Off", config.onScreenOff) {
            onConfigChanged(json.encodeToString(ScreenOnOffConfig.serializer(), config.copy(onScreenOff = it)))
        }
    }
}

@Composable
fun BatteryLevelConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BatteryLevelTriggerConfig>(configJson) }
            .getOrDefault(BatteryLevelTriggerConfig())
    }

    Column {
        SliderWithLabel(
            label = "Battery threshold",
            value = config.threshold.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(
                        BatteryLevelTriggerConfig.serializer(),
                        config.copy(threshold = it.toInt()),
                    ),
                )
            },
            valueRange = 5f..95f,
            valueText = "${config.threshold}%",
        )
        SwitchRow("When below threshold", config.whenBelow) {
            onConfigChanged(
                json.encodeToString(
                    BatteryLevelTriggerConfig.serializer(),
                    config.copy(whenBelow = it),
                ),
            )
        }
    }
}

@Composable
fun PowerConnectedConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<PowerConnectedConfig>(configJson) }
            .getOrDefault(PowerConnectedConfig())
    }

    Column {
        SwitchRow("Trigger on connect", config.onConnect) {
            onConfigChanged(json.encodeToString(PowerConnectedConfig.serializer(), config.copy(onConnect = it)))
        }
        SwitchRow("Trigger on disconnect", config.onDisconnect) {
            onConfigChanged(json.encodeToString(PowerConnectedConfig.serializer(), config.copy(onDisconnect = it)))
        }
    }
}

@Composable
fun DayTimeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DayTimeConfig>(configJson) }
            .getOrDefault(DayTimeConfig())
    }
    var showTimePicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Time: %02d:%02d".format(config.hour, config.minute))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Days", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        DayOfWeekSelector(
            selectedDays = config.daysOfWeek,
            onDaysChanged = {
                onConfigChanged(json.encodeToString(DayTimeConfig.serializer(), config.copy(daysOfWeek = it)))
            },
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = config.hour,
            initialMinute = config.minute,
            onConfirm = { h, m ->
                showTimePicker = false
                onConfigChanged(json.encodeToString(DayTimeConfig.serializer(), config.copy(hour = h, minute = m)))
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
fun AppLaunchConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AppLaunchConfig>(configJson) }
            .getOrDefault(AppLaunchConfig())
    }
    var showAppPicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showAppPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (config.packageName.isBlank()) "Select Application" else config.packageName)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SwitchRow("Trigger on launch", config.onLaunch) {
            onConfigChanged(json.encodeToString(AppLaunchConfig.serializer(), config.copy(onLaunch = it)))
        }
        SwitchRow("Trigger on close", config.onClose) {
            onConfigChanged(json.encodeToString(AppLaunchConfig.serializer(), config.copy(onClose = it)))
        }
    }

    if (showAppPicker) {
        AppPickerSheet(
            onAppSelected = { pkg ->
                showAppPicker = false
                onConfigChanged(json.encodeToString(AppLaunchConfig.serializer(), config.copy(packageName = pkg)))
            },
            onDismiss = { showAppPicker = false },
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
