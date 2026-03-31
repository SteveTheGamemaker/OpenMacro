package com.openmacro.core.engine.constraint

import android.app.NotificationManager
import android.content.Context
import com.openmacro.core.model.config.DoNotDisturbConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DoNotDisturbChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "do_not_disturb"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<DoNotDisturbConstraintConfig>(configJson)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isDndEnabled = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        return isDndEnabled == config.enabled
    }
}
