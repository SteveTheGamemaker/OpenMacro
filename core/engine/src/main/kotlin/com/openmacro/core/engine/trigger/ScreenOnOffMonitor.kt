package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.ScreenOnOffConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScreenOnOffMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "screen_on_off"

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
                val isScreenOn = intent.action == Intent.ACTION_SCREEN_ON
                handleScreenEvent(isScreenOn)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring screen on/off")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring screen on/off")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleScreenEvent(isScreenOn: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<ScreenOnOffConfig>(config.configJson)
            } catch (_: Exception) {
                ScreenOnOffConfig()
            }

            val shouldFire = (isScreenOn && parsed.onScreenOn) ||
                    (!isScreenOn && parsed.onScreenOff)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("screen_on" to isScreenOn.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "ScreenOnOffMonitor"
    }
}
