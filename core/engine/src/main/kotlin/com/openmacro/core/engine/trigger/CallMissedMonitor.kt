package com.openmacro.core.engine.trigger

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.CallMissedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CallMissedMonitor @Inject constructor(
    private val callStateTracker: CallStateTracker,
) : TriggerMonitor {
    override val triggerTypeId = "call_missed"

    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var context: Context? = null
    private val stateCallback: (CallStateTracker.CallStateChange) -> Unit = { change ->
        // Missed call = transition from RINGING to IDLE (never went to OFFHOOK)
        if (change.state == TelephonyManager.CALL_STATE_IDLE &&
            change.previousState == TelephonyManager.CALL_STATE_RINGING
        ) {
            handleCallMissed(change.number)
        }
    }

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        this.configs = configs
        this.callback = onTrigger
        if (this.context == null) {
            this.context = context
            callStateTracker.addCallback(stateCallback)
            callStateTracker.start(context)
        }
        Log.d(TAG, "Started monitoring missed calls")
    }

    override fun stop() {
        callStateTracker.removeCallback(stateCallback)
        context?.let { callStateTracker.stop(it) }
        context = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring missed calls")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleCallMissed(number: String) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<CallMissedConfig>(config.configJson)
            } catch (_: Exception) {
                CallMissedConfig()
            }

            val numberMatch = parsed.numberFilter.isBlank() ||
                number.filter { it.isDigit() }.contains(parsed.numberFilter.filter { it.isDigit() })

            if (numberMatch) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("call_number" to number),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "CallMissedMonitor"
    }
}
