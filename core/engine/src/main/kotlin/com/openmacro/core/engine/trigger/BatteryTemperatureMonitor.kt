package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.BatteryTemperatureTriggerConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BatteryTemperatureMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "battery_temperature"

    private var appContext: Context? = null
    private var receiver: BroadcastReceiver? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastTemp: Float = -1.0f
    private val firedSet = mutableSetOf<Long>()

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
                val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                val temp = tempRaw / 10.0f
                handleBatteryTemperature(temp)
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(TAG, "Started monitoring battery temperature")
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
        lastTemp = -1.0f
        firedSet.clear()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        firedSet.clear()
    }

    private fun handleBatteryTemperature(temp: Float) {
        if (temp == lastTemp) return
        lastTemp = temp

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<BatteryTemperatureTriggerConfig>(config.configJson)
            } catch (_: Exception) {
                BatteryTemperatureTriggerConfig()
            }

            val matches = if (parsed.whenAbove) temp >= parsed.threshold else temp <= parsed.threshold
            if (matches && config.id !in firedSet) {
                firedSet.add(config.id)
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("battery_temperature" to temp.toString()),
                    )
                )
            } else if (!matches) {
                firedSet.remove(config.id)
            }
        }
    }

    companion object {
        private const val TAG = "BatteryTemperatureMonitor"
    }
}
