package com.openmacro.core.engine

import android.Manifest
import android.os.Build

/**
 * Maps trigger/action/constraint type IDs to the Android runtime permissions they require.
 * Only includes dangerous permissions that need runtime requests — normal permissions
 * (INTERNET, VIBRATE, etc.) are auto-granted at install time.
 */
object PermissionHelper {

    /**
     * Returns the list of dangerous runtime permissions required for a given trigger type.
     */
    fun triggerPermissions(typeId: String): List<String> = when (typeId) {
        "sms_received" -> listOf(Manifest.permission.RECEIVE_SMS)
        "sms_sent" -> listOf(Manifest.permission.READ_SMS)
        "call_incoming", "call_ended", "call_missed" -> listOf(Manifest.permission.READ_PHONE_STATE)
        "bluetooth_event" -> bluetoothPermissions()
        "wifi_ssid_transition" -> locationPermissions()
        "geofence", "location" -> locationPermissions()
        else -> emptyList()
    }

    /**
     * Returns the list of dangerous runtime permissions required for a given action type.
     */
    fun actionPermissions(typeId: String): List<String> = when (typeId) {
        "display_notification" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
        "send_sms" -> listOf(Manifest.permission.SEND_SMS)
        "make_call" -> listOf(Manifest.permission.CALL_PHONE)
        "bluetooth_configure" -> bluetoothPermissions()
        else -> emptyList()
    }

    /**
     * Returns the list of dangerous runtime permissions required for a given constraint type.
     */
    fun constraintPermissions(typeId: String): List<String> = when (typeId) {
        "wifi_connected" -> locationPermissions()
        "bluetooth_connected" -> bluetoothPermissions()
        "call_state" -> listOf(Manifest.permission.READ_PHONE_STATE)
        else -> emptyList()
    }

    private fun bluetoothPermissions(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            emptyList() // Pre-S bluetooth permissions are normal, not dangerous
        }

    private fun locationPermissions(): List<String> = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
}
