package com.openmacro.core.engine.action

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.AirplaneModeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AirplaneModeHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "airplane_mode"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<AirplaneModeConfig>(config.configJson)
        val ctx = context.androidContext

        // Airplane mode cannot be toggled programmatically without WRITE_SECURE_SETTINGS.
        // Try it, and fall back to opening settings if it fails.
        try {
            Settings.Global.putInt(
                ctx.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                if (parsed.enable) 1 else 0,
            )
            // Broadcast the change
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", parsed.enable)
            }
            ctx.sendBroadcast(intent)
        } catch (_: SecurityException) {
            // Fallback: open airplane mode settings
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }
    }
}
