package com.openmacro.feature.macros.editor.trigger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.AirplaneModeChangedConfig
import com.openmacro.core.model.config.BluetoothEventConfig
import com.openmacro.core.model.config.CallEndedConfig
import com.openmacro.core.model.config.CallIncomingConfig
import com.openmacro.core.model.config.CallMissedConfig
import com.openmacro.core.model.config.DataConnectivityChangeConfig
import com.openmacro.core.model.config.RegularIntervalConfig
import com.openmacro.core.model.config.SmsReceivedConfig
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
fun RegularIntervalConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<RegularIntervalConfig>(configJson) }
            .getOrDefault(RegularIntervalConfig())
    }

    Column {
        SliderWithLabel(
            label = "Interval",
            value = config.intervalMs.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(RegularIntervalConfig.serializer(), config.copy(intervalMs = it.toLong()))
                )
            },
            valueRange = 5_000f..3_600_000f,
            valueText = formatInterval(config.intervalMs),
        )
    }
}

private fun formatInterval(ms: Long): String = when {
    ms >= 3_600_000 -> "${"%.1f".format(ms / 3_600_000f)}h"
    ms >= 60_000 -> "${"%.1f".format(ms / 60_000f)}m"
    else -> "${ms / 1_000}s"
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
