package com.openmacro.core.engine.action

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.WifiConfigureConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WifiConfigureHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "wifi_configure"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<WifiConfigureConfig>(config.configJson)
        val ctx = context.androidContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, can't programmatically toggle WiFi. Open settings panel.
            val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } else {
            @Suppress("DEPRECATION")
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wm.isWifiEnabled = parsed.enable
        }
    }
}
