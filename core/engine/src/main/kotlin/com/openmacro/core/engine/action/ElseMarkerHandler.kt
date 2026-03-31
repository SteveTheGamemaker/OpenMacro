package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import javax.inject.Inject

/**
 * No-op handler that serves as a visual separator between "then" and "else"
 * blocks inside an IfClause. Never executed directly — the IfClauseHandler
 * uses it as a split point when processing its children.
 */
class ElseMarkerHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "else_marker"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        // No-op — this action is only a structural marker
    }
}
