package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.DeleteVariableConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DeleteVariableHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "delete_variable"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<DeleteVariableConfig>(config.configJson)
        if (parsed.variableName.isBlank()) return

        if (parsed.variableName.startsWith("lv_")) {
            context.localVariables.remove(parsed.variableName)
        } else {
            context.variableStore?.deleteGlobal(parsed.variableName)
        }
    }
}
