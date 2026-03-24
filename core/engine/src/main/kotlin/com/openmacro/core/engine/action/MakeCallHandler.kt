package com.openmacro.core.engine.action

import android.content.Intent
import android.net.Uri
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.MakeCallConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MakeCallHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "make_call"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<MakeCallConfig>(config.configJson)
        if (parsed.phoneNumber.isBlank()) return

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${parsed.phoneNumber}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.androidContext.startActivity(intent)
    }
}
