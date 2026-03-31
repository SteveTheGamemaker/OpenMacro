package com.openmacro.feature.macros.editor.action

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.openmacro.core.model.config.SetBrightnessConfig
import com.openmacro.core.model.config.ScreenOnOffActionConfig
import com.openmacro.core.model.config.ForceScreenRotationConfig
import com.openmacro.core.model.config.AutoRotateConfig
import com.openmacro.core.model.config.DarkThemeConfig
import com.openmacro.core.model.config.SetWallpaperConfig
import com.openmacro.core.model.config.KeepDeviceAwakeConfig
import com.openmacro.core.ui.components.MagicTextField
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun SetBrightnessConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SetBrightnessConfig>(configJson) }
            .getOrDefault(SetBrightnessConfig())
    }
    Column {
        SwitchRow("Auto brightness", config.autoMode) {
            onConfigChanged(json.encodeToString(config.copy(autoMode = it)))
        }
        if (!config.autoMode) {
            SliderWithLabel(
                label = "Brightness level",
                value = config.level.toFloat(),
                onValueChange = { onConfigChanged(json.encodeToString(config.copy(level = it.toInt()))) },
                valueRange = 0f..255f,
                valueText = "${(config.level * 100 / 255)}%",
            )
        }
    }
}

@Composable
fun ScreenOnOffActionConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ScreenOnOffActionConfig>(configJson) }
            .getOrDefault(ScreenOnOffActionConfig())
    }
    Column {
        SwitchRow("Turn screen on", config.turnOn) {
            onConfigChanged(json.encodeToString(config.copy(turnOn = it)))
        }
        if (!config.turnOn) {
            Text(
                "Requires Device Admin permission to lock screen",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
fun ForceScreenRotationConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ForceScreenRotationConfig>(configJson) }
            .getOrDefault(ForceScreenRotationConfig())
    }
    val rotations = listOf(0 to "Natural", 1 to "90°", 2 to "180°", 3 to "270°")
    Column {
        rotations.forEach { (value, label) ->
            SwitchRow(label, config.rotation == value) {
                if (it) onConfigChanged(json.encodeToString(config.copy(rotation = value)))
            }
        }
        Text(
            "Requires WRITE_SETTINGS permission",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
fun AutoRotateConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<AutoRotateConfig>(configJson) }
            .getOrDefault(AutoRotateConfig())
    }
    SwitchRow("Enable auto-rotate", config.enable) {
        onConfigChanged(json.encodeToString(config.copy(enable = it)))
    }
}

@Composable
fun DarkThemeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DarkThemeConfig>(configJson) }
            .getOrDefault(DarkThemeConfig())
    }
    SwitchRow("Enable dark theme", config.enable) {
        onConfigChanged(json.encodeToString(config.copy(enable = it)))
    }
}

@Composable
fun SetWallpaperConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SetWallpaperConfig>(configJson) }
            .getOrDefault(SetWallpaperConfig())
    }
    Column {
        MagicTextField(
            value = config.imagePath,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(imagePath = it))) },
            label = "Image path",
            modifier = Modifier.fillMaxWidth(),
        )
        val targets = listOf("home", "lock", "both")
        targets.forEach { target ->
            SwitchRow(target.replaceFirstChar { it.uppercase() }, config.target == target) {
                if (it) onConfigChanged(json.encodeToString(config.copy(target = target)))
            }
        }
    }
}

@Composable
fun KeepDeviceAwakeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<KeepDeviceAwakeConfig>(configJson) }
            .getOrDefault(KeepDeviceAwakeConfig())
    }
    Column {
        SwitchRow("Keep awake", config.enable) {
            onConfigChanged(json.encodeToString(config.copy(enable = it)))
        }
        if (config.enable) {
            SliderWithLabel(
                label = "Duration (0 = indefinite)",
                value = config.durationMs.toFloat(),
                onValueChange = { onConfigChanged(json.encodeToString(config.copy(durationMs = it.toLong()))) },
                valueRange = 0f..3600000f,
                valueText = if (config.durationMs == 0L) "Indefinite" else "${config.durationMs / 1000}s",
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
