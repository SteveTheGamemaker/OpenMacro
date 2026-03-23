package com.openmacro.core.engine.action

import android.content.Context
import android.media.AudioManager
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SetVolumeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SetVolumeHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "set_volume"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SetVolumeConfig>(config.configJson)
        val am = context.androidContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val maxVolume = am.getStreamMaxVolume(parsed.streamType)
        val targetVolume = (parsed.level * maxVolume / 100).coerceIn(0, maxVolume)

        am.setStreamVolume(parsed.streamType, targetVolume, 0)
    }
}
