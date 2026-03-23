package com.openmacro.core.model

/**
 * Registry of known constraint types. Each type has a unique [typeId] used for DB storage.
 * New constraint types are added as new enum entries — purely additive.
 */
enum class ConstraintType(val typeId: String, val displayName: String) {
    // Milestone 4
    BATTERY_LEVEL("battery_level", "Battery Level"),
    TIME_OF_DAY("time_of_day", "Time of Day"),
    DAY_OF_WEEK("day_of_week", "Day of Week"),
    WIFI_CONNECTED("wifi_connected", "WiFi Connected"),
    SCREEN_STATE("screen_state", "Screen State"),
    POWER_CONNECTED("power_connected", "Power Connected"),
    APP_RUNNING("app_running", "App Running"),
    VARIABLE_VALUE("variable_value", "Variable Value"),

    // Milestone 5
    BLUETOOTH_CONNECTED("bluetooth_connected", "Bluetooth Connected"),
    WIFI_ENABLED("wifi_enabled", "WiFi Enabled"),
    AIRPLANE_MODE("airplane_mode", "Airplane Mode"),
    CALL_STATE("call_state", "Call State"),
    ;

    companion object {
        private val byTypeId = entries.associateBy { it.typeId }
        fun fromTypeId(typeId: String): ConstraintType? = byTypeId[typeId]
    }
}
