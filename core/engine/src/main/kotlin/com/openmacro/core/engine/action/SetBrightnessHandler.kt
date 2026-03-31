package com.openmacro.core.engine.action

import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SetBrightnessConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SetBrightnessHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "set_brightness"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SetBrightnessConfig>(config.configJson)
        val resolver = context.androidContext.contentResolver

        if (parsed.autoMode) {
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        } else {
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, parsed.level.coerceIn(0, 255))
        }
    }
}
