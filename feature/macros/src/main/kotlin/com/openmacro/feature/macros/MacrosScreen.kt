package com.openmacro.feature.macros

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openmacro.core.engine.PermissionHelper
import com.openmacro.core.model.MacroWithDetails

/**
 * Collects all dangerous runtime permissions needed by a macro's triggers, actions, and constraints.
 */
private fun collectRequiredPermissions(macro: MacroWithDetails): List<String> {
    val permissions = mutableSetOf<String>()
    for (trigger in macro.triggers) {
        permissions.addAll(PermissionHelper.triggerPermissions(trigger.typeId))
    }
    for (action in macro.actions) {
        permissions.addAll(PermissionHelper.actionPermissions(action.typeId))
    }
    for (constraint in macro.constraints) {
        permissions.addAll(PermissionHelper.constraintPermissions(constraint.typeId))
    }
    return permissions.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacrosScreen(
    onCreateMacro: () -> Unit = {},
    onEditMacro: (Long) -> Unit = {},
    viewModel: MacrosViewModel = hiltViewModel(),
) {
    val macros by viewModel.macros.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Track which macro we're trying to enable (pending permission grant)
    var pendingEnableMacroId by remember { mutableStateOf<Long?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Enable the macro regardless of permission results — the user saw the dialog.
        // Denied permissions will cause individual triggers/actions to fail gracefully.
        pendingEnableMacroId?.let { macroId ->
            viewModel.toggleMacro(macroId, true, context)
        }
        pendingEnableMacroId = null
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Macros") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateMacro) {
                Icon(Icons.Default.Add, contentDescription = "Create macro")
            }
        },
    ) { innerPadding ->
        if (macros.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No macros yet",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to create your first macro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(macros, key = { it.macro.id }) { macro ->
                    SwipeToDismissMacroItem(
                        macro = macro,
                        onClick = { onEditMacro(macro.macro.id) },
                        onToggle = { enabled ->
                            if (enabled) {
                                val needed = collectRequiredPermissions(macro)
                                val missing = needed.filter {
                                    ContextCompat.checkSelfPermission(context, it) !=
                                        PackageManager.PERMISSION_GRANTED
                                }
                                if (missing.isNotEmpty()) {
                                    pendingEnableMacroId = macro.macro.id
                                    permissionLauncher.launch(missing.toTypedArray())
                                } else {
                                    viewModel.toggleMacro(macro.macro.id, true, context)
                                }
                            } else {
                                viewModel.toggleMacro(macro.macro.id, false, context)
                            }
                        },
                        onDelete = { viewModel.deleteMacro(macro.macro.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissMacroItem(
    macro: MacroWithDetails,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "swipe-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        MacroItem(
            macro = macro,
            onClick = onClick,
            onToggle = onToggle,
        )
    }
}

@Composable
private fun MacroItem(
    macro: MacroWithDetails,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = macro.macro.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${macro.triggers.size} trigger(s), ${macro.actions.size} action(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = macro.macro.isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}
