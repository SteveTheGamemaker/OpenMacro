package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DeviceBootConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DeviceBootMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "device_boot"

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
                handleBootEvent()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring device boot")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring device boot")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleBootEvent() {
        for (config in configs) {
            callback?.invoke(
                TriggerEvent(
                    triggerTypeId = triggerTypeId,
                    data = mapOf("event" to "boot_completed"),
                )
            )
        }
    }

    companion object {
        private const val TAG = "DeviceBootMonitor"
    }
}
