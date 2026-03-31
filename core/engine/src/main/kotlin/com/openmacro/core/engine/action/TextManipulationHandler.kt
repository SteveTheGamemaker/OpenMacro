package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.TextManipulationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Performs text manipulation operations and stores the result in a variable.
 */
class TextManipulationHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "text_manipulation"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = try {
            Json.decodeFromString<TextManipulationConfig>(config.configJson)
        } catch (_: Exception) {
            TextManipulationConfig()
        }

        if (parsed.resultVariable.isBlank()) return

        val input = parsed.input
        val result = try {
            when (parsed.operation) {
                "substring" -> {
                    val start = parsed.param1.toIntOrNull() ?: 0
                    val end = parsed.param2.toIntOrNull() ?: input.length
                    input.substring(
                        start.coerceIn(0, input.length),
                        end.coerceIn(0, input.length),
                    )
                }
                "replace" -> input.replace(parsed.param1, parsed.param2)
                "split" -> {
                    // Split by param1, store as JSON array
                    val parts = input.split(parsed.param1)
                    JsonArray(parts.map { JsonPrimitive(it) }).toString()
                }
                "join" -> {
                    // Input is a JSON array, join with param1 delimiter
                    try {
                        val arr = Json.parseToJsonElement(input) as? JsonArray
                        arr?.joinToString(parsed.param1) {
                            it.jsonPrimitive.content
                        } ?: input
                    } catch (_: Exception) { input }
                }
                "trim" -> input.trim()
                "uppercase" -> input.uppercase()
                "lowercase" -> input.lowercase()
                "regex_extract" -> {
                    val regex = Regex(parsed.param1)
                    regex.find(input)?.groupValues?.getOrElse(
                        parsed.param2.toIntOrNull() ?: 0,
                    ) { "" } ?: ""
                }
                "format" -> {
                    // Simple format: replace %1, %2, ... with param1, param2
                    input.replace("%1", parsed.param1).replace("%2", parsed.param2)
                }
                "length" -> input.length.toString()
                "indexOf" -> input.indexOf(parsed.param1).toString()
                "contains" -> input.contains(parsed.param1, ignoreCase = true).toString()
                else -> {
                    Log.w(TAG, "Unknown text operation: ${parsed.operation}")
                    input
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Text manipulation failed: ${e.message}")
            ""
        }

        val resultVar = parsed.resultVariable
        if (resultVar.startsWith("lv_")) {
            context.localVariables[resultVar] = result
        } else {
            context.variableStore?.setGlobal(resultVar.removePrefix("v_"), result)
        }
    }

    companion object {
        private const val TAG = "TextManipHandler"
    }
}
