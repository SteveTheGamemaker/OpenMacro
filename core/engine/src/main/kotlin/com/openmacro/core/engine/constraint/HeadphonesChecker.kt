package com.openmacro.core.engine.constraint

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.openmacro.core.model.config.HeadphonesConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HeadphonesChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "headphones"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<HeadphonesConstraintConfig>(configJson)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val headphonesConnected = devices.any { device ->
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            device.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }

        return headphonesConnected == config.connected
    }
}
