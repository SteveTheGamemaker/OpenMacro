package com.openmacro.core.engine.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DayTimeReceiver : BroadcastReceiver() {
    @Inject lateinit var dayTimeMonitor: DayTimeMonitor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DayTimeMonitor.ACTION_DAY_TIME_TRIGGER) {
            val configIndex = intent.getIntExtra(DayTimeMonitor.EXTRA_CONFIG_INDEX, -1)
            if (configIndex >= 0) {
                dayTimeMonitor.onAlarmReceived(configIndex)
            }
        }
    }
}
