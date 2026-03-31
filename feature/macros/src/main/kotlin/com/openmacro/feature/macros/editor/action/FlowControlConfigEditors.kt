package com.openmacro.feature.macros.editor.action

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openmacro.core.model.config.ArrayManipulationConfig
import com.openmacro.core.model.config.IfClauseConfig
import com.openmacro.core.model.config.IterateConfig
import com.openmacro.core.model.config.JsonParseConfig
import com.openmacro.core.model.config.RepeatConfig
import com.openmacro.core.model.config.RunActionBlockConfig
import com.openmacro.core.model.config.TextManipulationConfig
import com.openmacro.core.ui.components.MagicTextField
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Composable
fun IfClauseConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<IfClauseConfig>(configJson) }
        catch (_: Exception) { IfClauseConfig() }
    }

    Column {
        MagicTextField(
            value = config.conditionExpression,
            onValueChange = {
                onConfigChanged(json.encodeToString(IfClauseConfig.serializer(), config.copy(conditionExpression = it)))
            },
            label = "Condition expression",
            placeholder = "e.g. v_counter > 5",
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Use expressions like: v_name == \"hello\", lv_count > 10, true",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<RepeatConfig>(configJson) }
        catch (_: Exception) { RepeatConfig() }
    }

    var modeExpanded by remember { mutableStateOf(false) }
    val modes = listOf("count" to "Fixed Count", "count_expression" to "Count Expression", "while" to "While")
    val modeName = modes.firstOrNull { it.first == config.mode }?.second ?: config.mode

    Column {
        ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = it }) {
            OutlinedTextField(
                value = modeName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Mode") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                modes.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            modeExpanded = false
                            onConfigChanged(json.encodeToString(RepeatConfig.serializer(), config.copy(mode = id)))
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (config.mode) {
            "count" -> {
                OutlinedTextField(
                    value = config.count.toString(),
                    onValueChange = {
                        val count = it.toIntOrNull() ?: return@OutlinedTextField
                        onConfigChanged(json.encodeToString(RepeatConfig.serializer(), config.copy(count = count)))
                    },
                    label = { Text("Count") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "count_expression" -> {
                MagicTextField(
                    value = config.countExpression,
                    onValueChange = {
                        onConfigChanged(json.encodeToString(RepeatConfig.serializer(), config.copy(countExpression = it)))
                    },
                    label = "Count expression",
                    placeholder = "e.g. v_items_count",
                )
            }
            "while" -> {
                MagicTextField(
                    value = config.whileExpression,
                    onValueChange = {
                        onConfigChanged(json.encodeToString(RepeatConfig.serializer(), config.copy(whileExpression = it)))
                    },
                    label = "While expression",
                    placeholder = "e.g. lv_repeat_index < 10",
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Loop index available as {lv_repeat_index}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun IterateConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<IterateConfig>(configJson) }
        catch (_: Exception) { IterateConfig() }
    }

    Column {
        OutlinedTextField(
            value = config.variableName,
            onValueChange = {
                onConfigChanged(json.encodeToString(IterateConfig.serializer(), config.copy(variableName = it)))
            },
            label = { Text("Variable to iterate") },
            placeholder = { Text("e.g. v_my_list") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = config.keyVariable,
                onValueChange = {
                    onConfigChanged(json.encodeToString(IterateConfig.serializer(), config.copy(keyVariable = it)))
                },
                label = { Text("Key variable") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = config.valueVariable,
                onValueChange = {
                    onConfigChanged(json.encodeToString(IterateConfig.serializer(), config.copy(valueVariable = it)))
                },
                label = { Text("Value variable") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrayManipulationConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<ArrayManipulationConfig>(configJson) }
        catch (_: Exception) { ArrayManipulationConfig() }
    }

    var opExpanded by remember { mutableStateOf(false) }
    val operations = listOf("push", "pop", "insert", "remove", "get", "set", "sort", "reverse", "clear", "size")

    Column {
        OutlinedTextField(
            value = config.variableName,
            onValueChange = {
                onConfigChanged(json.encodeToString(ArrayManipulationConfig.serializer(), config.copy(variableName = it)))
            },
            label = { Text("Array variable") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = opExpanded, onExpandedChange = { opExpanded = it }) {
            OutlinedTextField(
                value = config.operation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Operation") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = opExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = opExpanded, onDismissRequest = { opExpanded = false }) {
                operations.forEach { op ->
                    DropdownMenuItem(
                        text = { Text(op) },
                        onClick = {
                            opExpanded = false
                            onConfigChanged(json.encodeToString(ArrayManipulationConfig.serializer(), config.copy(operation = op)))
                        },
                    )
                }
            }
        }
        if (config.operation in listOf("push", "insert", "set")) {
            Spacer(modifier = Modifier.height(8.dp))
            MagicTextField(
                value = config.value,
                onValueChange = {
                    onConfigChanged(json.encodeToString(ArrayManipulationConfig.serializer(), config.copy(value = it)))
                },
                label = "Value",
            )
        }
        if (config.operation in listOf("insert", "remove", "get", "set")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.index.toString(),
                onValueChange = {
                    val idx = it.toIntOrNull() ?: return@OutlinedTextField
                    onConfigChanged(json.encodeToString(ArrayManipulationConfig.serializer(), config.copy(index = idx)))
                },
                label = { Text("Index") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (config.operation in listOf("pop", "get", "size")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.resultVariable,
                onValueChange = {
                    onConfigChanged(json.encodeToString(ArrayManipulationConfig.serializer(), config.copy(resultVariable = it)))
                },
                label = { Text("Store result in") },
                placeholder = { Text("e.g. lv_result") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun JsonParseConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<JsonParseConfig>(configJson) }
        catch (_: Exception) { JsonParseConfig() }
    }

    Column {
        MagicTextField(
            value = config.jsonSource,
            onValueChange = {
                onConfigChanged(json.encodeToString(JsonParseConfig.serializer(), config.copy(jsonSource = it)))
            },
            label = "JSON source",
            placeholder = "JSON string or {variable}",
            singleLine = false,
            maxLines = 3,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.jsonPath,
            onValueChange = {
                onConfigChanged(json.encodeToString(JsonParseConfig.serializer(), config.copy(jsonPath = it)))
            },
            label = { Text("Path (dot notation)") },
            placeholder = { Text("e.g. data.items.0.name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.resultVariable,
            onValueChange = {
                onConfigChanged(json.encodeToString(JsonParseConfig.serializer(), config.copy(resultVariable = it)))
            },
            label = { Text("Store result in") },
            placeholder = { Text("e.g. lv_result") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextManipulationConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<TextManipulationConfig>(configJson) }
        catch (_: Exception) { TextManipulationConfig() }
    }

    var opExpanded by remember { mutableStateOf(false) }
    val operations = listOf("replace", "substring", "split", "join", "trim", "uppercase", "lowercase", "regex_extract", "length", "indexOf", "contains")

    Column {
        ExposedDropdownMenuBox(expanded = opExpanded, onExpandedChange = { opExpanded = it }) {
            OutlinedTextField(
                value = config.operation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Operation") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = opExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = opExpanded, onDismissRequest = { opExpanded = false }) {
                operations.forEach { op ->
                    DropdownMenuItem(
                        text = { Text(op) },
                        onClick = {
                            opExpanded = false
                            onConfigChanged(json.encodeToString(TextManipulationConfig.serializer(), config.copy(operation = op)))
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MagicTextField(
            value = config.input,
            onValueChange = {
                onConfigChanged(json.encodeToString(TextManipulationConfig.serializer(), config.copy(input = it)))
            },
            label = "Input text",
            singleLine = false,
            maxLines = 3,
        )
        if (config.operation in listOf("replace", "split", "substring", "regex_extract", "indexOf", "contains")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.param1,
                onValueChange = {
                    onConfigChanged(json.encodeToString(TextManipulationConfig.serializer(), config.copy(param1 = it)))
                },
                label = { Text(if (config.operation == "substring") "Start index" else "Pattern / Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (config.operation in listOf("replace", "substring", "regex_extract", "join")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.param2,
                onValueChange = {
                    onConfigChanged(json.encodeToString(TextManipulationConfig.serializer(), config.copy(param2 = it)))
                },
                label = { Text(if (config.operation == "substring") "End index" else if (config.operation == "join") "Delimiter" else "Replacement / Group") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = config.resultVariable,
            onValueChange = {
                onConfigChanged(json.encodeToString(TextManipulationConfig.serializer(), config.copy(resultVariable = it)))
            },
            label = { Text("Store result in") },
            placeholder = { Text("e.g. lv_result") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun RunActionBlockConfigEditor(configJson: String, onConfigChanged: (String) -> Unit) {
    val config = remember(configJson) {
        try { json.decodeFromString<RunActionBlockConfig>(configJson) }
        catch (_: Exception) { RunActionBlockConfig() }
    }

    Column {
        OutlinedTextField(
            value = if (config.blockId > 0) config.blockId.toString() else "",
            onValueChange = {
                val id = it.toLongOrNull() ?: 0
                onConfigChanged(json.encodeToString(RunActionBlockConfig.serializer(), config.copy(blockId = id)))
            },
            label = { Text("Action Block ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Enter the ID of an action block to run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
