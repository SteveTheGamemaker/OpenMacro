package com.openmacro.core.engine.action

import android.content.Intent
import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.GpsEnableDisableConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GpsEnableDisableHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "gps_enable_disable"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        // GPS cannot be toggled programmatically on modern Android — open settings
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.androidContext.startActivity(intent)
    }
}
