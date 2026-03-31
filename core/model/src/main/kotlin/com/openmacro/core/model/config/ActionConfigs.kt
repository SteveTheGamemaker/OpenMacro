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

// Milestone 6 — Flow Control

@Serializable
data class IfClauseConfig(
    val conditionExpression: String = "",
    val hasElse: Boolean = false,
)

@Serializable
data class RepeatConfig(
    val mode: String = "count", // "count", "count_expression", or "while"
    val count: Int = 1,
    val countExpression: String = "",
    val whileExpression: String = "",
)

@Serializable
data class IterateConfig(
    val variableName: String = "", // name of array/dictionary variable to iterate
    val keyVariable: String = "lv_key", // local var to store key/index
    val valueVariable: String = "lv_value", // local var to store value
)

@Serializable
data class ArrayManipulationConfig(
    val operation: String = "push", // push, pop, insert, remove, sort, reverse, clear, size, get, set
    val variableName: String = "", // array variable name
    val value: String = "", // value to push/insert/set
    val index: Int = 0, // index for insert/remove/get/set
    val resultVariable: String = "", // variable to store result (pop/size/get)
)

@Serializable
data class JsonParseConfig(
    val jsonSource: String = "", // JSON string or variable containing JSON
    val jsonPath: String = "", // dot-notation path, e.g. "data.items.0.name"
    val resultVariable: String = "", // variable to store extracted value
)

@Serializable
data class TextManipulationConfig(
    val operation: String = "replace", // substring, replace, split, join, trim, uppercase, lowercase, regex_extract, format
    val input: String = "", // input text
    val param1: String = "", // first parameter (pattern, start index, etc.)
    val param2: String = "", // second parameter (replacement, end index, etc.)
    val resultVariable: String = "", // variable to store result
)

@Serializable
data class WaitUntilTriggerConfig(
    val triggerTypeId: String = "", // which trigger type to wait for
    val triggerConfigJson: String = "{}", // trigger-specific config
    val timeoutMs: Long = 60_000, // max wait time
)

@Serializable
data class RunActionBlockConfig(
    val blockId: Long = 0, // ID of the action block to run
    val inputMappings: Map<String, String> = emptyMap(), // param name → value/expression
    val outputMappings: Map<String, String> = emptyMap(), // param name → local variable to store result
)

// Milestone 7 — Device Actions

@Serializable
data class SetBrightnessConfig(
    val level: Int = 128, // 0-255
    val autoMode: Boolean = false,
)

@Serializable
data class ScreenOnOffActionConfig(
    val turnOn: Boolean = true,
)

@Serializable
data class ForceScreenRotationConfig(
    val rotation: Int = 0, // 0=natural, 1=90°, 2=180°, 3=270°
)

@Serializable
data class AutoRotateConfig(
    val enable: Boolean = true,
)

@Serializable
data class DarkThemeConfig(
    val enable: Boolean = true,
)

@Serializable
data class SetWallpaperConfig(
    val imagePath: String = "",
    val target: String = "both", // "home", "lock", "both"
)

@Serializable
data class KeepDeviceAwakeConfig(
    val enable: Boolean = true,
    val durationMs: Long = 0, // 0 = indefinite until disabled
)

@Serializable
data class GpsEnableDisableConfig(
    val enable: Boolean = true,
)
