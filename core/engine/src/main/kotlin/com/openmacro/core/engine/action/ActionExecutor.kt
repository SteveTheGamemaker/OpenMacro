package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.engine.text.MagicTextResolver
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.buildActionTree
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes actions by walking an action tree. Supports nested flow control
 * (if/else, loops) via [FlowControlHandler] and propagates [FlowResult]
 * signals for break/continue/cancel.
 *
 * For backward compatibility, the flat-list [execute] overload builds the
 * tree automatically, so existing callers (MacroDispatcher) need no changes.
 */
@Singleton
class ActionExecutor @Inject constructor(
    private val registry: ActionRegistry,
    private val magicTextResolver: MagicTextResolver,
) {
    /**
     * Execute a flat list of actions (backward-compatible entry point).
     * Builds the action tree from parentActionId relationships and walks it.
     */
    suspend fun execute(actions: List<ActionConfig>, context: ExecutionContext) {
        val tree = buildActionTree(actions)
        executeNodes(tree, context)
    }

    /**
     * Execute a tree of action nodes. Returns a [FlowResult] so that
     * flow control handlers can inspect how a child block completed.
     */
    suspend fun executeNodes(
        nodes: List<ActionNode>,
        context: ExecutionContext,
    ): FlowResult {
        for (node in nodes) {
            if (!node.action.isEnabled) continue

            val result = executeNode(node, context)
            when (result) {
                is FlowResult.Continue -> { /* keep going */ }
                is FlowResult.Break,
                is FlowResult.ContinueLoop,
                is FlowResult.CancelMacro -> return result
            }
        }
        return FlowResult.Continue
    }

    private suspend fun executeNode(
        node: ActionNode,
        context: ExecutionContext,
    ): FlowResult {
        val action = node.action
        val handler = registry.get(action.typeId)
        if (handler == null) {
            Log.w(TAG, "No handler for action type: ${action.typeId}")
            return FlowResult.Continue
        }

        // Resolve magic text tokens in the config JSON before execution
        val resolvedAction = action.copy(
            configJson = magicTextResolver.resolve(action.configJson, context),
        )
        val resolvedNode = node.copy(action = resolvedAction)

        return if (handler is FlowControlHandler) {
            handler.executeFlow(
                config = resolvedAction,
                context = context,
                children = resolvedNode.children,
                executeBlock = { childNodes -> executeNodes(childNodes, context) },
            )
        } else {
            handler.execute(resolvedAction, context)
            FlowResult.Continue
        }
    }

    companion object {
        private const val TAG = "ActionExecutor"
    }
}
