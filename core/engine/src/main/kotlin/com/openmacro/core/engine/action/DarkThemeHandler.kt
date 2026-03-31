package com.openmacro.core.engine.action

import android.app.UiModeManager
import android.content.Context
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.DarkThemeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DarkThemeHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "dark_theme"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<DarkThemeConfig>(config.configJson)
        val uiModeManager = context.androidContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        uiModeManager.nightMode = if (parsed.enable) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO
    }
}
