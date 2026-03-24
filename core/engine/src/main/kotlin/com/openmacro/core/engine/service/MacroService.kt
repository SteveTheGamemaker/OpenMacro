package com.openmacro.core.engine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.engine.MacroDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MacroService : Service() {

    @Inject lateinit var macroDispatcher: MacroDispatcher
    @Inject lateinit var repository: MacroRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0))
        macroDispatcher.start(this)

        // Watch enabled macro count for notification updates
        serviceScope.launch {
            repository.observeEnabledMacros().collect { macros ->
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, buildNotification(macros.size))

                if (macros.isEmpty()) {
                    Log.i(TAG, "No enabled macros — stopping service")
                    stopSelf()
                }
            }
        }

        Log.i(TAG, "MacroService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        macroDispatcher.stop()
        serviceScope.cancel()
        Log.i(TAG, "MacroService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Macro Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps OpenMacro running in the background"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(enabledCount: Int): Notification {
        // Launch the main activity when tapping the notification
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val text = if (enabledCount == 0) "No active macros"
        else "$enabledCount macro${if (enabledCount != 1) "s" else ""} active"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.openmacro.core.engine.R.drawable.ic_notification)
            .setContentTitle("OpenMacro")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val TAG = "MacroService"
        private const val CHANNEL_ID = "macro_service"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, MacroService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MacroService::class.java))
        }

        suspend fun startIfNeeded(context: Context, repository: MacroRepository) {
            val enabledMacros = repository.observeEnabledMacros().first()
            if (enabledMacros.isNotEmpty()) {
                start(context)
            }
        }
    }
}
