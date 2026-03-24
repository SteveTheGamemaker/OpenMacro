package com.openmacro.core.engine.action

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.FillClipboardConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FillClipboardHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "fill_clipboard"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<FillClipboardConfig>(config.configJson)
        val ctx = context.androidContext

        // ClipboardManager must be accessed on the main thread
        Handler(Looper.getMainLooper()).post {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("OpenMacro", parsed.text)
            clipboard.setPrimaryClip(clip)
        }
    }
}
