package com.openmacro.core.engine.constraint

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.openmacro.core.model.config.PowerConnectedConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PowerConnectedChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "power_connected"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<PowerConnectedConstraintConfig>(configJson)
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val isConnected = plugged != 0
        return isConnected == config.connected
    }
}
