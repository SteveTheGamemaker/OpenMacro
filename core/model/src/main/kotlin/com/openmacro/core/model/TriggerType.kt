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
    SMS_SENT("sms_sent", "SMS Sent"),

    // Milestone 7 — Sensors
    SHAKE_DEVICE("shake_device", "Shake Device"),
    FLIP_DEVICE("flip_device", "Flip Device"),
    PROXIMITY_SENSOR("proximity_sensor", "Proximity Sensor"),
    LIGHT_SENSOR("light_sensor", "Light Sensor"),
    SCREEN_ORIENTATION("screen_orientation", "Screen Orientation"),
    ACTIVITY_RECOGNITION("activity_recognition", "Activity Recognition"),

    // Milestone 7 — Device State
    DEVICE_BOOT("device_boot", "Device Boot"),
    BATTERY_TEMPERATURE("battery_temperature", "Battery Temperature"),
    BATTERY_SAVER_STATE("battery_saver_state", "Battery Saver State"),
    DARK_THEME_CHANGE("dark_theme_change", "Dark Theme Change"),
    GPS_ENABLED_DISABLED("gps_enabled_disabled", "GPS Enabled/Disabled"),
    DO_NOT_DISTURB("do_not_disturb", "Do Not Disturb"),
    SILENT_MODE("silent_mode", "Silent Mode"),
    TORCH_ON_OFF("torch_on_off", "Torch On/Off"),

    // Milestone 7 — Location
    GEOFENCE("geofence", "Geofence"),
    LOCATION("location", "Location"),
    ;

    companion object {
        private val byTypeId = entries.associateBy { it.typeId }
        fun fromTypeId(typeId: String): TriggerType? = byTypeId[typeId]
    }
}
