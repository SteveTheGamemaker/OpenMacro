package com.openmacro.core.engine.constraint

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.openmacro.core.model.config.WifiConnectedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WifiConnectedChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "wifi_connected"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<WifiConnectedConfig>(configJson)
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false

        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return false

        // If SSID filter specified, check it
        val requiredSsid = config.ssid
        if (!requiredSsid.isNullOrBlank()) {
            @Suppress("DEPRECATION")
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val currentSsid = wm.connectionInfo?.ssid?.removeSurrounding("\"") ?: return false
            return currentSsid.equals(requiredSsid, ignoreCase = true)
        }

        return true
    }
}
