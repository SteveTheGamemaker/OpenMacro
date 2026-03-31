package com.openmacro.core.engine.trigger

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import com.openmacro.core.engine.TriggerEvent
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.TorchOnOffConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TorchOnOffMonitor @Inject constructor() : TriggerMonitor {
    override val triggerTypeId = "torch_on_off"

    private var cameraManager: CameraManager? = null
    private var torchCallback: CameraManager.TorchCallback? = null
    private var configs: List<TriggerConfig> = emptyList()
    private var callback: ((TriggerEvent) -> Unit)? = null

    override fun start(
        context: Context,
        configs: List<TriggerConfig>,
        onTrigger: (TriggerEvent) -> Unit,
    ) {
        if (cameraManager != null) {
            updateConfigs(configs)
            return
        }
        this.configs = configs
        this.callback = onTrigger

        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        torchCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                handleTorchEvent(enabled)
            }
        }

        cameraManager?.registerTorchCallback(torchCallback!!, null)
        Log.d(TAG, "Started monitoring torch on/off")
    }

    override fun stop() {
        torchCallback?.let { cb ->
            try { cameraManager?.unregisterTorchCallback(cb) } catch (_: Exception) {}
        }
        torchCallback = null
        cameraManager = null
        callback = null
        configs = emptyList()
        Log.d(TAG, "Stopped monitoring torch on/off")
    }

    override fun updateConfigs(configs: List<TriggerConfig>) {
        this.configs = configs
    }

    private fun handleTorchEvent(enabled: Boolean) {
        for (config in configs) {
            val parsed = try {
                Json.decodeFromString<TorchOnOffConfig>(config.configJson)
            } catch (_: Exception) {
                TorchOnOffConfig()
            }

            val shouldFire = (enabled && parsed.onTorchOn) ||
                    (!enabled && parsed.onTorchOff)

            if (shouldFire) {
                callback?.invoke(
                    TriggerEvent(
                        triggerTypeId = triggerTypeId,
                        data = mapOf("torch_on" to enabled.toString()),
                    )
                )
            }
        }
    }

    companion object {
        private const val TAG = "TorchOnOffMonitor"
    }
}
