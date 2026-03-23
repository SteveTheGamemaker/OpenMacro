package com.openmacro.core.model.config

import kotlinx.serialization.Serializable

/**
 * Serializable config data classes for each trigger type.
 * These get stored as JSON in TriggerConfig.configJson.
 */

@Serializable
data class ScreenOnOffConfig(
    val onScreenOn: Boolean = true,
    val onScreenOff: Boolean = false,
)

@Serializable
data class BatteryLevelTriggerConfig(
    val threshold: Int = 20,
    val whenBelow: Boolean = true,
)

@Serializable
data class PowerConnectedConfig(
    val onConnect: Boolean = true,
    val onDisconnect: Boolean = false,
)

@Serializable
data class DayTimeConfig(
    val hour: Int = 8,
    val minute: Int = 0,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5), // Mon-Fri (Calendar constants)
)

@Serializable
data class AppLaunchConfig(
    val packageName: String = "",
    val onLaunch: Boolean = true,
    val onClose: Boolean = false,
)

@Serializable
data class RegularIntervalConfig(
    val intervalMs: Long = 60_000,
)
