package com.openmacro.core.engine.trigger

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared telephony state tracker used by CallIncoming, CallEnded, and CallMissed monitors.
 * Maintains call state transitions to determine which trigger to fire.
 */
@Singleton
class CallStateTracker @Inject constructor() {

    data class CallStateChange(
        val state: Int,
        val number: String,
        val previousState: Int,
    )

    private var listener: Any? = null
    private var previousState = TelephonyManager.CALL_STATE_IDLE
    private var lastNumber = ""
    private var callbacks: MutableList<(CallStateChange) -> Unit> = mutableListOf()
    private var startCount = 0

    fun addCallback(cb: (CallStateChange) -> Unit) {
        callbacks.add(cb)
    }

    fun removeCallback(cb: (CallStateChange) -> Unit) {
        callbacks.remove(cb)
    }

    @Synchronized
    fun start(context: Context) {
        startCount++
        if (listener != null) return

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    handleStateChange(state)
                }
            }
            tm.registerTelephonyCallback(context.mainExecutor, telephonyCallback)
            listener = telephonyCallback
        } else {
            @Suppress("DEPRECATION")
            val phoneStateListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    lastNumber = phoneNumber ?: ""
                    handleStateChange(state)
                }
            }
            @Suppress("DEPRECATION")
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            listener = phoneStateListener
        }

        Log.d(TAG, "CallStateTracker started")
    }

    @Synchronized
    fun stop(context: Context) {
        startCount--
        if (startCount > 0) return

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val l = listener ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.unregisterTelephonyCallback(l as TelephonyCallback)
        } else {
            @Suppress("DEPRECATION")
            tm.listen(l as PhoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        listener = null
        previousState = TelephonyManager.CALL_STATE_IDLE
        Log.d(TAG, "CallStateTracker stopped")
    }

    private fun handleStateChange(state: Int) {
        val change = CallStateChange(
            state = state,
            number = lastNumber,
            previousState = previousState,
        )
        previousState = state

        for (cb in callbacks.toList()) {
            try {
                cb(change)
            } catch (e: Exception) {
                Log.e(TAG, "Error in callback", e)
            }
        }
    }

    companion object {
        private const val TAG = "CallStateTracker"
    }
}
