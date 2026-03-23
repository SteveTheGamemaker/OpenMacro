package com.openmacro.core.engine.constraint

import android.content.Context
import android.os.PowerManager
import com.openmacro.core.model.config.ScreenStateConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScreenStateChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "screen_state"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<ScreenStateConfig>(configJson)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = pm.isInteractive
        return screenOn == config.screenOn
    }
}
