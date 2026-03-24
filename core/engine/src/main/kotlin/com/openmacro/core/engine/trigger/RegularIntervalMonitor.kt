package com.openmacro.core.engine.trigger

import android.content.Context
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.RegularIntervalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RegularIntervalMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "regular_interval"

    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private val jobs = mutableMapOf<Long, Job>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var intervalCount = 0L

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (jobs.isNotEmpty()) {
            updateConfigs(configs)
            return
        }
        this.configs = configs
        this.callback = onTrigger
        startTimers()
        Log.d(TAG, "Started regular interval monitor with ${configs.size} configs")
    }

    override fun stop() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        callback = null
        configs = emptyList()
        intervalCount = 0
        Log.d(TAG, "Stopped regular interval monitor")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        // Restart timers with new configs
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        startTimers()
    }

    private fun startTimers() {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<RegularIntervalConfig>(config.configJson)
            } catch (_: Exception) {
                RegularIntervalConfig()
            }

            val job = scope.launch {
                while (true) {
                    delay(parsed.intervalMs)
                    intervalCount++
                    callback?.invoke(
                        TriggerEvent(
                            triggerTypeId = triggerTypeId,
                            data = mapOf("interval_count" to intervalCount.toString()),
                        )
                    )
                }
            }
            jobs[config.id] = job
        }
    }

    companion object {
        private const val TAG = "RegularIntervalMonitor"
    }
}
