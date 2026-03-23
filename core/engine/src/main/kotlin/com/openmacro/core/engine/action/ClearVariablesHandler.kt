package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import javax.inject.Inject

class ClearVariablesHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "clear_variables"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        context.variableStore?.clearGlobals()
        context.localVariables.clear()
    }
}
