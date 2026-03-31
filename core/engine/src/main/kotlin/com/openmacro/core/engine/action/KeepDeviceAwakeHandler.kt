package com.openmacro.core.engine.action

import android.content.Context
import android.os.PowerManager
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.KeepDeviceAwakeConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class KeepDeviceAwakeHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "keep_device_awake"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<KeepDeviceAwakeConfig>(config.configJson)
        val pm = context.androidContext.getSystemService(Context.POWER_SERVICE) as PowerManager

        if (parsed.enable) {
            wakeLock?.release()
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OpenMacro:KeepAwake")
            if (parsed.durationMs > 0) {
                wakeLock?.acquire(parsed.durationMs)
            } else {
                @Suppress("WakelockTimeout")
                wakeLock?.acquire()
            }
        } else {
            wakeLock?.release()
            wakeLock = null
        }
    }

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null
    }
}
