package com.openmacro.core.engine.action

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.VibrateConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class VibrateHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "vibrate"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<VibrateConfig>(config.configJson)
        val ctx = context.androidContext

        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect = if (parsed.pattern.isNotEmpty()) {
            VibrationEffect.createWaveform(parsed.pattern.toLongArray(), -1)
        } else {
            VibrationEffect.createOneShot(parsed.durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        }

        vibrator.vibrate(effect)
    }
}
