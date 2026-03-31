package com.openmacro.core.engine.action

import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.AutoRotateConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AutoRotateHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "auto_rotate"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<AutoRotateConfig>(config.configJson)
        val resolver = context.androidContext.contentResolver
        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, if (parsed.enable) 1 else 0)
    }
}
