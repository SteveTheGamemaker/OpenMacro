package com.openmacro.core.engine.action

import android.content.Intent
import android.net.Uri
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.OpenWebsiteConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class OpenWebsiteHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "open_website"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<OpenWebsiteConfig>(config.configJson)
        if (parsed.url.isBlank()) return

        var url = parsed.url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.androidContext.startActivity(intent)
    }
}
