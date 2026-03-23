package com.openmacro.core.engine.trigger

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of all available trigger monitors, keyed by triggerTypeId.
 * Hilt injects the full set; the dispatcher looks up monitors here.
 */
@Singleton
class TriggerRegistry @Inject constructor(
    monitors: Set<@JvmSuppressWildcards TriggerMonitor>,
) {
    private val byTypeId: Map<String, TriggerMonitor> =
        monitors.associateBy { it.triggerTypeId }

    fun get(typeId: String): TriggerMonitor? = byTypeId[typeId]

    fun all(): Collection<TriggerMonitor> = byTypeId.values
}
