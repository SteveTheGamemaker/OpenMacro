package com.openmacro.feature.macros.editor.constraint

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
import com.openmacro.core.model.config.LocationConstraintConfig
import com.openmacro.core.model.config.HeadphonesConstraintConfig
import com.openmacro.core.model.config.DoNotDisturbConstraintConfig
import com.openmacro.core.model.config.SilentModeConstraintConfig
import com.openmacro.core.ui.components.SliderWithLabel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun LocationConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<LocationConstraintConfig>(configJson) }
            .getOrDefault(LocationConstraintConfig())
    }
    Column {
        OutlinedTextField(
            value = config.locationName,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(locationName = it))) },
            label = { Text("Location name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = if (config.latitude != 0.0) config.latitude.toString() else "",
            onValueChange = { v -> v.toDoubleOrNull()?.let { onConfigChanged(json.encodeToString(config.copy(latitude = it))) } },
            label = { Text("Latitude") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = if (config.longitude != 0.0) config.longitude.toString() else "",
            onValueChange = { v -> v.toDoubleOrNull()?.let { onConfigChanged(json.encodeToString(config.copy(longitude = it))) } },
            label = { Text("Longitude") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        SliderWithLabel(
            label = "Radius",
            value = config.radiusMeters,
            onValueChange = { onConfigChanged(json.encodeToString(config.copy(radiusMeters = it))) },
            valueRange = 50f..5000f,
            valueText = "${config.radiusMeters.toInt()}m",
        )
    }
}

@Composable
fun HeadphonesConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<HeadphonesConstraintConfig>(configJson) }
            .getOrDefault(HeadphonesConstraintConfig())
    }
    SwitchRow("Headphones connected", config.connected) {
        onConfigChanged(json.encodeToString(config.copy(connected = it)))
    }
}

@Composable
fun DoNotDisturbConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<DoNotDisturbConstraintConfig>(configJson) }
            .getOrDefault(DoNotDisturbConstraintConfig())
    }
    SwitchRow("DND enabled", config.enabled) {
        onConfigChanged(json.encodeToString(config.copy(enabled = it)))
    }
}

@Composable
fun SilentModeConstraintEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<SilentModeConstraintConfig>(configJson) }
            .getOrDefault(SilentModeConstraintConfig())
    }
    SwitchRow("Silent mode", config.silent) {
        onConfigChanged(json.encodeToString(config.copy(silent = it)))
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
