package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.engine.expression.ExpressionEngine
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.config.IfClauseConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Flow control handler for If/Else logic.
 *
 * Children are split into "then" and "else" blocks by an ElseMarker action.
 * Actions before the ElseMarker are the "then" block; actions after are the "else" block.
 * If no ElseMarker is present, all children are the "then" block.
 *
 * The condition expression is evaluated using the ExpressionEngine.
 */
class IfClauseHandler @Inject constructor(
    private val expressionEngine: ExpressionEngine,
) : FlowControlHandler {
    override val actionTypeId = "if_clause"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val parsed = try {
            Json.decodeFromString<IfClauseConfig>(config.configJson)
        } catch (_: Exception) {
            IfClauseConfig()
        }

        val conditionMet = try {
            expressionEngine.evaluateBoolean(parsed.conditionExpression, context)
        } catch (e: Exception) {
            Log.w(TAG, "Expression error: ${e.message}, treating as false")
            false
        }
        Log.d(TAG, "If condition '${parsed.conditionExpression}' = $conditionMet")

        // Split children at the ElseMarker
        val elseIndex = children.indexOfFirst { it.action.typeId == "else_marker" }
        val thenBlock: List<ActionNode>
        val elseBlock: List<ActionNode>

        if (elseIndex >= 0) {
            thenBlock = children.subList(0, elseIndex)
            elseBlock = children.subList(elseIndex + 1, children.size)
        } else {
            thenBlock = children
            elseBlock = emptyList()
        }

        return if (conditionMet) {
            executeBlock(thenBlock)
        } else if (elseBlock.isNotEmpty()) {
            executeBlock(elseBlock)
        } else {
            FlowResult.Continue
        }
    }

    companion object {
        private const val TAG = "IfClauseHandler"
    }
}
