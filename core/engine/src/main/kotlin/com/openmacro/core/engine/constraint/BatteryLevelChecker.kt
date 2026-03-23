package com.openmacro.core.engine.constraint

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.openmacro.core.model.config.BatteryLevelConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BatteryLevelChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "battery_level"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<BatteryLevelConstraintConfig>(configJson)
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.let { intent ->
            val lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            (lvl * 100) / scale
        } ?: return false

        return level in config.minLevel..config.maxLevel
    }
}
