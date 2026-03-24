package com.openmacro.core.engine.action

import android.content.Intent
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.LaunchApplicationConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LaunchApplicationHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "launch_application"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<LaunchApplicationConfig>(config.configJson)
        val ctx = context.androidContext
        val pm = ctx.packageManager

        val launchIntent = pm.getLaunchIntentForPackage(parsed.packageName)
            ?: throw IllegalArgumentException("No launch intent for package: ${parsed.packageName}")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        ctx.startActivity(launchIntent)
    }
}
