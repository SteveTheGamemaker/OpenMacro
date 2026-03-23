package com.openmacro.core.engine.di

import com.openmacro.core.engine.action.ActionHandler
import com.openmacro.core.engine.action.DisplayNotificationHandler
import com.openmacro.core.engine.action.LaunchApplicationHandler
import com.openmacro.core.engine.action.SetVolumeHandler
import com.openmacro.core.engine.action.VibrateHandler
import com.openmacro.core.engine.action.WaitHandler
import com.openmacro.core.engine.trigger.AppLaunchMonitor
import com.openmacro.core.engine.trigger.BatteryLevelMonitor
import com.openmacro.core.engine.trigger.DayTimeMonitor
import com.openmacro.core.engine.trigger.PowerConnectedMonitor
import com.openmacro.core.engine.trigger.ScreenOnOffMonitor
import com.openmacro.core.engine.trigger.TriggerMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {

    // ── Trigger Monitors ──

    @Binds @IntoSet
    abstract fun bindScreenOnOff(impl: ScreenOnOffMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindBatteryLevel(impl: BatteryLevelMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindPowerConnected(impl: PowerConnectedMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindDayTime(impl: DayTimeMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindAppLaunch(impl: AppLaunchMonitor): TriggerMonitor

    // ── Action Handlers ──

    @Binds @IntoSet
    abstract fun bindDisplayNotification(impl: DisplayNotificationHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindLaunchApplication(impl: LaunchApplicationHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindSetVolume(impl: SetVolumeHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindVibrate(impl: VibrateHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindWait(impl: WaitHandler): ActionHandler
}
