package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.FlipDeviceConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FlipDeviceMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "flip_device"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastFaceDown: Boolean? = null

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
            Log.d(TAG, "Started monitoring flip device")
        } else {
            Log.w(TAG, "No accelerometer sensor available")
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        lastFaceDown = null
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val z = event.values[2]
        // Face down when z-axis gravity is negative (device flipped)
        val isFaceDown = z < -7.0f
        val isFaceUp = z > 7.0f

        val currentState = when {
            isFaceDown -> true
            isFaceUp -> false
            else -> return // Intermediate position, ignore
        }

        if (currentState == lastFaceDown) return
        lastFaceDown = currentState

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<FlipDeviceConfig>(config.configJson)
            } catch (_: Exception) {
                FlipDeviceConfig()
            }

            val shouldFire = (currentState && parsed.onFaceDown) ||
                    (!currentState && parsed.onFaceUp)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("face_down" to currentState.toString()),
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "FlipDeviceMonitor"
    }
}
