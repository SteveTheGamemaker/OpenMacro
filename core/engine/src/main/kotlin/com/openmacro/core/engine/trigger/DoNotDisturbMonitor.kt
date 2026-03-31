package com.openmacro.core.engine.trigger

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DoNotDisturbConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DoNotDisturbMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "do_not_disturb"

    private var job: Job? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastDndEnabled: Boolean? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (job != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger
        lastDndEnabled = null

        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val isDnd = getDndStatus()
                if (isDnd != lastDndEnabled) {
                    lastDndEnabled = isDnd
                    handleDndEvent(isDnd)
                }
                delay(2000)
            }
        }
        Log.d(TAG, "Started monitoring do not disturb")
    }

    override fun stop() {
        job?.cancel()
        job = null
        appContext = null
        callback = null
        configs = emptyList()
        lastDndEnabled = null
        Log.d(TAG, "Stopped monitoring do not disturb")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun getDndStatus(): Boolean {
        val nm = appContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val filter = nm?.currentInterruptionFilter
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private fun handleDndEvent(isDnd: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<DoNotDisturbConfig>(config.configJson)
            } catch (_: Exception) {
                DoNotDisturbConfig()
            }

            val shouldFire = (isDnd && parsed.onEnabled) ||
                    (!isDnd && parsed.onDisabled)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("dnd_enabled" to isDnd.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "DoNotDisturbMonitor"
    }
}
