package com.openmacro.core.model

import com.openmacro.core.model.config.AppLaunchConfig
import com.openmacro.core.model.config.BatteryLevelConstraintConfig
import com.openmacro.core.model.config.BatteryLevelTriggerConfig
import com.openmacro.core.model.config.DayOfWeekConfig
import com.openmacro.core.model.config.DayTimeConfig
import com.openmacro.core.model.config.DisplayNotificationConfig
import com.openmacro.core.model.config.LaunchApplicationConfig
import com.openmacro.core.model.config.PowerConnectedConfig
import com.openmacro.core.model.config.RegularIntervalConfig
import com.openmacro.core.model.config.ScreenOnOffConfig
import com.openmacro.core.model.config.SetVolumeConfig
import com.openmacro.core.model.config.TimeOfDayConfig
import com.openmacro.core.model.config.VariableValueConfig
import com.openmacro.core.model.config.VibrateConfig
import com.openmacro.core.model.config.WaitConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Trigger configs ──

    @Test
    fun `ScreenOnOffConfig round-trip`() {
        val config = ScreenOnOffConfig(onScreenOn = false, onScreenOff = true)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<ScreenOnOffConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `BatteryLevelTriggerConfig round-trip`() {
        val config = BatteryLevelTriggerConfig(threshold = 50, whenBelow = false)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<BatteryLevelTriggerConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `PowerConnectedConfig round-trip`() {
        val config = PowerConnectedConfig(onConnect = false, onDisconnect = true)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<PowerConnectedConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `DayTimeConfig round-trip`() {
        val config = DayTimeConfig(hour = 14, minute = 30, daysOfWeek = listOf(6, 7))
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<DayTimeConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `AppLaunchConfig round-trip`() {
        val config = AppLaunchConfig(packageName = "com.example.app", onLaunch = true, onClose = true)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<AppLaunchConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `RegularIntervalConfig round-trip`() {
        val config = RegularIntervalConfig(intervalMs = 300_000)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<RegularIntervalConfig>(encoded)
        assertEquals(config, decoded)
    }

    // ── Action configs ──

    @Test
    fun `DisplayNotificationConfig round-trip`() {
        val config = DisplayNotificationConfig(title = "Hello", body = "World", channelId = "alerts")
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<DisplayNotificationConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `LaunchApplicationConfig round-trip`() {
        val config = LaunchApplicationConfig(packageName = "com.example.app")
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<LaunchApplicationConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `SetVolumeConfig round-trip`() {
        val config = SetVolumeConfig(streamType = 2, level = 75)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<SetVolumeConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `VibrateConfig round-trip`() {
        val config = VibrateConfig(durationMs = 1000, pattern = listOf(0, 200, 100, 200))
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<VibrateConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `WaitConfig round-trip`() {
        val config = WaitConfig(durationMs = 5000)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<WaitConfig>(encoded)
        assertEquals(config, decoded)
    }

    // ── Constraint configs ──

    @Test
    fun `BatteryLevelConstraintConfig round-trip`() {
        val config = BatteryLevelConstraintConfig(minLevel = 20, maxLevel = 80)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<BatteryLevelConstraintConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `TimeOfDayConfig round-trip`() {
        val config = TimeOfDayConfig(startHour = 22, startMinute = 0, endHour = 6, endMinute = 0)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<TimeOfDayConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `DayOfWeekConfig round-trip`() {
        val config = DayOfWeekConfig(days = listOf(1, 3, 5))
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<DayOfWeekConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `VariableValueConfig round-trip`() {
        val config = VariableValueConfig(variableName = "counter", operator = ">=", value = "10")
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<VariableValueConfig>(encoded)
        assertEquals(config, decoded)
    }

    // ── Edge cases ──

    @Test
    fun `default values are preserved through serialization`() {
        val config = ScreenOnOffConfig()
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<ScreenOnOffConfig>(encoded)
        assertEquals(true, decoded.onScreenOn)
        assertEquals(false, decoded.onScreenOff)
    }

    @Test
    fun `unknown keys are ignored during deserialization`() {
        val jsonString = """{"onScreenOn":true,"onScreenOff":false,"futureField":"value"}"""
        val decoded = json.decodeFromString<ScreenOnOffConfig>(jsonString)
        assertEquals(ScreenOnOffConfig(onScreenOn = true, onScreenOff = false), decoded)
    }
}
