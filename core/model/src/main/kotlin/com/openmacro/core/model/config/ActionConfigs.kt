package com.openmacro.core.model.config

import kotlinx.serialization.Serializable

/**
 * Serializable config data classes for each action type.
 * These get stored as JSON in ActionConfig.configJson.
 */

@Serializable
data class DisplayNotificationConfig(
    val title: String = "",
    val body: String = "",
    val channelId: String = "default",
)

@Serializable
data class LaunchApplicationConfig(
    val packageName: String = "",
)

@Serializable
data class SetVolumeConfig(
    val streamType: Int = 3, // AudioManager.STREAM_MUSIC
    val level: Int = 50,
)

@Serializable
data class VibrateConfig(
    val durationMs: Long = 500,
    val pattern: List<Long> = emptyList(), // empty = simple vibration
)

@Serializable
data class WaitConfig(
    val durationMs: Long = 1000,
)

@Serializable
data class SetVariableConfig(
    val variableName: String = "",
    val value: String = "",
)

@Serializable
data class DeleteVariableConfig(
    val variableName: String = "",
)

// Milestone 5

@Serializable
data class WifiConfigureConfig(
    val enable: Boolean = true,
)

@Serializable
data class BluetoothConfigureConfig(
    val enable: Boolean = true,
)

@Serializable
data class AirplaneModeConfig(
    val enable: Boolean = true,
)

@Serializable
data class SendSmsConfig(
    val phoneNumber: String = "",
    val message: String = "",
)

@Serializable
data class MakeCallConfig(
    val phoneNumber: String = "",
)

@Serializable
data class LaunchHomeScreenConfig(
    val placeholder: Boolean = true,
)

@Serializable
data class OpenWebsiteConfig(
    val url: String = "",
    val useBrowser: Boolean = true,
)

@Serializable
data class HttpRequestConfig(
    val url: String = "",
    val method: String = "GET",
    val headers: String = "",
    val body: String = "",
    val saveResponseTo: String = "",
)

@Serializable
data class SpeakTextConfig(
    val text: String = "",
    val language: String = "",
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
)

@Serializable
data class FillClipboardConfig(
    val text: String = "",
)
