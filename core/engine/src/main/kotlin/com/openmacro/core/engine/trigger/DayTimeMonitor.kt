package com.openmacro.core.engine.trigger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DayTimeConfig
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

class DayTimeMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "day_time"

    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var context: Context? = null
    private val pendingIntents = mutableListOf<PendingIntent>()

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        this.context = context.applicationContext
        this.callback = onTrigger
        this.configs = configs
        scheduleAlarms()
    }

    override fun stop() {
        cancelAlarms()
        context = null
        callback = null
        configs = emptyList()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        cancelAlarms()
        scheduleAlarms()
    }

    private fun scheduleAlarms() {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for ((index, config) in configs.withIndex()) {
            scheduleNextAlarm(ctx, alarmManager, index, config)
        }
        Log.d(TAG, "Scheduled ${configs.size} day/time alarms")
    }

    private fun scheduleNextAlarm(
        ctx: Context,
        alarmManager: AlarmManager,
        index: Int,
        config: TriggerConfig,
    ) {
        val parsed = try {
            Json.decodeFromString<DayTimeConfig>(config.configJson)
        } catch (_: Exception) {
            DayTimeConfig()
        }

        val intent = Intent(ACTION_DAY_TIME_TRIGGER).apply {
            setPackage(ctx.packageName)
            putExtra(EXTRA_CONFIG_INDEX, index)
        }
        val pi = PendingIntent.getBroadcast(
            ctx,
            ALARM_REQUEST_BASE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        pendingIntents.add(pi)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parsed.hour)
            set(Calendar.MINUTE, parsed.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Use exact alarm to survive Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi,
                )
            } else {
                // Fall back to inexact if permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi,
                )
                Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted, using inexact alarm")
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pi,
            )
        }

        Log.d(TAG, "Alarm scheduled for ${calendar.time} (index=$index)")
    }

    private fun cancelAlarms() {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (pi in pendingIntents) {
            alarmManager.cancel(pi)
        }
        pendingIntents.clear()
    }

    fun onAlarmReceived(configIndex: Int) {
        val config = configs.getOrNull(configIndex)
        if (config == null) {
            Log.w(TAG, "Alarm received for invalid config index $configIndex (have ${configs.size} configs)")
            return
        }

        val parsed = try {
            Json.decodeFromString<DayTimeConfig>(config.configJson)
        } catch (_: Exception) {
            DayTimeConfig()
        }

        // Reschedule for tomorrow since we use one-shot exact alarms
        val ctx = context
        if (ctx != null) {
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            scheduleNextAlarm(ctx, alarmManager, configIndex, config)
        }

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (today !in parsed.daysOfWeek) {
            Log.d(TAG, "Alarm fired but today ($today) not in configured days ${parsed.daysOfWeek}, skipping")
            return
        }

        Log.d(TAG, "Day/Time trigger firing for config $configIndex")
        callback?.invoke(
            TriggerEvent(
                triggerTypeId = triggerTypeId,
                data = mapOf(
                    "hour" to parsed.hour.toString(),
                    "minute" to parsed.minute.toString(),
                ),
            )
        )
    }

    companion object {
        private const val TAG = "DayTimeMonitor"
        const val ACTION_DAY_TIME_TRIGGER = "com.openmacro.DAY_TIME_TRIGGER"
        const val EXTRA_CONFIG_INDEX = "config_index"
        private const val ALARM_REQUEST_BASE = 10000
    }
}
