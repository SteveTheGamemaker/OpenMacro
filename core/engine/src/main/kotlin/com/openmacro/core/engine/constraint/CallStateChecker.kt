package com.openmacro.core.engine.constraint

import android.content.Context
import android.telephony.TelephonyManager
import com.openmacro.core.model.config.CallStateConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CallStateChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "call_state"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<CallStateConstraintConfig>(configJson)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val currentState = try {
            when (tm.callState) {
                TelephonyManager.CALL_STATE_IDLE -> "idle"
                TelephonyManager.CALL_STATE_RINGING -> "ringing"
                TelephonyManager.CALL_STATE_OFFHOOK -> "offhook"
                else -> "idle"
            }
        } catch (_: SecurityException) {
            "idle"
        }

        return currentState == config.state
    }
}
