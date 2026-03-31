package com.openmacro.feature.macros.editor.trigger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.BatteryTemperatureTriggerConfig
import com.openmacro.core.model.config.BatterySaverStateConfig
import com.openmacro.core.model.config.DarkThemeChangeConfig
import com.openmacro.core.model.config.GpsEnabledDisabledConfig
import com.openmacro.core.model.config.DoNotDisturbConfig
import com.openmacro.core.model.config.SilentModeConfig
import com.openmacro.core.model.config.TorchOnOffConfig
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun BatteryTemperatureConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BatteryTemperatureTriggerConfig>(configJson) }
            .getOrDefault(BatteryTemperatureTriggerConfig())
    }
    Column {
        SliderWithLabel(
            label = "Temperature threshold",
            value = config.threshold,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(threshold = it))) },
            valueRange = 20f..60f,
            valueText = "${config.threshold.toInt()}°C",
        )
        SwitchRow("Trigger when above threshold", config.whenAbove) {
            onConfigChanged(json.encodeToString(config.copy(whenAbove = it)))
        }
    }
}

@Composable
fun BatterySaverStateConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<BatterySaverStateConfig>(configJson) }
            .getOrDefault(BatterySaverStateConfig())
    }
    Column {
        SwitchRow("On enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(config.copy(onEnabled = it)))
        }
        SwitchRow("On disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(config.copy(onDisabled = it)))
        }
    }
}

@Composable
fun DarkThemeChangeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DarkThemeChangeConfig>(configJson) }
            .getOrDefault(DarkThemeChangeConfig())
    }
    Column {
        SwitchRow("On dark mode enabled", config.onDarkEnabled) {
            onConfigChanged(json.encodeToString(config.copy(onDarkEnabled = it)))
        }
        SwitchRow("On dark mode disabled", config.onDarkDisabled) {
            onConfigChanged(json.encodeToString(config.copy(onDarkDisabled = it)))
        }
    }
}

@Composable
fun GpsEnabledDisabledConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<GpsEnabledDisabledConfig>(configJson) }
            .getOrDefault(GpsEnabledDisabledConfig())
    }
    Column {
        SwitchRow("On enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(config.copy(onEnabled = it)))
        }
        SwitchRow("On disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(config.copy(onDisabled = it)))
        }
    }
}

@Composable
fun DoNotDisturbConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DoNotDisturbConfig>(configJson) }
            .getOrDefault(DoNotDisturbConfig())
    }
    Column {
        SwitchRow("On enabled", config.onEnabled) {
            onConfigChanged(json.encodeToString(config.copy(onEnabled = it)))
        }
        SwitchRow("On disabled", config.onDisabled) {
            onConfigChanged(json.encodeToString(config.copy(onDisabled = it)))
        }
    }
}

@Composable
fun SilentModeConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SilentModeConfig>(configJson) }
            .getOrDefault(SilentModeConfig())
    }
    Column {
        SwitchRow("On silent/vibrate", config.onSilent) {
            onConfigChanged(json.encodeToString(config.copy(onSilent = it)))
        }
        SwitchRow("On normal", config.onNormal) {
            onConfigChanged(json.encodeToString(config.copy(onNormal = it)))
        }
    }
}

@Composable
fun TorchOnOffConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<TorchOnOffConfig>(configJson) }
            .getOrDefault(TorchOnOffConfig())
    }
    Column {
        SwitchRow("On torch on", config.onTorchOn) {
            onConfigChanged(json.encodeToString(config.copy(onTorchOn = it)))
        }
        SwitchRow("On torch off", config.onTorchOff) {
            onConfigChanged(json.encodeToString(config.copy(onTorchOff = it)))
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
