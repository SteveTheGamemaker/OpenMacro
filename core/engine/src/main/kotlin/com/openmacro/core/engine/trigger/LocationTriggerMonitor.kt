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
import com.openmacro.core.model.config.LocationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocationTriggerMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "location"

    private var appContext: Context? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val firedSet = mutableSetOf<Long>()

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
                checkLocation()
                delay(POLL_INTERVAL_MS)
            }
        }
        Log.d(TAG, "Started location trigger monitoring (${configs.size} configs)")
    }

    override fun stop() {
        pollJob?.cancel()
        pollJob = null
        appContext = null
        callback = null
        configs = emptyList()
        firedSet.clear()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        firedSet.clear()
    }

    private fun checkLocation() {
        val ctx = appContext ?: return
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        @Suppress("DEPRECATION")
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<LocationConfig>(config.configJson)
            } catch (_: Exception) {
                LocationConfig()
            }
            if (parsed.latitude == 0.0 && parsed.longitude == 0.0) continue

            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                parsed.latitude, parsed.longitude,
                results
            )
            val isInside = results[0] <= parsed.radiusMeters

            if (isInside && config.id !in firedSet) {
                firedSet.add(config.id)
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "location_name" to parsed.locationName,
                            "latitude" to location.latitude.toString(),
                            "longitude" to location.longitude.toString(),
                        ),
                    )
                )
            } else if (!isInside) {
                firedSet.remove(config.id)
            }
        }
    }

    companion object {
        private const val TAG = "LocationTriggerMonitor"
        private const val POLL_INTERVAL_MS = 15_000L // 15 seconds
    }
}
