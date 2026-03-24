package com.openmacro.core.engine.action

import android.telephony.SmsManager
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SendSmsConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendSmsHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "send_sms"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SendSmsConfig>(config.configJson)
        if (parsed.phoneNumber.isBlank()) return

        @Suppress("DEPRECATION")
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(parsed.message)
        if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(parsed.phoneNumber, null, parts, null, null)
        } else {
            smsManager.sendTextMessage(parsed.phoneNumber, null, parsed.message, null, null)
        }
    }
}
