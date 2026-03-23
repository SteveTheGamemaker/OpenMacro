package com.openmacro.core.engine

import android.content.Context
import com.openmacro.core.engine.variable.VariableStore

/**
 * Context passed to action handlers during macro execution.
 * Carries the Android context, trigger event data, variable stores,
 * and the log ID for status updates.
 */
data class ExecutionContext(
    val androidContext: Context,
    val triggerEvent: TriggerEvent,
    val logId: Long,
    val macroId: Long,
    val macroName: String,
    val variableStore: VariableStore? = null,
    val localVariables: MutableMap<String, String> = mutableMapOf(),
)
