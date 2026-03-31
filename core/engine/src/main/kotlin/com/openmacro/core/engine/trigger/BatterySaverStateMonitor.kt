package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.BatterySaverStateConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BatterySaverStateMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "battery_saver_state"

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
                val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isPowerSaveMode = powerManager.isPowerSaveMode
                handleBatterySaverEvent(isPowerSaveMode)
            }
        }

        val filter = IntentFilter().apply {
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring battery saver state")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring battery saver state")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleBatterySaverEvent(isPowerSaveMode: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<BatterySaverStateConfig>(config.configJson)
            } catch (_: Exception) {
                BatterySaverStateConfig()
            }

            val shouldFire = (isPowerSaveMode && parsed.onEnabled) ||
                    (!isPowerSaveMode && parsed.onDisabled)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("battery_saver_enabled" to isPowerSaveMode.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "BatterySaverStateMonitor"
    }
}
