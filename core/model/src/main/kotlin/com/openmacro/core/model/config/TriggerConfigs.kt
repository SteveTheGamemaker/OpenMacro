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

// Milestone 5

@Serializable
data class WifiStateChangeConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
)

@Serializable
data class WifiSsidTransitionConfig(
    val ssid: String = "",
    val onConnect: Boolean = true,
    val onDisconnect: Boolean = false,
)

@Serializable
data class BluetoothEventConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
    val onDeviceConnected: Boolean = false,
    val onDeviceDisconnected: Boolean = false,
    val deviceAddress: String = "",
)

@Serializable
data class DataConnectivityChangeConfig(
    val onConnected: Boolean = true,
    val onDisconnected: Boolean = false,
)

@Serializable
data class AirplaneModeChangedConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
)

@Serializable
data class SmsReceivedConfig(
    val senderFilter: String = "",
)

@Serializable
data class CallIncomingConfig(
    val numberFilter: String = "",
)

@Serializable
data class CallEndedConfig(
    val numberFilter: String = "",
)

@Serializable
data class CallMissedConfig(
    val numberFilter: String = "",
)
