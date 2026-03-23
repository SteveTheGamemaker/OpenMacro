package com.openmacro.core.model

/**
 * Registry of known trigger types. Each type has a unique [typeId] used for DB storage
 * and a display [displayName] for the UI.
 *
 * New trigger types are added here as new enum entries — purely additive.
 * The actual monitoring logic lives in the :core:engine module's TriggerRegistry.
 */
enum class TriggerType(val typeId: String, val displayName: String) {
    // Milestone 2
    SCREEN_ON_OFF("screen_on_off", "Screen On/Off"),
    BATTERY_LEVEL("battery_level", "Battery Level"),
    POWER_CONNECTED("power_connected", "Power Connected/Disconnected"),
    DAY_TIME("day_time", "Day/Time"),
    APP_LAUNCH("app_launch", "Application Launched/Closed"),

    // Milestone 5
    WIFI_STATE_CHANGE("wifi_state_change", "WiFi State Change"),
    WIFI_SSID_TRANSITION("wifi_ssid_transition", "WiFi SSID Transition"),
    BLUETOOTH_EVENT("bluetooth_event", "Bluetooth Event"),
    DATA_CONNECTIVITY_CHANGE("data_connectivity_change", "Data Connectivity Change"),
    AIRPLANE_MODE_CHANGED("airplane_mode_changed", "Airplane Mode Changed"),
    SMS_RECEIVED("sms_received", "SMS Received"),
    CALL_INCOMING("call_incoming", "Incoming Call"),
    CALL_ENDED("call_ended", "Call Ended"),
    CALL_MISSED("call_missed", "Missed Call"),
    REGULAR_INTERVAL("regular_interval", "Regular Interval"),
    ;

    companion object {
        private val byTypeId = entries.associateBy { it.typeId }
        fun fromTypeId(typeId: String): TriggerType? = byTypeId[typeId]
    }
}
