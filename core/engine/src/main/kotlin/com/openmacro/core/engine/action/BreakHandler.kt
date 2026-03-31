package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import javax.inject.Inject

/**
 * Signals the nearest enclosing loop to stop iterating.
 */
class BreakHandler @Inject constructor() : FlowControlHandler {
    override val actionTypeId = "break_loop"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult = FlowResult.Break
}
