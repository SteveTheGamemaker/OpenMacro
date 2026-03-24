package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.AirplaneModeChangedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AirplaneModeChangedMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "airplane_mode_changed"

    private var receiver: BroadcastReceiver? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (receiver != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val isAirplaneMode = Settings.Global.getInt(
                    ctx.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    0,
                ) != 0
                handleAirplaneModeChange(isAirplaneMode)
            }
        }

        context.applicationContext.registerReceiver(receiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        Log.d(TAG, "Started monitoring airplane mode changes")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring airplane mode changes")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleAirplaneModeChange(enabled: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<AirplaneModeChangedConfig>(config.configJson)
            } catch (_: Exception) {
                AirplaneModeChangedConfig()
            }

            val shouldFire = (enabled && parsed.onEnabled) || (!enabled && parsed.onDisabled)
            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("airplane_mode" to enabled.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "AirplaneModeMonitor"
    }
}
