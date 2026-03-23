package com.openmacro.core.engine.trigger

import android.content.Context
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig

/**
 * Base interface for all trigger monitors. Each implementation watches for a
 * specific system event and calls [onTrigger] when it fires.
 *
 * Think of this like an event listener in web dev — it subscribes to a system
 * event and emits when the condition is met.
 */
interface TriggerMonitor {
    val triggerTypeId: String

    fun start(context: Context, configs: List<TriggerConfig>, onTrigger: (TriggerEvent) -> Unit)
    fun stop()
    fun updateConfigs(configs: List<TriggerConfig>)
}
