package com.openmacro.core.engine.text

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import com.openmacro.core.engine.ExecutionContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves {magic_text} tokens in strings.
 *
 * Built-in tokens: {battery_level}, {time}, {date}, {device_name}
 * Variable tokens: {v_name} for globals, {lv_name} for locals
 * Trigger data: {trigger_<key>} for trigger event data
 *
 * Unknown tokens are left as-is.
 */
@Singleton
class MagicTextResolver @Inject constructor() {

    private val tokenPattern = Regex("\\{([^}]+)\\}")

    fun resolve(text: String, context: ExecutionContext): String {
        if (!text.contains('{')) return text

        return tokenPattern.replace(text) { match ->
            val token = match.groupValues[1]
            resolveToken(token, context) ?: match.value
        }
    }

    private fun resolveToken(token: String, context: ExecutionContext): String? {
        // Built-in tokens
        when (token) {
            "battery_level" -> return getBatteryLevel(context.androidContext)
            "time" -> return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            "date" -> return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            "device_name" -> return Build.MODEL
            "macro_name" -> return context.macroName
            "trigger_type" -> return context.triggerEvent.triggerTypeId
            "ssid" -> return getCurrentSsid(context.androidContext)
            "call_number" -> return context.triggerEvent.data["call_number"]
                ?: context.localVariables["call_number"]
            "sms_message" -> return context.triggerEvent.data["sms_message"]
                ?: context.localVariables["sms_message"]
            "sms_sender" -> return context.triggerEvent.data["sms_sender"]
                ?: context.localVariables["sms_sender"]
            "http_response_body" -> return context.localVariables["http_response_body"]
            "http_response_code" -> return context.localVariables["http_response_code"]
        }

        // Trigger data tokens: {trigger_<key>}
        if (token.startsWith("trigger_")) {
            val key = token.removePrefix("trigger_")
            return context.triggerEvent.data[key]
        }

        // Local variable tokens: {lv_<name>}
        if (token.startsWith("lv_")) {
            return context.localVariables[token]
        }

        // Global variable tokens: {v_<name>}
        if (token.startsWith("v_")) {
            val name = token.removePrefix("v_")
            return context.variableStore?.getGlobal(name)
        }

        return null
    }

    @Suppress("DEPRECATION")
    private fun getCurrentSsid(context: Context): String {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ssid = wm.connectionInfo?.ssid?.removeSurrounding("\"") ?: ""
            if (ssid == "<unknown ssid>") "" else ssid
        } catch (_: Exception) {
            ""
        }
    }

    private fun getBatteryLevel(context: Context): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return batteryIntent?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            ((level * 100) / scale).toString()
        } ?: "?"
    }
}
