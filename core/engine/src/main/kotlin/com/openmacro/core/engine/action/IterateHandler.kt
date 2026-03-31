package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.config.IterateConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Flow control handler that iterates over an array or dictionary variable.
 *
 * For arrays: sets keyVariable = index (0-based), valueVariable = item value.
 * For dictionaries: sets keyVariable = key, valueVariable = value.
 * Also sets lv_repeat_index = current iteration index.
 *
 * Respects Break/Continue/CancelMacro flow results.
 */
class IterateHandler @Inject constructor() : FlowControlHandler {
    override val actionTypeId = "iterate"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val parsed = try {
            Json.decodeFromString<IterateConfig>(config.configJson)
        } catch (_: Exception) {
            IterateConfig()
        }

        val varName = parsed.variableName
        if (varName.isBlank()) return FlowResult.Continue

        // Get the variable value
        val rawValue = if (varName.startsWith("lv_")) {
            context.localVariables[varName]
        } else {
            context.variableStore?.getGlobal(varName.removePrefix("v_"))
        } ?: return FlowResult.Continue

        // Try to parse as JSON array or object
        return try {
            val jsonElement = Json.parseToJsonElement(rawValue)
            when (jsonElement) {
                is JsonArray -> iterateArray(jsonElement, parsed, context, children, executeBlock)
                is JsonObject -> iterateObject(jsonElement, parsed, context, children, executeBlock)
                else -> {
                    Log.w(TAG, "Variable '$varName' is not an array or dictionary")
                    FlowResult.Continue
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse variable '$varName' for iteration: ${e.message}")
            FlowResult.Continue
        }
    }

    private suspend fun iterateArray(
        array: JsonArray,
        config: IterateConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        Log.d(TAG, "Iterating array, size=${array.size}")
        for ((index, element) in array.withIndex()) {
            context.localVariables[config.keyVariable] = index.toString()
            context.localVariables[config.valueVariable] = elementToString(element)
            context.localVariables["lv_repeat_index"] = index.toString()

            val result = executeBlock(children)
            when (result) {
                is FlowResult.Break -> return FlowResult.Continue
                is FlowResult.CancelMacro -> return result
                is FlowResult.ContinueLoop -> continue
                is FlowResult.Continue -> { /* next iteration */ }
            }
        }
        return FlowResult.Continue
    }

    private suspend fun iterateObject(
        obj: JsonObject,
        config: IterateConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        Log.d(TAG, "Iterating dictionary, size=${obj.size}")
        for ((index, entry) in obj.entries.withIndex()) {
            context.localVariables[config.keyVariable] = entry.key
            context.localVariables[config.valueVariable] = elementToString(entry.value)
            context.localVariables["lv_repeat_index"] = index.toString()

            val result = executeBlock(children)
            when (result) {
                is FlowResult.Break -> return FlowResult.Continue
                is FlowResult.CancelMacro -> return result
                is FlowResult.ContinueLoop -> continue
                is FlowResult.Continue -> { /* next iteration */ }
            }
        }
        return FlowResult.Continue
    }

    private fun elementToString(element: kotlinx.serialization.json.JsonElement): String {
        return when (element) {
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    }

    companion object {
        private const val TAG = "IterateHandler"
    }
}
