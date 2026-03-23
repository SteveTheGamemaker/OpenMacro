package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig

/**
 * Base interface for all action handlers. Each implementation knows how to
 * execute one type of action given its config JSON.
 */
interface ActionHandler {
    val actionTypeId: String

    suspend fun execute(config: ActionConfig, context: ExecutionContext)
}
