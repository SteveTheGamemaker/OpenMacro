package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DarkThemeChangeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DarkThemeChangeMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "dark_theme_change"

    private var receiver: BroadcastReceiver? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastDarkMode: Boolean? = null

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
                val isDark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                handleDarkThemeEvent(isDark)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring dark theme change")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        lastDarkMode = null
        Log.d(TAG, "Stopped monitoring dark theme change")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleDarkThemeEvent(isDark: Boolean) {
        if (lastDarkMode == isDark) return
        lastDarkMode = isDark

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<DarkThemeChangeConfig>(config.configJson)
            } catch (_: Exception) {
                DarkThemeChangeConfig()
            }

            val shouldFire = (isDark && parsed.onDarkEnabled) ||
                    (!isDark && parsed.onDarkDisabled)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("dark_mode_enabled" to isDark.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "DarkThemeChangeMonitor"
    }
}
