package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.ScreenOrientationConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.abs

class ScreenOrientationMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "screen_orientation"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastOrientation: String? = null

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
            Log.d(TAG, "Started monitoring screen orientation")
        } else {
            Log.w(TAG, "No accelerometer sensor available")
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        lastOrientation = null
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = abs(event.values[0])
        val y = abs(event.values[1])

        val orientation = if (y > x) "portrait" else "landscape"
        if (orientation == lastOrientation) return
        lastOrientation = orientation

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<ScreenOrientationConfig>(config.configJson)
            } catch (_: Exception) {
                ScreenOrientationConfig()
            }

            if (orientation == parsed.orientation) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("orientation" to orientation),
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "ScreenOrientationMonitor"
    }
}
