package com.openmacro.core.engine.action

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of all available action handlers, keyed by actionTypeId.
 */
@Singleton
class ActionRegistry @Inject constructor(
    handlers: Set<@JvmSuppressWildcards ActionHandler>,
) {
    private val byTypeId: Map<String, ActionHandler> =
        handlers.associateBy { it.actionTypeId }

    fun get(typeId: String): ActionHandler? = byTypeId[typeId]
}
