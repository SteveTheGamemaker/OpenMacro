package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.engine.expression.ExpressionEngine
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.config.RepeatConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Flow control handler for repeat/loop actions.
 *
 * Modes:
 * - "count": Execute children a fixed number of times.
 * - "count_expression": Evaluate expression for iteration count.
 * - "while": Execute children while an expression evaluates to true.
 *
 * Sets lv_repeat_index (0-based) on each iteration.
 * Respects Break (exit loop) and ContinueLoop (skip to next iteration).
 * CancelMacro propagates out immediately.
 */
class RepeatHandler @Inject constructor(
    private val expressionEngine: ExpressionEngine,
) : FlowControlHandler {
    override val actionTypeId = "repeat"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val parsed = try {
            Json.decodeFromString<RepeatConfig>(config.configJson)
        } catch (_: Exception) {
            RepeatConfig()
        }

        return when (parsed.mode) {
            "while" -> executeWhile(parsed, context, children, executeBlock)
            "count_expression" -> executeCountExpression(parsed, context, children, executeBlock)
            else -> executeCount(parsed, context, children, executeBlock)
        }
    }

    private suspend fun executeCount(
        config: RepeatConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        Log.d(TAG, "Repeat count=${config.count}")
        return runLoop(config.count, context, children, executeBlock)
    }

    private suspend fun executeCountExpression(
        config: RepeatConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val count = try {
            expressionEngine.evaluate(config.countExpression, context).toNumber().toInt()
        } catch (e: Exception) {
            Log.w(TAG, "Count expression error: ${e.message}, using count=${config.count}")
            config.count
        }
        Log.d(TAG, "Repeat count_expression='${config.countExpression}' = $count")
        return runLoop(count, context, children, executeBlock)
    }

    private suspend fun runLoop(
        count: Int,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        for (i in 0 until count) {
            context.localVariables["lv_repeat_index"] = i.toString()
            val result = executeBlock(children)
            when (result) {
                is FlowResult.Break -> return FlowResult.Continue // break exits loop normally
                is FlowResult.CancelMacro -> return result
                is FlowResult.ContinueLoop -> continue
                is FlowResult.Continue -> { /* next iteration */ }
            }
        }
        return FlowResult.Continue
    }

    private suspend fun executeWhile(
        config: RepeatConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        var index = 0
        val maxIterations = 10_000 // safety limit

        while (index < maxIterations) {
            val conditionMet = try {
                expressionEngine.evaluateBoolean(config.whileExpression, context)
            } catch (e: Exception) {
                Log.w(TAG, "While expression error: ${e.message}, stopping loop")
                false
            }
            if (!conditionMet) break

            context.localVariables["lv_repeat_index"] = index.toString()
            val result = executeBlock(children)
            when (result) {
                is FlowResult.Break -> return FlowResult.Continue
                is FlowResult.CancelMacro -> return result
                is FlowResult.ContinueLoop -> { index++; continue }
                is FlowResult.Continue -> { /* next iteration */ }
            }
            index++
        }
        return FlowResult.Continue
    }

    companion object {
        private const val TAG = "RepeatHandler"
    }
}
