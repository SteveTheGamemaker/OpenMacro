package com.openmacro.core.engine.constraint

import android.content.Context
import com.openmacro.core.model.config.TimeOfDayConfig
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

class TimeOfDayChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "time_of_day"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<TimeOfDayConfig>(configJson)
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = config.startHour * 60 + config.startMinute
        val endMinutes = config.endHour * 60 + config.endMinute

        return if (startMinutes <= endMinutes) {
            // Same day range (e.g., 09:00 - 17:00)
            currentMinutes in startMinutes..endMinutes
        } else {
            // Crosses midnight (e.g., 22:00 - 06:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
}
