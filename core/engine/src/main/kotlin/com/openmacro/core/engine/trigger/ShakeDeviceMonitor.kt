package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.ShakeDeviceConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.sqrt

class ShakeDeviceMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "shake_device"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastShakeTime: Long = 0

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
            Log.d(TAG, "Started monitoring shake device")
        } else {
            Log.w(TAG, "No accelerometer sensor available")
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        lastShakeTime = 0
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Remove gravity component and calculate total acceleration
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

        val now = System.currentTimeMillis()

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<ShakeDeviceConfig>(config.configJson)
            } catch (_: Exception) {
                ShakeDeviceConfig()
            }

            if (acceleration >= parsed.sensitivity && (now - lastShakeTime) > parsed.shakeDurationMs) {
                lastShakeTime = now
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("acceleration" to acceleration.toString()),
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "ShakeDeviceMonitor"
    }
}
