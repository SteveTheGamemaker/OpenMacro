package com.openmacro.core.engine.trigger

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.WifiSsidTransitionConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WifiSsidTransitionMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "wifi_ssid_transition"

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastSsid: String? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (networkCallback != null) {
            updateConfigs(configs)
            return
        }
        this.configs = configs
        this.callback = onTrigger

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION")
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                @Suppress("DEPRECATION")
                val ssid = wm.connectionInfo?.ssid?.removeSurrounding("\"") ?: return
                if (ssid == "<unknown ssid>") return
                val prevSsid = lastSsid
                lastSsid = ssid
                if (prevSsid != ssid) {
                    handleSsidTransition(ssid, connected = true)
                }
            }

            override fun onLost(network: Network) {
                val prevSsid = lastSsid
                lastSsid = null
                if (prevSsid != null) {
                    handleSsidTransition(prevSsid, connected = false)
                }
            }
        }

        cm.registerNetworkCallback(request, networkCallback!!)
        Log.d(TAG, "Started monitoring WiFi SSID transitions")
    }

    override fun stop() {
        networkCallback = null
        callback = null
        configs = emptyList()
        lastSsid = null
        Log.d(TAG, "Stopped monitoring WiFi SSID transitions")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleSsidTransition(ssid: String, connected: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<WifiSsidTransitionConfig>(config.configJson)
            } catch (_: Exception) {
                WifiSsidTransitionConfig()
            }

            val ssidMatch = parsed.ssid.isBlank() || parsed.ssid.equals(ssid, ignoreCase = true)
            val shouldFire = ssidMatch && (
                (connected && parsed.onConnect) || (!connected && parsed.onDisconnect)
            )

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "ssid" to ssid,
                            "wifi_connected" to connected.toString(),
                        ),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "WifiSsidTransitionMon"
    }
}
