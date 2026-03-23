package com.openmacro.core.engine

import android.content.Context

/**
 * Context passed to action handlers during macro execution.
 * Carries the Android context, trigger event data, and the log ID for status updates.
 */
data class ExecutionContext(
    val androidContext: Context,
    val triggerEvent: TriggerEvent,
    val logId: Long,
    val macroId: Long,
    val macroName: String,
)
