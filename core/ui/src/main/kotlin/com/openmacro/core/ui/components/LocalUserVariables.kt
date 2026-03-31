package com.openmacro.core.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * Provides user-defined variables (name to type) to MagicTextField and MagicTextPickerSheet
 * without threading the parameter through every config editor.
 */
val LocalUserVariables = compositionLocalOf<List<Pair<String, String>>> { emptyList() }

/**
 * Provides the macro's trigger type IDs so MagicTextField can show relevant
 * trigger-specific tokens first (e.g. SMS sender/message for sms_received).
 */
val LocalTriggerTypeIds = compositionLocalOf<List<String>> { emptyList() }
