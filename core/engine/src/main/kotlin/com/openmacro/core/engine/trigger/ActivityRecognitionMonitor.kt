package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.ActivityRecognitionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Simple activity recognition using accelerometer data.
 * Uses step frequency and acceleration magnitude to estimate activity type.
 * This avoids a Google Play Services dependency (ActivityRecognitionClient).
 */
class ActivityRecognitionMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "activity_recognition"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var job: Job? = null
    private var lastDetectedActivity: String? = null

    // Acceleration samples for analysis
    private val recentMagnitudes = mutableListOf<Float>()
    private val maxSamples = 100 // ~5 seconds at SENSOR_DELAY_NORMAL

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (sensorManager != null) {
            updateConfigs(configs)
            return
        }
        this.configs = configs
        this.callback = onTrigger

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Periodically analyze accumulated samples
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(5000) // Analyze every 5 seconds
                analyzeActivity()
            }
        }
        Log.d(TAG, "Started monitoring activity recognition")
    }

    override fun stop() {
        job?.cancel()
        job = null
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        recentMagnitudes.clear()
        lastDetectedActivity = null
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        synchronized(recentMagnitudes) {
            recentMagnitudes.add(magnitude)
            if (recentMagnitudes.size > maxSamples) {
                recentMagnitudes.removeAt(0)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun analyzeActivity() {
        val samples = synchronized(recentMagnitudes) { recentMagnitudes.toList() }
        if (samples.size < 20) return

        // Calculate variance of acceleration magnitude
        val mean = samples.average().toFloat()
        val variance = samples.map { (it - mean) * (it - mean) }.average().toFloat()

        // Simple heuristic classification based on acceleration variance
        val activity = when {
            variance < 0.5f -> "still"
            variance < 3.0f -> "walking"
            variance < 15.0f -> "running"
            variance < 50.0f -> "cycling"
            else -> "driving"
        }

        if (activity == lastDetectedActivity) return
        lastDetectedActivity = activity

        // Simple confidence based on how clearly the variance falls in a range
        val confidence = 75 // Simplified — real implementation would be more nuanced

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<ActivityRecognitionConfig>(config.configJson)
            } catch (_: Exception) {
                ActivityRecognitionConfig()
            }

            if (activity == parsed.activityType && confidence >= parsed.confidenceThreshold) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf(
                            "activity" to activity,
                            "confidence" to confidence.toString(),
                        ),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "ActivityRecognitionMonitor"
    }
}
