package com.openmacro.feature.macros.editor.action

import android.media.AudioManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.DeleteVariableConfig
import com.openmacro.core.model.config.DisplayNotificationConfig
import com.openmacro.core.model.config.LaunchApplicationConfig
import com.openmacro.core.model.config.SetVariableConfig
import com.openmacro.core.model.config.SetVolumeConfig
import com.openmacro.core.model.config.VibrateConfig
import com.openmacro.core.model.config.WaitConfig
import com.openmacro.core.ui.components.AppPickerSheet
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun DisplayNotificationConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DisplayNotificationConfig>(configJson) }
            .getOrDefault(DisplayNotificationConfig())
    }

    Column {
        OutlinedTextField(
            value = config.title,
            onValueChange = {
                onConfigChanged(json.encodeToString(DisplayNotificationConfig.serializer(), config.copy(title = it)))
            },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.body,
            onValueChange = {
                onConfigChanged(json.encodeToString(DisplayNotificationConfig.serializer(), config.copy(body = it)))
            },
            label = { Text("Body") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun LaunchApplicationConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<LaunchApplicationConfig>(configJson) }
            .getOrDefault(LaunchApplicationConfig())
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
                onConfigChanged(json.encodeToString(LaunchApplicationConfig.serializer(), LaunchApplicationConfig(packageName = pkg)))
            },
            onDismiss = { showAppPicker = false },
        )
    }
}

private val STREAM_TYPES = listOf(
    AudioManager.STREAM_MUSIC to "Music",
    AudioManager.STREAM_RING to "Ring",
    AudioManager.STREAM_NOTIFICATION to "Notification",
    AudioManager.STREAM_ALARM to "Alarm",
    AudioManager.STREAM_SYSTEM to "System",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetVolumeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SetVolumeConfig>(configJson) }
            .getOrDefault(SetVolumeConfig())
    }
    var expanded by remember { mutableStateOf(false) }
    val currentStreamName = STREAM_TYPES.firstOrNull { it.first == config.streamType }?.second ?: "Music"

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = currentStreamName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Stream") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                STREAM_TYPES.forEach { (streamType, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            expanded = false
                            onConfigChanged(
                                json.encodeToString(SetVolumeConfig.serializer(), config.copy(streamType = streamType)),
                            )
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SliderWithLabel(
            label = "Volume level",
            value = config.level.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(SetVolumeConfig.serializer(), config.copy(level = it.toInt())),
                )
            },
            valueRange = 0f..100f,
            valueText = "${config.level}%",
        )
    }
}

@Composable
fun VibrateConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<VibrateConfig>(configJson) }
            .getOrDefault(VibrateConfig())
    }

    Column {
        SliderWithLabel(
            label = "Duration",
            value = config.durationMs.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(VibrateConfig.serializer(), config.copy(durationMs = it.toLong())),
                )
            },
            valueRange = 100f..5000f,
            valueText = "${config.durationMs}ms",
        )
    }
}

@Composable
fun WaitConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<WaitConfig>(configJson) }
            .getOrDefault(WaitConfig())
    }

    Column {
        SliderWithLabel(
            label = "Wait duration",
            value = config.durationMs.toFloat(),
            onValueChange = {
                onConfigChanged(
                    json.encodeToString(WaitConfig.serializer(), config.copy(durationMs = it.toLong())),
                )
            },
            valueRange = 100f..30000f,
            valueText = if (config.durationMs >= 1000) "${"%.1f".format(config.durationMs / 1000f)}s"
            else "${config.durationMs}ms",
        )
    }
}

@Composable
fun SetVariableConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SetVariableConfig>(configJson) }
            .getOrDefault(SetVariableConfig())
    }

    Column {
        OutlinedTextField(
            value = config.variableName,
            onValueChange = {
                onConfigChanged(json.encodeToString(SetVariableConfig.serializer(), config.copy(variableName = it)))
            },
            label = { Text("Variable name (prefix lv_ for local)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.value,
            onValueChange = {
                onConfigChanged(json.encodeToString(SetVariableConfig.serializer(), config.copy(value = it)))
            },
            label = { Text("Value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DeleteVariableConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DeleteVariableConfig>(configJson) }
            .getOrDefault(DeleteVariableConfig())
    }

    Column {
        OutlinedTextField(
            value = config.variableName,
            onValueChange = {
                onConfigChanged(json.encodeToString(DeleteVariableConfig.serializer(), config.copy(variableName = it)))
            },
            label = { Text("Variable name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
