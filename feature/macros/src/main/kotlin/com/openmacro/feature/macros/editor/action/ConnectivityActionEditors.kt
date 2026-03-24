package com.openmacro.feature.macros.editor.action

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
import com.openmacro.core.model.config.AirplaneModeConfig
import com.openmacro.core.model.config.BluetoothConfigureConfig
import com.openmacro.core.model.config.FillClipboardConfig
import com.openmacro.core.model.config.HttpRequestConfig
import com.openmacro.core.model.config.MakeCallConfig
import com.openmacro.core.model.config.OpenWebsiteConfig
import com.openmacro.core.model.config.SendSmsConfig
import com.openmacro.core.model.config.SpeakTextConfig
import com.openmacro.core.model.config.WifiConfigureConfig
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun WifiConfigureConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WifiConfigureConfig>(configJson) }
            .getOrDefault(WifiConfigureConfig())
    }

    Column {
        SwitchRow("Enable WiFi", config.enable) {
            onConfigChanged(json.encodeToString(WifiConfigureConfig.serializer(), config.copy(enable = it)))
        }
        Text(
            "Note: On Android 10+, this opens WiFi settings panel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun BluetoothConfigureConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BluetoothConfigureConfig>(configJson) }
            .getOrDefault(BluetoothConfigureConfig())
    }

    Column {
        SwitchRow("Enable Bluetooth", config.enable) {
            onConfigChanged(json.encodeToString(BluetoothConfigureConfig.serializer(), config.copy(enable = it)))
        }
    }
}

@Composable
fun AirplaneModeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AirplaneModeConfig>(configJson) }
            .getOrDefault(AirplaneModeConfig())
    }

    Column {
        SwitchRow("Enable Airplane Mode", config.enable) {
            onConfigChanged(json.encodeToString(AirplaneModeConfig.serializer(), config.copy(enable = it)))
        }
        Text(
            "May require special permissions or open settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SendSmsConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SendSmsConfig>(configJson) }
            .getOrDefault(SendSmsConfig())
    }

    Column {
        OutlinedTextField(
            value = config.phoneNumber,
            onValueChange = {
                onConfigChanged(json.encodeToString(SendSmsConfig.serializer(), config.copy(phoneNumber = it)))
            },
            label = { Text("Phone number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.message,
            onValueChange = {
                onConfigChanged(json.encodeToString(SendSmsConfig.serializer(), config.copy(message = it)))
            },
            label = { Text("Message") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun MakeCallConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<MakeCallConfig>(configJson) }
            .getOrDefault(MakeCallConfig())
    }

    Column {
        OutlinedTextField(
            value = config.phoneNumber,
            onValueChange = {
                onConfigChanged(json.encodeToString(MakeCallConfig.serializer(), config.copy(phoneNumber = it)))
            },
            label = { Text("Phone number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun OpenWebsiteConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<OpenWebsiteConfig>(configJson) }
            .getOrDefault(OpenWebsiteConfig())
    }

    Column {
        OutlinedTextField(
            value = config.url,
            onValueChange = {
                onConfigChanged(json.encodeToString(OpenWebsiteConfig.serializer(), config.copy(url = it)))
            },
            label = { Text("URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        SwitchRow("Open in browser", config.useBrowser) {
            onConfigChanged(json.encodeToString(OpenWebsiteConfig.serializer(), config.copy(useBrowser = it)))
        }
    }
}

private val HTTP_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpRequestConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<HttpRequestConfig>(configJson) }
            .getOrDefault(HttpRequestConfig())
    }
    var methodExpanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = config.url,
            onValueChange = {
                onConfigChanged(json.encodeToString(HttpRequestConfig.serializer(), config.copy(url = it)))
            },
            label = { Text("URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = methodExpanded,
            onExpandedChange = { methodExpanded = it },
        ) {
            OutlinedTextField(
                value = config.method,
                onValueChange = {},
                readOnly = true,
                label = { Text("Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = methodExpanded,
                onDismissRequest = { methodExpanded = false },
            ) {
                HTTP_METHODS.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            methodExpanded = false
                            onConfigChanged(json.encodeToString(HttpRequestConfig.serializer(), config.copy(method = method)))
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.headers,
            onValueChange = {
                onConfigChanged(json.encodeToString(HttpRequestConfig.serializer(), config.copy(headers = it)))
            },
            label = { Text("Headers (Key: Value, one per line)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.body,
            onValueChange = {
                onConfigChanged(json.encodeToString(HttpRequestConfig.serializer(), config.copy(body = it)))
            },
            label = { Text("Body") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.saveResponseTo,
            onValueChange = {
                onConfigChanged(json.encodeToString(HttpRequestConfig.serializer(), config.copy(saveResponseTo = it)))
            },
            label = { Text("Save response to variable (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SpeakTextConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SpeakTextConfig>(configJson) }
            .getOrDefault(SpeakTextConfig())
    }

    Column {
        OutlinedTextField(
            value = config.text,
            onValueChange = {
                onConfigChanged(json.encodeToString(SpeakTextConfig.serializer(), config.copy(text = it)))
            },
            label = { Text("Text to speak") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.language,
            onValueChange = {
                onConfigChanged(json.encodeToString(SpeakTextConfig.serializer(), config.copy(language = it)))
            },
            label = { Text("Language (e.g. en-US, blank = default)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        SliderWithLabel(
            label = "Pitch",
            value = config.pitch,
            onValueChange = {
                onConfigChanged(json.encodeToString(SpeakTextConfig.serializer(), config.copy(pitch = it)))
            },
            valueRange = 0.1f..4.0f,
            valueText = "%.1f".format(config.pitch),
        )
        SliderWithLabel(
            label = "Speed",
            value = config.speed,
            onValueChange = {
                onConfigChanged(json.encodeToString(SpeakTextConfig.serializer(), config.copy(speed = it)))
            },
            valueRange = 0.1f..4.0f,
            valueText = "%.1f".format(config.speed),
        )
    }
}

@Composable
fun FillClipboardConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<FillClipboardConfig>(configJson) }
            .getOrDefault(FillClipboardConfig())
    }

    Column {
        OutlinedTextField(
            value = config.text,
            onValueChange = {
                onConfigChanged(json.encodeToString(FillClipboardConfig.serializer(), config.copy(text = it)))
            },
            label = { Text("Text to copy") },
            minLines = 2,
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
