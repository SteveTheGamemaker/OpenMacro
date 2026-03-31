package com.openmacro.core.engine.constraint

import android.content.Context
import android.media.AudioManager
import com.openmacro.core.model.config.SilentModeConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SilentModeChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "silent_mode"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<SilentModeConstraintConfig>(configJson)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT ||
                audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        return isSilent == config.silent
    }
}
