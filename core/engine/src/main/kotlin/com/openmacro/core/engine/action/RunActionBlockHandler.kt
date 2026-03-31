package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.database.repository.ActionBlockRepository
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.buildActionTree
import com.openmacro.core.model.config.RunActionBlockConfig
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Executes a reusable action block (function-like invocation).
 *
 * Creates a child execution context with a fresh local variable scope.
 * Input parameters are copied from the caller's context into the block's scope.
 * After execution, output parameters are copied back to the caller's scope.
 * Global variables remain shared.
 */
class RunActionBlockHandler @Inject constructor(
    private val actionBlockRepository: ActionBlockRepository,
    private val actionExecutor: Lazy<ActionExecutor>,
) : FlowControlHandler {
    override val actionTypeId = "run_action_block"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val parsed = try {
            Json.decodeFromString<RunActionBlockConfig>(config.configJson)
        } catch (_: Exception) {
            RunActionBlockConfig()
        }

        if (parsed.blockId <= 0) {
            Log.w(TAG, "No block ID specified")
            return FlowResult.Continue
        }

        // Load the action block definition
        val block = actionBlockRepository.getById(parsed.blockId)
        if (block == null) {
            Log.w(TAG, "Action block ${parsed.blockId} not found")
            return FlowResult.Continue
        }

        // Load the block's actions
        val blockActions = actionBlockRepository.observeBlockActions(parsed.blockId).first()
        if (blockActions.isEmpty()) {
            Log.d(TAG, "Action block '${block.name}' has no actions")
            return FlowResult.Continue
        }

        // Create a child context with fresh local variables (scoped isolation)
        val childLocals = mutableMapOf<String, String>()

        // Copy input parameters
        for ((paramName, valueExpr) in parsed.inputMappings) {
            val localKey = "lv_$paramName"
            // Value could be a literal or a reference to caller's local variable
            val value = if (valueExpr.startsWith("lv_")) {
                context.localVariables[valueExpr] ?: ""
            } else {
                valueExpr // literal value (magic text already resolved)
            }
            childLocals[localKey] = value
        }

        val childContext = context.copy(
            localVariables = childLocals,
        )

        // Build tree and execute
        val blockTree = buildActionTree(blockActions)
        val result = actionExecutor.get().executeNodes(blockTree, childContext)

        // Copy output parameters back to caller's scope
        for ((paramName, targetVar) in parsed.outputMappings) {
            val localKey = "lv_$paramName"
            val value = childLocals[localKey] ?: ""
            if (targetVar.startsWith("lv_")) {
                context.localVariables[targetVar] = value
            } else {
                context.variableStore?.setGlobal(targetVar.removePrefix("v_"), value)
            }
        }

        Log.d(TAG, "Executed action block '${block.name}'")

        // CancelMacro should propagate up, but Break/Continue are consumed by the block
        return when (result) {
            is FlowResult.CancelMacro -> result
            else -> FlowResult.Continue
        }
    }

    companion object {
        private const val TAG = "RunActionBlock"
    }
}
