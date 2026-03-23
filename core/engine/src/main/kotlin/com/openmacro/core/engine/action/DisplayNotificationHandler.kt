package com.openmacro.core.engine.action

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.DisplayNotificationConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DisplayNotificationHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "display_notification"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<DisplayNotificationConfig>(config.configJson)
        val ctx = context.androidContext

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure channel exists
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Macro Notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        nm.createNotificationChannel(channel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("POST_NOTIFICATIONS permission not granted")
        }

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(parsed.title.ifEmpty { context.macroName })
            .setContentText(parsed.body)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_BASE_ID + context.logId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "macro_notifications"
        private const val NOTIFICATION_BASE_ID = 20000
    }
}
