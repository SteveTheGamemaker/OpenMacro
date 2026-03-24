package com.openmacro.core.engine.trigger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.BluetoothEventConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BluetoothEventMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "bluetooth_event"

    private var receiver: BroadcastReceiver? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (receiver != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        when (state) {
                            BluetoothAdapter.STATE_ON -> handleAdapterState(true)
                            BluetoothAdapter.STATE_OFF -> handleAdapterState(false)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        handleDeviceEvent(device, connected = true)
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        handleDeviceEvent(device, connected = false)
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.applicationContext.registerReceiver(receiver, filter)
        Log.d(TAG, "Started monitoring Bluetooth events")
    }

    override fun stop() {
        receiver?.let { r ->
            try { appContext?.unregisterReceiver(r) } catch (_: Exception) {}
        }
        receiver = null
        appContext = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring Bluetooth events")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleAdapterState(enabled: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<BluetoothEventConfig>(config.configJson)
            } catch (_: Exception) {
                BluetoothEventConfig()
            }

            val shouldFire = (enabled && parsed.onEnabled) || (!enabled && parsed.onDisabled)
            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("bluetooth_enabled" to enabled.toString()),
                    )
                )
            }
        }
    }

    private fun handleDeviceEvent(device: BluetoothDevice?, connected: Boolean) {
        val address = try { device?.address } catch (_: SecurityException) { null } ?: ""
        val name = try { device?.name } catch (_: SecurityException) { null } ?: ""

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<BluetoothEventConfig>(config.configJson)
            } catch (_: Exception) {
                BluetoothEventConfig()
            }

            val addressMatch = parsed.deviceAddress.isBlank() ||
                parsed.deviceAddress.equals(address, ignoreCase = true)
            val shouldFire = addressMatch && (
                (connected && parsed.onDeviceConnected) || (!connected && parsed.onDeviceDisconnected)
            )

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "device_name" to name,
                            "device_address" to address,
                            "device_connected" to connected.toString(),
                        ),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "BluetoothEventMonitor"
    }
}
