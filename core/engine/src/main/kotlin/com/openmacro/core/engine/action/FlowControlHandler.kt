package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult

/**
 * Extended handler interface for flow control actions (if/else, loops, etc.)
 * that need to execute child actions.
 *
 * The executor detects this interface at dispatch time and passes the action's
 * children plus an [executeBlock] callback so the handler can run nested actions
 * without holding a direct reference to the executor.
 */
interface FlowControlHandler : ActionHandler {

    /**
     * Execute this flow control action.
     *
     * @param config   The action's config (with resolved magic text)
     * @param context  The execution context (variables, trigger data, etc.)
     * @param children The child [ActionNode]s nested under this action
     * @param executeBlock Callback to execute a list of child nodes; returns
     *                     a [FlowResult] indicating how execution ended
     */
    suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult

    /**
     * Default [ActionHandler.execute] delegates to [executeFlow] with empty children.
     * This should never be called directly by the executor for flow control actions —
     * it exists only to satisfy the interface contract.
     */
    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        // No-op fallback; the executor calls executeFlow() instead
    }
}
