package com.openmacro.core.engine.trigger

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.SmsSentConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SmsSentMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "sms_sent"

    private var observer: ContentObserver? = null
    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastSentId: Long = -1L

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (observer != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger

        // Seed lastSentId so we don't fire for existing messages
        lastSentId = getLatestSentId(context.applicationContext)

        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                checkForNewSentSms()
            }
        }

        context.applicationContext.contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            observer!!,
        )
        Log.d(TAG, "Started monitoring SMS sent")
    }

    override fun stop() {
        observer?.let { obs ->
            try { appContext?.contentResolver?.unregisterContentObserver(obs) } catch (_: Exception) {}
        }
        observer = null
        appContext = null
        callback = null
        configs = emptyList()
        lastSentId = -1L
        Log.d(TAG, "Stopped monitoring SMS sent")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun getLatestSentId(context: Context): Long {
        return try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                null, null,
                "${Telephony.Sms._ID} DESC LIMIT 1",
            )
            cursor?.use {
                if (it.moveToFirst()) it.getLong(0) else 0L
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun checkForNewSentSms() {
        val ctx = appContext ?: return
        try {
            val cursor = ctx.contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
                "${Telephony.Sms._ID} > ?",
                arrayOf(lastSentId.toString()),
                "${Telephony.Sms._ID} ASC",
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val address = it.getString(1) ?: ""
                    val body = it.getString(2) ?: ""
                    lastSentId = id
                    handleSmsSent(address, body)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking sent SMS", e)
        }
    }

    private fun handleSmsSent(recipient: String, message: String) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<SmsSentConfig>(config.configJson)
            } catch (_: Exception) {
                SmsSentConfig()
            }

            val recipientMatch = parsed.recipientFilter.isBlank() ||
                recipient.filter { it.isDigit() }.contains(parsed.recipientFilter.filter { it.isDigit() })

            if (recipientMatch) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "sms_recipient" to recipient,
                            "sms_message" to message,
                        ),
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "SmsSentMonitor"
    }
}
