package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.PowerConnectedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PowerConnectedMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "power_connected"

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
                val connected = intent.action == Intent.ACTION_POWER_CONNECTED
                handlePowerEvent(connected)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring power connected")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handlePowerEvent(connected: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<PowerConnectedConfig>(config.configJson)
            } catch (_: Exception) {
                PowerConnectedConfig()
            }

            val shouldFire = (connected && parsed.onConnect) ||
                    (!connected && parsed.onDisconnect)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("power_connected" to connected.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "PowerConnectedMonitor"
    }
}
