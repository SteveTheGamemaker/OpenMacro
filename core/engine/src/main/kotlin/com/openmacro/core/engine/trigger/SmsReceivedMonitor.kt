package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.SmsReceivedConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SmsReceivedMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "sms_received"

    private var receiver: BroadcastReceiver? = null
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
        this.configs = configs
        this.callback = onTrigger

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
                for (smsMessage in messages) {
                    val sender = smsMessage.originatingAddress ?: ""
                    val body = smsMessage.messageBody ?: ""
                    handleSmsReceived(sender, body)
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION),
        )
        Log.d(TAG, "Started monitoring SMS received")
    }

    override fun stop() {
        receiver = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring SMS received")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleSmsReceived(sender: String, message: String) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<SmsReceivedConfig>(config.configJson)
            } catch (_: Exception) {
                SmsReceivedConfig()
            }

            val senderMatch = parsed.senderFilter.isBlank() ||
                sender.contains(parsed.senderFilter, ignoreCase = true)

            if (senderMatch) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "sms_sender" to sender,
                            "sms_message" to message,
                        ),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceivedMonitor"
    }
}
