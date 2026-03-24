package com.openmacro.core.engine.constraint

import android.content.Context
import android.net.wifi.WifiManager
import com.openmacro.core.model.config.WifiEnabledConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WifiEnabledChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "wifi_enabled"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<WifiEnabledConfig>(configJson)
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val isEnabled = wm.isWifiEnabled
        return isEnabled == config.enabled
    }
}
