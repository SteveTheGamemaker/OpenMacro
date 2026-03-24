package com.openmacro.core.engine.trigger

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.CallIncomingConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CallIncomingMonitor @Inject constructor(
    private val callStateTracker: CallStateTracker,
) : TriggerMonitor {
    override val triggerTypeId = "call_incoming"

    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var context: Context? = null
    private val stateCallback: (CallStateTracker.CallStateChange) -> Unit = { change ->
        if (change.state == TelephonyManager.CALL_STATE_RINGING) {
            handleIncomingCall(change.number)
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
        Log.d(TAG, "Started monitoring incoming calls")
    }

    override fun stop() {
        callStateTracker.removeCallback(stateCallback)
        context?.let { callStateTracker.stop(it) }
        context = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring incoming calls")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleIncomingCall(number: String) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<CallIncomingConfig>(config.configJson)
            } catch (_: Exception) {
                CallIncomingConfig()
            }

            val numberMatch = parsed.numberFilter.isBlank() ||
                number.contains(parsed.numberFilter)

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
        private const val TAG = "CallIncomingMonitor"
    }
}
