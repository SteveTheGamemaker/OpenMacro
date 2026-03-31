package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.BatteryLevelTriggerConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BatteryLevelMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "battery_level"

    private var appContext: Context? = null
    private var receiver: BroadcastReceiver? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastLevel: Int = -1
    private val firedSet = mutableSetOf<Long>() // track which configs already fired to avoid repeat

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
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val pct = if (scale > 0) (level * 100) / scale else level
                handleBatteryLevel(pct)
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(TAG, "Started monitoring battery level")
    }

    override fun stop() {
        receiver?.let { r ->
            try {
                appContext?.unregisterReceiver(r)
            } catch (_: Exception) { }
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        lastLevel = -1
        firedSet.clear()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        firedSet.clear()
    }

    private fun handleBatteryLevel(level: Int) {
        if (level == lastLevel) return
        lastLevel = level

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<BatteryLevelTriggerConfig>(config.configJson)
            } catch (_: Exception) {
                BatteryLevelTriggerConfig()
            }

            val matches = if (parsed.whenBelow) level <= parsed.threshold else level >= parsed.threshold
            if (matches && config.id !in firedSet) {
                firedSet.add(config.id)
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("battery_level" to level.toString()),
                    )
                )
            } else if (!matches) {
                firedSet.remove(config.id) // reset so it can fire again when crossing threshold
            }
        }
    }

    companion object {
        private const val TAG = "BatteryLevelMonitor"
    }
}
