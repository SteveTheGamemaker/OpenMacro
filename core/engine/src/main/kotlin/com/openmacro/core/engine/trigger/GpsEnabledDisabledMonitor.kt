package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.GpsEnabledDisabledConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GpsEnabledDisabledMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "gps_enabled_disabled"

    private var receiver: BroadcastReceiver? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastGpsEnabled: Boolean? = null

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
                val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (lastGpsEnabled != isEnabled) {
                    lastGpsEnabled = isEnabled
                    handleGpsEvent(isEnabled)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring GPS enabled/disabled")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        lastGpsEnabled = null
        Log.d(TAG, "Stopped monitoring GPS enabled/disabled")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleGpsEvent(isEnabled: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<GpsEnabledDisabledConfig>(config.configJson)
            } catch (_: Exception) {
                GpsEnabledDisabledConfig()
            }

            val shouldFire = (isEnabled && parsed.onEnabled) ||
                    (!isEnabled && parsed.onDisabled)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("gps_enabled" to isEnabled.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "GpsEnabledDisabledMonitor"
    }
}
