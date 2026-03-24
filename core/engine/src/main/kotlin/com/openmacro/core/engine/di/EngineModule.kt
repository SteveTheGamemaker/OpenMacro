package com.openmacro.core.engine.di

import com.openmacro.core.engine.action.ActionHandler
import com.openmacro.core.engine.action.AirplaneModeHandler
import com.openmacro.core.engine.action.BluetoothConfigureHandler
import com.openmacro.core.engine.action.ClearVariablesHandler
import com.openmacro.core.engine.action.DeleteVariableHandler
import com.openmacro.core.engine.action.DisplayNotificationHandler
import com.openmacro.core.engine.action.FillClipboardHandler
import com.openmacro.core.engine.action.HttpRequestHandler
import com.openmacro.core.engine.action.LaunchApplicationHandler
import com.openmacro.core.engine.action.LaunchHomeScreenHandler
import com.openmacro.core.engine.action.MakeCallHandler
import com.openmacro.core.engine.action.OpenWebsiteHandler
import com.openmacro.core.engine.action.SendSmsHandler
import com.openmacro.core.engine.action.SetVariableHandler
import com.openmacro.core.engine.action.SetVolumeHandler
import com.openmacro.core.engine.action.SpeakTextHandler
import com.openmacro.core.engine.action.VibrateHandler
import com.openmacro.core.engine.action.WaitHandler
import com.openmacro.core.engine.action.WifiConfigureHandler
import com.openmacro.core.engine.constraint.AirplaneModeChecker
import com.openmacro.core.engine.constraint.AppRunningChecker
import com.openmacro.core.engine.constraint.BatteryLevelChecker
import com.openmacro.core.engine.constraint.BluetoothConnectedChecker
import com.openmacro.core.engine.constraint.CallStateChecker
import com.openmacro.core.engine.constraint.ConstraintChecker
import com.openmacro.core.engine.constraint.DayOfWeekChecker
import com.openmacro.core.engine.constraint.PowerConnectedChecker
import com.openmacro.core.engine.constraint.ScreenStateChecker
import com.openmacro.core.engine.constraint.TimeOfDayChecker
import com.openmacro.core.engine.constraint.VariableValueChecker
import com.openmacro.core.engine.constraint.WifiConnectedChecker
import com.openmacro.core.engine.constraint.WifiEnabledChecker
import com.openmacro.core.engine.trigger.AirplaneModeChangedMonitor
import com.openmacro.core.engine.trigger.AppLaunchMonitor
import com.openmacro.core.engine.trigger.BatteryLevelMonitor
import com.openmacro.core.engine.trigger.BluetoothEventMonitor
import com.openmacro.core.engine.trigger.CallEndedMonitor
import com.openmacro.core.engine.trigger.CallIncomingMonitor
import com.openmacro.core.engine.trigger.CallMissedMonitor
import com.openmacro.core.engine.trigger.DataConnectivityChangeMonitor
import com.openmacro.core.engine.trigger.DayTimeMonitor
import com.openmacro.core.engine.trigger.PowerConnectedMonitor
import com.openmacro.core.engine.trigger.RegularIntervalMonitor
import com.openmacro.core.engine.trigger.ScreenOnOffMonitor
import com.openmacro.core.engine.trigger.SmsReceivedMonitor
import com.openmacro.core.engine.trigger.TriggerMonitor
import com.openmacro.core.engine.trigger.WifiSsidTransitionMonitor
import com.openmacro.core.engine.trigger.WifiStateChangeMonitor
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

    // Milestone 5 Triggers

    @Binds @IntoSet
    abstract fun bindWifiStateChange(impl: WifiStateChangeMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindWifiSsidTransition(impl: WifiSsidTransitionMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindBluetoothEvent(impl: BluetoothEventMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindDataConnectivityChange(impl: DataConnectivityChangeMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindAirplaneModeChanged(impl: AirplaneModeChangedMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindSmsReceived(impl: SmsReceivedMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindCallIncoming(impl: CallIncomingMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindCallEnded(impl: CallEndedMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindCallMissed(impl: CallMissedMonitor): TriggerMonitor

    @Binds @IntoSet
    abstract fun bindRegularInterval(impl: RegularIntervalMonitor): TriggerMonitor

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

    @Binds @IntoSet
    abstract fun bindSetVariable(impl: SetVariableHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindDeleteVariable(impl: DeleteVariableHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindClearVariables(impl: ClearVariablesHandler): ActionHandler

    // Milestone 5 Actions

    @Binds @IntoSet
    abstract fun bindWifiConfigure(impl: WifiConfigureHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindBluetoothConfigure(impl: BluetoothConfigureHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindAirplaneMode(impl: AirplaneModeHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindSendSms(impl: SendSmsHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindMakeCall(impl: MakeCallHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindLaunchHomeScreen(impl: LaunchHomeScreenHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindOpenWebsite(impl: OpenWebsiteHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindHttpRequest(impl: HttpRequestHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindSpeakText(impl: SpeakTextHandler): ActionHandler

    @Binds @IntoSet
    abstract fun bindFillClipboard(impl: FillClipboardHandler): ActionHandler

    // ── Constraint Checkers ──

    @Binds @IntoSet
    abstract fun bindBatteryLevelChecker(impl: BatteryLevelChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindTimeOfDayChecker(impl: TimeOfDayChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindDayOfWeekChecker(impl: DayOfWeekChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindWifiConnectedChecker(impl: WifiConnectedChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindScreenStateChecker(impl: ScreenStateChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindPowerConnectedChecker(impl: PowerConnectedChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindAppRunningChecker(impl: AppRunningChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindVariableValueChecker(impl: VariableValueChecker): ConstraintChecker

    // Milestone 5 Constraints

    @Binds @IntoSet
    abstract fun bindBluetoothConnectedChecker(impl: BluetoothConnectedChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindWifiEnabledChecker(impl: WifiEnabledChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindAirplaneModeChecker(impl: AirplaneModeChecker): ConstraintChecker

    @Binds @IntoSet
    abstract fun bindCallStateChecker(impl: CallStateChecker): ConstraintChecker
}
