package com.openmacro.core.engine.trigger

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DataConnectivityChangeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DataConnectivityChangeMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "data_connectivity_change"

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (networkCallback != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger

        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handleConnectivityChange(connected = true)
            }

            override fun onLost(network: Network) {
                handleConnectivityChange(connected = false)
            }
        }

        cm.registerNetworkCallback(request, networkCallback!!)
        Log.d(TAG, "Started monitoring data connectivity changes")
    }

    override fun stop() {
        networkCallback?.let { cb ->
            try {
                val cm = appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                cm?.unregisterNetworkCallback(cb)
            } catch (_: Exception) {}
        }
        networkCallback = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring data connectivity changes")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleConnectivityChange(connected: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<DataConnectivityChangeConfig>(config.configJson)
            } catch (_: Exception) {
                DataConnectivityChangeConfig()
            }

            val shouldFire = (connected && parsed.onConnected) || (!connected && parsed.onDisconnected)
            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("data_connected" to connected.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "DataConnMonitor"
    }
}
