package com.openmacro.core.model.config

import kotlinx.serialization.Serializable

/**
 * Serializable config data classes for each constraint type.
 * These get stored as JSON in ConstraintConfig.configJson.
 */

@Serializable
data class BatteryLevelConstraintConfig(
    val minLevel: Int = 0,
    val maxLevel: Int = 100,
)

@Serializable
data class TimeOfDayConfig(
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
)

@Serializable
data class DayOfWeekConfig(
    val days: List<Int> = listOf(1, 2, 3, 4, 5), // Mon-Fri
)

@Serializable
data class WifiConnectedConfig(
    val ssid: String? = null, // null = any WiFi
)

@Serializable
data class ScreenStateConfig(
    val screenOn: Boolean = true,
)

@Serializable
data class PowerConnectedConstraintConfig(
    val connected: Boolean = true,
)

@Serializable
data class AppRunningConfig(
    val packageName: String = "",
)

@Serializable
data class VariableValueConfig(
    val variableName: String = "",
    val operator: String = "==",
    val value: String = "",
)

// Milestone 5

@Serializable
data class BluetoothConnectedConfig(
    val deviceAddress: String = "",
)

@Serializable
data class WifiEnabledConfig(
    val enabled: Boolean = true,
)

@Serializable
data class AirplaneModeConstraintConfig(
    val enabled: Boolean = true,
)

@Serializable
data class CallStateConstraintConfig(
    val state: String = "idle",
)
