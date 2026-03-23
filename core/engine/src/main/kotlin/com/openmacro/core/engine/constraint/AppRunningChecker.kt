package com.openmacro.core.engine.constraint

import android.app.usage.UsageStatsManager
import android.content.Context
import com.openmacro.core.model.config.AppRunningConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AppRunningChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "app_running"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<AppRunningConfig>(configJson)
        if (config.packageName.isBlank()) return false

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 5000, now)

        return stats.any {
            it.packageName == config.packageName && it.lastTimeUsed >= now - 5000
        }
    }
}
