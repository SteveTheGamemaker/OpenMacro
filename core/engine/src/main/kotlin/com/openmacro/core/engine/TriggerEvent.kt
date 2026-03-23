package com.openmacro.core.engine

/**
 * Emitted by TriggerMonitors when a system event occurs.
 * The MacroDispatcher matches these to macros by triggerTypeId.
 */
data class TriggerEvent(
    val triggerTypeId: String,
    val data: Map<String, String> = emptyMap(),
)
