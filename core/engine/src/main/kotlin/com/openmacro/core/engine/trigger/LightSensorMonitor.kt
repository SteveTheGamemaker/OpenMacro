package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.LightSensorConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LightSensorMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "light_sensor"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastLux: Float = -1f
    private val firedSet = mutableSetOf<Long>()

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
        val light = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (light != null) {
            sensorManager?.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Started monitoring light sensor")
        } else {
            Log.w(TAG, "No light sensor available")
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        lastLux = -1f
        firedSet.clear()
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
        firedSet.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_LIGHT) return

        val lux = event.values[0]
        if (lux == lastLux) return
        lastLux = lux

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<LightSensorConfig>(config.configJson)
            } catch (_: Exception) {
                LightSensorConfig()
            }

            val matches = if (parsed.whenBelow) lux <= parsed.threshold else lux >= parsed.threshold
            if (matches && config.id !in firedSet) {
                firedSet.add(config.id)
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("lux" to lux.toString()),
                    )
                )
            } else if (!matches) {
                firedSet.remove(config.id)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "LightSensorMonitor"
    }
}
