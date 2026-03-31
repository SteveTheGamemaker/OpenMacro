package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import javax.inject.Inject

/**
 * Stops all remaining actions in the macro immediately.
 */
class CancelMacroHandler @Inject constructor() : FlowControlHandler {
    override val actionTypeId = "cancel_macro"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult = FlowResult.CancelMacro
}
