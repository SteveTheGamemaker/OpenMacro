package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.ProximitySensorConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ProximitySensorMonitor @Inject constructor() : TriggerMonitor, SensorEventListener {
    override val triggerTypeId = "proximity_sensor"

    private var sensorManager: SensorManager? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null
    private var lastNear: Boolean? = null

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
        val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximity != null) {
            sensorManager?.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Started monitoring proximity sensor")
        } else {
            Log.w(TAG, "No proximity sensor available")
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        callback = null
        configs = emptyList()
        lastNear = null
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val isNear = event.values[0] < event.sensor.maximumRange
        if (isNear == lastNear) return
        lastNear = isNear

        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<ProximitySensorConfig>(config.configJson)
            } catch (_: Exception) {
                ProximitySensorConfig()
            }

            val shouldFire = (isNear && parsed.onNear) || (!isNear && parsed.onFar)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("near" to isNear.toString()),
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "ProximitySensorMonitor"
    }
}
