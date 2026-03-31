package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.VariableType
import com.openmacro.core.model.config.ArrayManipulationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Manipulates array variables (push, pop, insert, remove, sort, reverse, clear, size, get, set).
 */
class ArrayManipulationHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "array_manipulation"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = try {
            Json.decodeFromString<ArrayManipulationConfig>(config.configJson)
        } catch (_: Exception) {
            ArrayManipulationConfig()
        }

        val varName = parsed.variableName
        if (varName.isBlank()) return

        val isLocal = varName.startsWith("lv_")
        val rawValue = if (isLocal) {
            context.localVariables[varName] ?: "[]"
        } else {
            context.variableStore?.getGlobal(varName.removePrefix("v_")) ?: "[]"
        }

        val items = try {
            val arr = Json.parseToJsonElement(rawValue) as? JsonArray
            arr?.map { it.jsonPrimitive.content }?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }

        var resultValue: String? = null

        when (parsed.operation) {
            "push" -> items.add(parsed.value)
            "pop" -> {
                resultValue = if (items.isNotEmpty()) items.removeAt(items.size - 1) else ""
            }
            "insert" -> {
                val idx = parsed.index.coerceIn(0, items.size)
                items.add(idx, parsed.value)
            }
            "remove" -> {
                if (parsed.index in items.indices) {
                    resultValue = items.removeAt(parsed.index)
                }
            }
            "sort" -> items.sort()
            "reverse" -> items.reverse()
            "clear" -> items.clear()
            "size" -> {
                resultValue = items.size.toString()
            }
            "get" -> {
                resultValue = items.getOrElse(parsed.index) { "" }
            }
            "set" -> {
                if (parsed.index in items.indices) {
                    items[parsed.index] = parsed.value
                }
            }
            else -> Log.w(TAG, "Unknown array operation: ${parsed.operation}")
        }

        // Save modified array back
        val jsonArray = JsonArray(items.map { JsonPrimitive(it) })
        val newValue = jsonArray.toString()

        if (isLocal) {
            context.localVariables[varName] = newValue
        } else {
            context.variableStore?.setGlobal(
                varName.removePrefix("v_"), newValue, VariableType.ARRAY,
            )
        }

        // Store result if requested
        if (resultValue != null && parsed.resultVariable.isNotBlank()) {
            val resultVar = parsed.resultVariable
            if (resultVar.startsWith("lv_")) {
                context.localVariables[resultVar] = resultValue
            } else {
                context.variableStore?.setGlobal(resultVar.removePrefix("v_"), resultValue)
            }
        }
    }

    companion object {
        private const val TAG = "ArrayManipHandler"
    }
}
