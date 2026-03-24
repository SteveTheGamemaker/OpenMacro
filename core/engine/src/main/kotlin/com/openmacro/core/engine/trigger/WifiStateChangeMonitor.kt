package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.WifiStateChangeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WifiStateChangeMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "wifi_state_change"

    private var receiver: BroadcastReceiver? = null
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
        this.configs = configs
        this.callback = onTrigger

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                when (state) {
                    WifiManager.WIFI_STATE_ENABLED -> handleWifiState(true)
                    WifiManager.WIFI_STATE_DISABLED -> handleWifiState(false)
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        Log.d(TAG, "Started monitoring WiFi state changes")
    }

    override fun stop() {
        receiver = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring WiFi state changes")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleWifiState(enabled: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<WifiStateChangeConfig>(config.configJson)
            } catch (_: Exception) {
                WifiStateChangeConfig()
            }

            val shouldFire = (enabled && parsed.onEnabled) || (!enabled && parsed.onDisabled)
            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("wifi_enabled" to enabled.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "WifiStateChangeMonitor"
    }
}
