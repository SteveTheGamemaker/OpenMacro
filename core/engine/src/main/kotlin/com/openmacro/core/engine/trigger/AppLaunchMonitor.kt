package com.openmacro.core.engine.trigger

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.AppLaunchConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Monitors for app launch/close events using UsageStatsManager.
 * Polls every 2 seconds — requires PACKAGE_USAGE_STATS permission.
 */
class AppLaunchMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "app_launch"

    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var pollingJob: Job? = null
    private var lastForegroundPackage: String? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (pollingJob != null) {
            updateConfigs(configs)
            return
        }
        this.configs = configs
        this.callback = onTrigger

        pollingJob = CoroutineScope(Dispatchers.Default).launch {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usm == null) {
                Log.w(TAG, "UsageStatsManager not available")
                return@launch
            }

            while (isActive) {
                val currentFg = getCurrentForegroundPackage(usm)
                if (currentFg != null && currentFg != lastForegroundPackage) {
                    val previousFg = lastForegroundPackage
                    lastForegroundPackage = currentFg

                    // Check launches
                    for (config in configs) {
                        val parsed = parseConfig(config)
                        if (parsed.onLaunch && parsed.packageName == currentFg) {
                            callback?.invoke(
                                TriggerEvent(
                                    triggerTypeId = triggerTypeId,
                                    data = mapOf(
                                        "package_name" to currentFg,
                                        "event" to "launch",
                                    ),
                                )
                            )
                        }
                        if (parsed.onClose && parsed.packageName == previousFg) {
                            callback?.invoke(
                                TriggerEvent(
                                    triggerTypeId = triggerTypeId,
                                    data = mapOf(
                                        "package_name" to parsed.packageName,
                                        "event" to "close",
                                    ),
                                )
                            )
                        }
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
        Log.d(TAG, "Started monitoring app launches")
    }

    override fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        callback = null
        configs = emptyList()
        lastForegroundPackage = null
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun getCurrentForegroundPackage(usm: UsageStatsManager): String? {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 5000, now)
        val event = UsageEvents.Event()
        var lastPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun parseConfig(config: TriggerConfig): AppLaunchConfig {
        return try {
            Json.decodeFromString<AppLaunchConfig>(config.configJson)
        } catch (_: Exception) {
            AppLaunchConfig()
        }
    }

    companion object {
        private const val TAG = "AppLaunchMonitor"
        private const val POLL_INTERVAL_MS = 2000L
    }
}
