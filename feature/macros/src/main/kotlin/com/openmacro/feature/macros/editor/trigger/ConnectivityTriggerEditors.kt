package com.openmacro.feature.macros.editor.trigger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.AirplaneModeChangedConfig
import com.openmacro.core.model.config.BluetoothEventConfig
import com.openmacro.core.model.config.CallEndedConfig
import com.openmacro.core.model.config.CallIncomingConfig
import com.openmacro.core.model.config.CallMissedConfig
import com.openmacro.core.model.config.DataConnectivityChangeConfig
import com.openmacro.core.model.config.RegularIntervalConfig
import com.openmacro.core.model.config.SmsReceivedConfig
import com.openmacro.core.model.config.SmsSentConfig
import com.openmacro.core.model.config.WifiSsidTransitionConfig
import com.openmacro.core.model.config.WifiStateChangeConfig
import com.openmacro.core.ui.components.ContactPickerField
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun WifiStateChangeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WifiStateChangeConfig>(configJson) }
            .getOrDefault(WifiStateChangeConfig())
    }

    Column {
        SwitchRow("Trigger on WiFi enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(WifiStateChangeConfig.serializer(), config.copy(onEnabled = it)))
        }
        SwitchRow("Trigger on WiFi disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(WifiStateChangeConfig.serializer(), config.copy(onDisabled = it)))
        }
    }
}

@Composable
fun WifiSsidTransitionConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WifiSsidTransitionConfig>(configJson) }
            .getOrDefault(WifiSsidTransitionConfig())
    }

    Column {
        OutlinedTextField(
            value = config.ssid,
            onValueChange = {
                onConfigChanged(json.encodeToString(WifiSsidTransitionConfig.serializer(), config.copy(ssid = it)))
            },
            label = { Text("SSID (blank = any)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        SwitchRow("Trigger on connect", config.onConnect) {
            onConfigChanged(json.encodeToString(WifiSsidTransitionConfig.serializer(), config.copy(onConnect = it)))
        }
        SwitchRow("Trigger on disconnect", config.onDisconnect) {
            onConfigChanged(json.encodeToString(WifiSsidTransitionConfig.serializer(), config.copy(onDisconnect = it)))
        }
    }
}

@Composable
fun BluetoothEventConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BluetoothEventConfig>(configJson) }
            .getOrDefault(BluetoothEventConfig())
    }

    Column {
        SwitchRow("Bluetooth enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(BluetoothEventConfig.serializer(), config.copy(onEnabled = it)))
        }
        SwitchRow("Bluetooth disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(BluetoothEventConfig.serializer(), config.copy(onDisabled = it)))
        }
        SwitchRow("Device connected", config.onDeviceConnected) {
            onConfigChanged(json.encodeToString(BluetoothEventConfig.serializer(), config.copy(onDeviceConnected = it)))
        }
        SwitchRow("Device disconnected", config.onDeviceDisconnected) {
            onConfigChanged(json.encodeToString(BluetoothEventConfig.serializer(), config.copy(onDeviceDisconnected = it)))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.deviceAddress,
            onValueChange = {
                onConfigChanged(json.encodeToString(BluetoothEventConfig.serializer(), config.copy(deviceAddress = it)))
            },
            label = { Text("Device address (optional, blank = any)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DataConnectivityChangeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DataConnectivityChangeConfig>(configJson) }
            .getOrDefault(DataConnectivityChangeConfig())
    }

    Column {
        SwitchRow("Trigger on connected", config.onConnected) {
            onConfigChanged(json.encodeToString(DataConnectivityChangeConfig.serializer(), config.copy(onConnected = it)))
        }
        SwitchRow("Trigger on disconnected", config.onDisconnected) {
            onConfigChanged(json.encodeToString(DataConnectivityChangeConfig.serializer(), config.copy(onDisconnected = it)))
        }
    }
}

@Composable
fun AirplaneModeChangedConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AirplaneModeChangedConfig>(configJson) }
            .getOrDefault(AirplaneModeChangedConfig())
    }

    Column {
        SwitchRow("Trigger on enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(AirplaneModeChangedConfig.serializer(), config.copy(onEnabled = it)))
        }
        SwitchRow("Trigger on disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(AirplaneModeChangedConfig.serializer(), config.copy(onDisabled = it)))
        }
    }
}

@Composable
fun SmsReceivedConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SmsReceivedConfig>(configJson) }
            .getOrDefault(SmsReceivedConfig())
    }

    Column {
        ContactPickerField(
            value = config.senderFilter,
            onValueChange = {
                onConfigChanged(json.encodeToString(SmsReceivedConfig.serializer(), config.copy(senderFilter = it)))
            },
            label = "Sender filter (blank = any)",
        )
    }
}

@Composable
fun CallIncomingConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<CallIncomingConfig>(configJson) }
            .getOrDefault(CallIncomingConfig())
    }

    Column {
        ContactPickerField(
            value = config.numberFilter,
            onValueChange = {
                onConfigChanged(json.encodeToString(CallIncomingConfig.serializer(), config.copy(numberFilter = it)))
            },
            label = "Number filter (blank = any)",
        )
    }
}

@Composable
fun CallEndedConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<CallEndedConfig>(configJson) }
            .getOrDefault(CallEndedConfig())
    }

    Column {
        ContactPickerField(
            value = config.numberFilter,
            onValueChange = {
                onConfigChanged(json.encodeToString(CallEndedConfig.serializer(), config.copy(numberFilter = it)))
            },
            label = "Number filter (blank = any)",
        )
    }
}

@Composable
fun CallMissedConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<CallMissedConfig>(configJson) }
            .getOrDefault(CallMissedConfig())
    }

    Column {
        ContactPickerField(
            value = config.numberFilter,
            onValueChange = {
                onConfigChanged(json.encodeToString(CallMissedConfig.serializer(), config.copy(numberFilter = it)))
            },
            label = "Number filter (blank = any)",
        )
    }
}

@Composable
fun SmsSentConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SmsSentConfig>(configJson) }
            .getOrDefault(SmsSentConfig())
    }

    Column {
        ContactPickerField(
            value = config.recipientFilter,
            onValueChange = {
                onConfigChanged(json.encodeToString(SmsSentConfig.serializer(), config.copy(recipientFilter = it)))
            },
            label = "Recipient filter (blank = any)",
        )
    }
}

@Composable
fun RegularIntervalConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<RegularIntervalConfig>(configJson) }
            .getOrDefault(RegularIntervalConfig())
    }

    val units = listOf("Seconds" to 1_000L, "Minutes" to 60_000L, "Hours" to 3_600_000L)
    val bestUnit = units.lastOrNull { config.intervalMs >= it.second && config.intervalMs % it.second == 0L } ?: units[0]
    var selectedUnit by remember(configJson) { mutableStateOf(bestUnit) }
    var textValue by remember(configJson) { mutableStateOf((config.intervalMs / selectedUnit.second).toString()) }

    Column {
        Text("Interval", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        IntervalInput(
            value = textValue,
            onValueChange = { newText ->
                textValue = newText
                val number = newText.toLongOrNull() ?: return@IntervalInput
                if (number > 0) {
                    val ms = number * selectedUnit.second
                    onConfigChanged(json.encodeToString(RegularIntervalConfig.serializer(), config.copy(intervalMs = ms)))
                }
            },
            selectedUnit = selectedUnit.first,
            onUnitSelected = { unitName ->
                val unit = units.first { it.first == unitName }
                selectedUnit = unit
                val number = textValue.toLongOrNull() ?: 1
                val ms = number * unit.second
                onConfigChanged(json.encodeToString(RegularIntervalConfig.serializer(), config.copy(intervalMs = ms)))
            },
            units = units.map { it.first },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalInput(
    value: String,
    onValueChange: (String) -> Unit,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    units: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newVal ->
                if (newVal.all { it.isDigit() }) onValueChange(newVal)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = selectedUnit,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            expanded = false
                            onUnitSelected(unit)
                        },
                    )
                }
            }
        }
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
