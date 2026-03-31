package com.openmacro.core.engine.action

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.ScreenOnOffActionConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScreenOnOffActionHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "screen_on_off"

    @Suppress("DEPRECATION")
    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<ScreenOnOffActionConfig>(config.configJson)
        val ctx = context.androidContext

        if (parsed.turnOn) {
            val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "OpenMacro:ScreenOn"
            )
            wl.acquire(3000L)
            wl.release()
        } else {
            try {
                val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.lockNow()
            } catch (e: SecurityException) {
                Log.w(TAG, "lockNow() requires device admin permission", e)
            }
        }
    }

    companion object {
        private const val TAG = "ScreenOnOffActionHandler"
    }
}
