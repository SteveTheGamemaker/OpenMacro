package com.openmacro.feature.actionblocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openmacro.core.model.ActionType
import com.openmacro.core.ui.components.ConfigCard
import com.openmacro.core.ui.components.LocalUserVariables
import com.openmacro.feature.macros.editor.AVAILABLE_ACTIONS
import com.openmacro.feature.macros.editor.ActionConfigContent
import com.openmacro.feature.macros.editor.AddButton
import com.openmacro.feature.macros.editor.SectionHeader
import com.openmacro.feature.macros.editor.TypePickerSheet
import com.openmacro.feature.macros.editor.actionIcon
import com.openmacro.feature.macros.editor.isContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBlockEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActionBlockEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userVariables by viewModel.userVariables.collectAsStateWithLifecycle()
    var showActionPicker by remember { mutableStateOf(false) }
    var actionPickerParentId by remember { mutableStateOf<Long?>(null) }
    var newInputParam by remember { mutableStateOf("") }
    var newOutputParam by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    CompositionLocalProvider(LocalUserVariables provides userVariables) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.blockId != null) "Edit Action Block" else "New Action Block") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.name.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Name
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::setName,
                    label = { Text("Block name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::setDescription,
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Input parameters
            item {
                Text("Input Parameters", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ParamChipList(
                    params = uiState.inputParams,
                    onRemove = viewModel::removeInputParam,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newInputParam,
                        onValueChange = { newInputParam = it },
                        label = { Text("Variable name") },
                        placeholder = { Text("e.g. input_text") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            viewModel.addInputParam(newInputParam)
                            newInputParam = ""
                        },
                        enabled = newInputParam.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }

            // Output parameters
            item {
                Text("Output Parameters", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ParamChipList(
                    params = uiState.outputParams,
                    onRemove = viewModel::removeOutputParam,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newOutputParam,
                        onValueChange = { newOutputParam = it },
                        label = { Text("Variable name") },
                        placeholder = { Text("e.g. result") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            viewModel.addOutputParam(newOutputParam)
                            newOutputParam = ""
                        },
                        enabled = newOutputParam.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }

            // Actions section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Actions", uiState.actions.size)
            }
            val displayItems = uiState.actionDisplayItems
            items(displayItems.size) { idx ->
                val item = displayItems[idx]
                val action = item.action
                val isElseMarker = action.typeId == ActionType.ELSE_MARKER.typeId
                val isContainer = action.isContainer()

                Column(
                    modifier = Modifier.padding(start = (item.depth * 24).dp),
                ) {
                    if (isElseMarker) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "\u2014 Else \u2014",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        ConfigCard(
                            title = action.type?.displayName ?: action.typeId,
                            icon = actionIcon(action.typeId),
                            onRemove = { viewModel.removeAction(action.id) },
                        ) {
                            ActionConfigContent(
                                typeId = action.typeId,
                                configJson = action.configJson,
                                onConfigChanged = { viewModel.updateActionConfigById(action.id, it) },
                            )

                            if (isContainer) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AddButton("Add Action Inside") {
                                    actionPickerParentId = action.id
                                    showActionPicker = true
                                }
                                if (action.typeId == ActionType.IF_CLAUSE.typeId) {
                                    val hasElse = uiState.actions.any {
                                        it.parentActionId == action.id &&
                                            it.typeId == ActionType.ELSE_MARKER.typeId
                                    }
                                    if (!hasElse) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AddButton("Add Else") {
                                            viewModel.addElseMarker(action.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                AddButton("Add Action") {
                    actionPickerParentId = null
                    showActionPicker = true
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showActionPicker) {
        TypePickerSheet(
            title = "Add Action",
            types = AVAILABLE_ACTIONS,
            onTypeSelected = { typeId ->
                showActionPicker = false
                ActionType.fromTypeId(typeId)?.let {
                    viewModel.addAction(it, parentActionId = actionPickerParentId)
                }
                actionPickerParentId = null
            },
            onDismiss = {
                showActionPicker = false
                actionPickerParentId = null
            },
        )
    }
    } // CompositionLocalProvider
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParamChipList(
    params: List<String>,
    onRemove: (Int) -> Unit,
) {
    if (params.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        params.forEachIndexed { index, param ->
            InputChip(
                selected = false,
                onClick = { onRemove(index) },
                label = { Text(param) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.padding(0.dp),
                    )
                },
            )
        }
    }
}
