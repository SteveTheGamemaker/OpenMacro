package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.JsonParseConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

/**
 * Parses a JSON string and extracts a value using dot-notation path.
 * Example path: "data.items.0.name" navigates into objects by key and arrays by index.
 */
class JsonParseHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "json_parse"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = try {
            Json.decodeFromString<JsonParseConfig>(config.configJson)
        } catch (_: Exception) {
            JsonParseConfig()
        }

        if (parsed.jsonSource.isBlank() || parsed.resultVariable.isBlank()) return

        val jsonString = parsed.jsonSource

        try {
            val root = Json.parseToJsonElement(jsonString)
            val result = navigatePath(root, parsed.jsonPath)
            val resultStr = when (result) {
                is JsonPrimitive -> result.content
                else -> result.toString()
            }

            val resultVar = parsed.resultVariable
            if (resultVar.startsWith("lv_")) {
                context.localVariables[resultVar] = resultStr
            } else {
                context.variableStore?.setGlobal(resultVar.removePrefix("v_"), resultStr)
            }
            Log.d(TAG, "Parsed '${parsed.jsonPath}' = $resultStr")
        } catch (e: Exception) {
            Log.w(TAG, "JSON parse failed: ${e.message}")
        }
    }

    private fun navigatePath(root: JsonElement, path: String): JsonElement {
        if (path.isBlank()) return root

        var current = root
        for (segment in path.split(".")) {
            current = when (current) {
                is JsonObject -> current[segment]
                    ?: throw IllegalArgumentException("Key '$segment' not found")
                is JsonArray -> {
                    val idx = segment.toIntOrNull()
                        ?: throw IllegalArgumentException("Expected array index, got '$segment'")
                    current.getOrNull(idx)
                        ?: throw IndexOutOfBoundsException("Index $idx out of bounds")
                }
                else -> throw IllegalArgumentException("Cannot navigate into primitive at '$segment'")
            }
        }
        return current
    }

    companion object {
        private const val TAG = "JsonParseHandler"
    }
}
