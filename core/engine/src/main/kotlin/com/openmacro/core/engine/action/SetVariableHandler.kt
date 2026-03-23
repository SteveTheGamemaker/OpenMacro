package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SetVariableConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SetVariableHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "set_variable"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SetVariableConfig>(config.configJson)
        if (parsed.variableName.isBlank()) return

        if (parsed.variableName.startsWith("lv_")) {
            // Local variable — lives only in this execution
            context.localVariables[parsed.variableName] = parsed.value
        } else {
            // Global variable — persisted
            context.variableStore?.setGlobal(parsed.variableName, parsed.value)
        }
    }
}
