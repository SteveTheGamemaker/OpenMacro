package com.openmacro.core.engine.trigger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.GeofenceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeofenceMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "geofence"

    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    // Track whether each config was previously inside the geofence
    private val wasInside = mutableMapOf<Long, Boolean>()

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (pollJob != null) {
            updateConfigs(configs)
            return
        }
        this.appContext = context.applicationContext
        this.configs = configs
        this.callback = onTrigger

        pollJob = scope.launch {
            while (true) {
                checkGeofences()
                delay(POLL_INTERVAL_MS)
            }
        }
        Log.d(TAG, "Started geofence monitoring (${configs.size} configs)")
    }

    override fun stop() {
        pollJob?.cancel()
        pollJob = null
        appContext = null
        callback = null
        configs = emptyList()
        wasInside.clear()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        // Remove stale entries
        val activeIds = configs.map { it.id }.toSet()
        wasInside.keys.retainAll(activeIds)
    }

    private fun checkGeofences() {
        val ctx = appContext ?: return
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        @Suppress("DEPRECATION")
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<GeofenceConfig>(config.configJson)
            } catch (_: Exception) {
                GeofenceConfig()
            }
            if (parsed.latitude == 0.0 && parsed.longitude == 0.0) continue

            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                parsed.latitude, parsed.longitude,
                results
            )
            val isInside = results[0] <= parsed.radiusMeters
            val previouslyInside = wasInside[config.id]

            if (previouslyInside != null) {
                val shouldFire = (parsed.onEnter && isInside && !previouslyInside) ||
                    (parsed.onExit && !isInside && previouslyInside)
                if (shouldFire) {
                    val event = if (isInside) "enter" else "exit"
                    callback?.invoke(
                        TriggerEvent(
                            triggerTypeId = triggerTypeId,
                            data = mapOf(
                                "geofence_event" to event,
                                "geofence_name" to parsed.locationName,
                                "latitude" to location.latitude.toString(),
                                "longitude" to location.longitude.toString(),
                            ),
                        )
                    )
                }
            }
            wasInside[config.id] = isInside
        }
    }

    companion object {
        private const val TAG = "GeofenceMonitor"
        private const val POLL_INTERVAL_MS = 10_000L // 10 seconds
    }
}
