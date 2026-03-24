package com.openmacro.core.engine.constraint

import android.content.Context
import android.provider.Settings
import com.openmacro.core.model.config.AirplaneModeConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AirplaneModeChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "airplane_mode"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<AirplaneModeConstraintConfig>(configJson)
        val isAirplaneMode = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0,
        ) != 0
        return isAirplaneMode == config.enabled
    }
}
