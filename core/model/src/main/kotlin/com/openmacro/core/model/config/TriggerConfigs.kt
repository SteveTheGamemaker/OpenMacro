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
    val daysOfWeek: List<Int> = listOf(2, 3, 4, 5, 6), // Mon-Fri (Calendar.MONDAY=2 through FRIDAY=6)
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
data class SmsSentConfig(
    val recipientFilter: String = "",
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

// Milestone 7 — Sensors

@Serializable
data class ShakeDeviceConfig(
    val sensitivity: Float = 12.0f, // acceleration threshold in m/s²
    val shakeDurationMs: Long = 500,
)

@Serializable
data class FlipDeviceConfig(
    val onFaceDown: Boolean = true,
    val onFaceUp: Boolean = false,
)

@Serializable
data class ProximitySensorConfig(
    val onNear: Boolean = true,
    val onFar: Boolean = false,
)

@Serializable
data class LightSensorConfig(
    val threshold: Float = 50.0f, // lux
    val whenBelow: Boolean = true,
)

@Serializable
data class ScreenOrientationConfig(
    val orientation: String = "landscape", // "portrait", "landscape"
)

@Serializable
data class ActivityRecognitionConfig(
    val activityType: String = "walking", // "still", "walking", "running", "driving", "cycling"
    val confidenceThreshold: Int = 75,
)

// Milestone 7 — Device State

@Serializable
data class DeviceBootConfig(
    val placeholder: Boolean = true,
)

@Serializable
data class BatteryTemperatureTriggerConfig(
    val threshold: Float = 40.0f,
    val whenAbove: Boolean = true,
)

@Serializable
data class BatterySaverStateConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
)

@Serializable
data class DarkThemeChangeConfig(
    val onDarkEnabled: Boolean = true,
    val onDarkDisabled: Boolean = false,
)

@Serializable
data class GpsEnabledDisabledConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
)

@Serializable
data class DoNotDisturbConfig(
    val onEnabled: Boolean = true,
    val onDisabled: Boolean = false,
)

@Serializable
data class SilentModeConfig(
    val onSilent: Boolean = true,
    val onNormal: Boolean = false,
)

@Serializable
data class TorchOnOffConfig(
    val onTorchOn: Boolean = true,
    val onTorchOff: Boolean = false,
)

// Milestone 7 — Location

@Serializable
data class GeofenceConfig(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 100.0f,
    val onEnter: Boolean = true,
    val onExit: Boolean = false,
    val dwellTimeMs: Long = 0,
    val locationName: String = "",
)

@Serializable
data class LocationConfig(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 500.0f,
    val locationName: String = "",
)
