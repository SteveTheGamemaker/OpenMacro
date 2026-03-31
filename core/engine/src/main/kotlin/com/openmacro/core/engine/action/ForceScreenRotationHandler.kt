package com.openmacro.core.engine.action

import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.ForceScreenRotationConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ForceScreenRotationHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "force_screen_rotation"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<ForceScreenRotationConfig>(config.configJson)
        val resolver = context.androidContext.contentResolver

        // Disable auto-rotate first
        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        // Set rotation
        Settings.System.putInt(resolver, Settings.System.USER_ROTATION, parsed.rotation)
    }
}
