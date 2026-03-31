package com.openmacro.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A text field with a magic text insert button that opens the token picker.
 * Use this instead of OutlinedTextField when the field supports magic text tokens.
 *
 * Reads trigger types and user variables from CompositionLocals automatically.
 */
@Composable
fun MagicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    placeholder: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val userVariables = LocalUserVariables.current
    val triggerTypeIds = LocalTriggerTypeIds.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        maxLines = maxLines,
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.DataObject, contentDescription = "Insert magic text")
            }
        },
        modifier = modifier.fillMaxWidth(),
    )

    if (showPicker) {
        MagicTextPickerSheet(
            triggerTypeIds = triggerTypeIds,
            userVariables = userVariables,
            onTokenSelected = { token ->
                showPicker = false
                onValueChange(value + token)
            },
            onDismiss = { showPicker = false },
        )
    }
}
