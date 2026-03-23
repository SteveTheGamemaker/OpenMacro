package com.openmacro.core.engine.action

import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.WaitConfig
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WaitHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "wait"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<WaitConfig>(config.configJson)
        delay(parsed.durationMs)
    }
}
