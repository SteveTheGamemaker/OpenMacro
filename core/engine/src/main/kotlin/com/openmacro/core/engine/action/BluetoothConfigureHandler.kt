package com.openmacro.core.engine.action

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.BluetoothConfigureConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BluetoothConfigureHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "bluetooth_configure"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<BluetoothConfigureConfig>(config.configJson)
        val ctx = context.androidContext
        val bm = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter

        if (adapter == null) return

        try {
            @Suppress("DEPRECATION")
            if (parsed.enable) {
                adapter.enable()
            } else {
                adapter.disable()
            }
        } catch (_: SecurityException) {
            // On newer Android, these are restricted. Open Bluetooth settings as fallback.
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }
    }
}
