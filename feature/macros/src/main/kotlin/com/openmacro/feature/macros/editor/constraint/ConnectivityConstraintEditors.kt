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
import com.openmacro.core.model.config.AirplaneModeConstraintConfig
import com.openmacro.core.model.config.BluetoothConnectedConfig
import com.openmacro.core.model.config.CallStateConstraintConfig
import com.openmacro.core.model.config.WifiEnabledConfig
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun BluetoothConnectedConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BluetoothConnectedConfig>(configJson) }
            .getOrDefault(BluetoothConnectedConfig())
    }

    Column {
        OutlinedTextField(
            value = config.deviceAddress,
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(BluetoothConnectedConfig.serializer(), config.copy(deviceAddress = it))
                )
            },
            label = { Text("Device address (blank = any)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun WifiEnabledConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WifiEnabledConfig>(configJson) }
            .getOrDefault(WifiEnabledConfig())
    }

    Column {
        SwitchRow("WiFi is enabled", config.enabled) {
            onConfigChanged(json.encodeToString(WifiEnabledConfig.serializer(), config.copy(enabled = it)))
        }
    }
}

@Composable
fun AirplaneModeConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AirplaneModeConstraintConfig>(configJson) }
            .getOrDefault(AirplaneModeConstraintConfig())
    }

    Column {
        SwitchRow("Airplane mode is on", config.enabled) {
            onConfigChanged(
                json.encodeToString(AirplaneModeConstraintConfig.serializer(), config.copy(enabled = it))
            )
        }
    }
}

private val CALL_STATES = listOf(
    "idle" to "Idle",
    "ringing" to "Ringing",
    "offhook" to "Off-hook (in call)",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallStateConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<CallStateConstraintConfig>(configJson) }
            .getOrDefault(CallStateConstraintConfig())
    }
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = CALL_STATES.firstOrNull { it.first == config.state }?.second ?: config.state

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Required call state") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                CALL_STATES.forEach { (state, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            expanded = false
                            onConfigChanged(
                                json.encodeToString(
                                    CallStateConstraintConfig.serializer(),
                                    config.copy(state = state),
                                )
                            )
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
