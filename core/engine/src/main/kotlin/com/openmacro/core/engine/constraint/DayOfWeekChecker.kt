package com.openmacro.core.engine.constraint

import android.content.Context
import com.openmacro.core.model.config.DayOfWeekConfig
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

class DayOfWeekChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "day_of_week"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<DayOfWeekConfig>(configJson)
        // Calendar: SUNDAY=1, MONDAY=2, ... SATURDAY=7
        // Our config: MONDAY=1, TUESDAY=2, ... SUNDAY=7
        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val ourDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
        return ourDay in config.days
    }
}
