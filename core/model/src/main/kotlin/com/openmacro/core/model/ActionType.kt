package com.openmacro.core.model

/**
 * Registry of known action types. Each type has a unique [typeId] used for DB storage.
 * New action types are added as new enum entries — purely additive.
 */
enum class ActionType(val typeId: String, val displayName: String) {
    // Milestone 2
    DISPLAY_NOTIFICATION("display_notification", "Display Notification"),
    LAUNCH_APPLICATION("launch_application", "Launch Application"),
    SET_VOLUME("set_volume", "Set Volume"),
    VIBRATE("vibrate", "Vibrate"),
    WAIT("wait", "Wait"),

    // Milestone 4 — Variables
    SET_VARIABLE("set_variable", "Set Variable"),
    DELETE_VARIABLE("delete_variable", "Delete Variable"),
    CLEAR_VARIABLES("clear_variables", "Clear Variables"),

    // Milestone 5 — Connectivity
    WIFI_CONFIGURE("wifi_configure", "WiFi Configure"),
    BLUETOOTH_CONFIGURE("bluetooth_configure", "Bluetooth Configure"),
    AIRPLANE_MODE("airplane_mode", "Airplane Mode"),
    SEND_SMS("send_sms", "Send SMS"),
    MAKE_CALL("make_call", "Make Call"),
    LAUNCH_HOME_SCREEN("launch_home_screen", "Launch Home Screen"),
    OPEN_WEBSITE("open_website", "Open Website"),
    HTTP_REQUEST("http_request", "HTTP Request"),
    SPEAK_TEXT("speak_text", "Speak Text"),
    FILL_CLIPBOARD("fill_clipboard", "Fill Clipboard"),

    // Milestone 6 — Flow Control
    IF_CLAUSE("if_clause", "If / Else"),
    ELSE_MARKER("else_marker", "Else"),
    REPEAT("repeat", "Repeat"),
    BREAK("break_loop", "Break Loop"),
    CONTINUE("continue_loop", "Continue Loop"),
    CANCEL_MACRO("cancel_macro", "Cancel Macro"),
    ITERATE("iterate", "Iterate"),
    ARRAY_MANIPULATION("array_manipulation", "Array Manipulation"),
    JSON_PARSE("json_parse", "JSON Parse"),
    TEXT_MANIPULATION("text_manipulation", "Text Manipulation"),
    WAIT_UNTIL_TRIGGER("wait_until_trigger", "Wait Until Trigger"),
    RUN_ACTION_BLOCK("run_action_block", "Run Action Block"),

    // Milestone 7 — Device Actions
    SET_BRIGHTNESS("set_brightness", "Set Brightness"),
    SCREEN_ON_OFF("screen_on_off", "Screen On/Off"),
    FORCE_SCREEN_ROTATION("force_screen_rotation", "Force Screen Rotation"),
    AUTO_ROTATE("auto_rotate", "Auto Rotate"),
    DARK_THEME("dark_theme", "Dark Theme"),
    SET_WALLPAPER("set_wallpaper", "Set Wallpaper"),
    KEEP_DEVICE_AWAKE("keep_device_awake", "Keep Device Awake"),
    GPS_ENABLE_DISABLE("gps_enable_disable", "GPS Enable/Disable"),
    ;

    companion object {
        private val byTypeId = entries.associateBy { it.typeId }
        fun fromTypeId(typeId: String): ActionType? = byTypeId[typeId]
    }
}
