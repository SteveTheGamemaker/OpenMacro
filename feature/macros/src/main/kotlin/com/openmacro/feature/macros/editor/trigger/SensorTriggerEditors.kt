package com.openmacro.feature.macros.editor.trigger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.ShakeDeviceConfig
import com.openmacro.core.model.config.FlipDeviceConfig
import com.openmacro.core.model.config.ProximitySensorConfig
import com.openmacro.core.model.config.LightSensorConfig
import com.openmacro.core.model.config.ScreenOrientationConfig
import com.openmacro.core.model.config.ActivityRecognitionConfig
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun ShakeDeviceConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ShakeDeviceConfig>(configJson) }
            .getOrDefault(ShakeDeviceConfig())
    }
    Column {
        SliderWithLabel(
            label = "Sensitivity",
            value = config.sensitivity,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(sensitivity = it))) },
            valueRange = 5f..25f,
            valueText = "${config.sensitivity.toInt()} m/s²",
        )
        SliderWithLabel(
            label = "Cooldown",
            value = config.shakeDurationMs.toFloat(),
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(shakeDurationMs = it.toLong()))) },
            valueRange = 200f..3000f,
            valueText = "${config.shakeDurationMs}ms",
        )
    }
}

@Composable
fun FlipDeviceConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<FlipDeviceConfig>(configJson) }
            .getOrDefault(FlipDeviceConfig())
    }
    Column {
        SwitchRow("On face down", config.onFaceDown) {
            onConfigChanged(json.encodeToString(config.copy(onFaceDown = it)))
        }
        SwitchRow("On face up", config.onFaceUp) {
            onConfigChanged(json.encodeToString(config.copy(onFaceUp = it)))
        }
    }
}

@Composable
fun ProximitySensorConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ProximitySensorConfig>(configJson) }
            .getOrDefault(ProximitySensorConfig())
    }
    Column {
        SwitchRow("On near", config.onNear) {
            onConfigChanged(json.encodeToString(config.copy(onNear = it)))
        }
        SwitchRow("On far", config.onFar) {
            onConfigChanged(json.encodeToString(config.copy(onFar = it)))
        }
    }
}

@Composable
fun LightSensorConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<LightSensorConfig>(configJson) }
            .getOrDefault(LightSensorConfig())
    }
    Column {
        SliderWithLabel(
            label = "Threshold",
            value = config.threshold,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(threshold = it))) },
            valueRange = 0f..1000f,
            valueText = "${config.threshold.toInt()} lux",
        )
        SwitchRow("Trigger when below threshold", config.whenBelow) {
            onConfigChanged(json.encodeToString(config.copy(whenBelow = it)))
        }
    }
}

@Composable
fun ScreenOrientationConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ScreenOrientationConfig>(configJson) }
            .getOrDefault(ScreenOrientationConfig())
    }
    Column {
        val options = listOf("portrait", "landscape")
        options.forEach { option ->
            SwitchRow(option.replaceFirstChar { it.uppercase() }, config.orientation == option) {
                if (it) onConfigChanged(json.encodeToString(config.copy(orientation = option)))
            }
        }
    }
}

@Composable
fun ActivityRecognitionConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<ActivityRecognitionConfig>(configJson) }
            .getOrDefault(ActivityRecognitionConfig())
    }
    Column {
        Text("Activity type", style = MaterialTheme.typography.bodySmall)
        val activities = listOf("still", "walking", "running", "cycling", "driving")
        activities.forEach { activity ->
            SwitchRow(activity.replaceFirstChar { it.uppercase() }, config.activityType == activity) {
                if (it) onConfigChanged(json.encodeToString(config.copy(activityType = activity)))
            }
        }
        SliderWithLabel(
            label = "Confidence threshold",
            value = config.confidenceThreshold.toFloat(),
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(confidenceThreshold = it.toInt()))) },
            valueRange = 25f..100f,
            valueText = "${config.confidenceThreshold}%",
        )
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
