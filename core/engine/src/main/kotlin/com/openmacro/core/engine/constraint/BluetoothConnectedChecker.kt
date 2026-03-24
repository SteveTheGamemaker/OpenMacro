package com.openmacro.core.engine.constraint

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.openmacro.core.model.config.BluetoothConnectedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BluetoothConnectedChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "bluetooth_connected"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<BluetoothConnectedConfig>(configJson)
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter ?: return false

        return try {
            val connectedDevices = bm.getConnectedDevices(BluetoothProfile.GATT)
            if (config.deviceAddress.isBlank()) {
                connectedDevices.isNotEmpty()
            } else {
                connectedDevices.any { it.address.equals(config.deviceAddress, ignoreCase = true) }
            }
        } catch (_: SecurityException) {
            false
        }
    }
}
