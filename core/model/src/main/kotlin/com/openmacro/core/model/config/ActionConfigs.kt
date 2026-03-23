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
