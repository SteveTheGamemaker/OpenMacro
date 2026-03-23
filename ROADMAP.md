# OpenMacro — Development Roadmap

## Architecture Overview

**Multi-module Gradle project:**
```
:app              — Activity, navigation, DI wiring
:core:common      — shared utilities, extension functions
:core:database    — Room database, DAOs, entities
:core:model       — pure Kotlin domain models (no Android deps)
:core:engine      — macro execution engine, trigger dispatch, constraint eval
:core:ui          — shared Compose components, theme
:feature:macros   — macro list + editor screens
:feature:logs     — execution log viewer
:feature:settings — app settings
```
Additional modules added in later milestones (`:feature:variables`, `:feature:actionblocks`, `:feature:location`, `:wear`).

**Tech stack:** Kotlin, Jetpack Compose (Material 3), Room, Hilt (DI), Kotlin Coroutines/Flows, Kotlinx Serialization, single-Activity architecture.

**Engine pattern (event-bus + registry):**
1. **TriggerMonitors** listen for system events → emit `TriggerEvent`
2. **MacroDispatcher** matches events to macros, evaluates constraints
3. **ConstraintEvaluator** evaluates AND/OR/XOR/NOT constraint trees
4. **ActionExecutor** runs actions sequentially, resolving Magic Text and variables

Each trigger/action/constraint type is registered via a **registry** (`TypeId → Handler`), so adding new types is purely additive.

**Foreground Service** (`MacroService`) keeps the engine alive. Starts when any macro is enabled, survives app close, restarts on device boot.

**Config storage:** Each trigger/action/constraint stores its parameters as a JSON blob in a `configJson` column. New types never require DB migrations.

---

## Progress

- [x] **Milestone 0 — Project Scaffolding**
- [ ] **Milestone 1 — Domain Model & Database**
- [ ] **Milestone 2 — Macro Engine Core + First Triggers/Actions**
- [ ] **Milestone 3 — Macro Editor UI**
- [ ] **Milestone 4 — Constraints + Variables + Magic Text**
- [ ] **Milestone 5 — Connectivity & Communication Expansion**
- [ ] **Milestone 6 — Flow Control, Expressions, Action Blocks**
- [ ] **Milestone 7 — Sensors, Location, Device State**
- [ ] **Milestone 8 — Notifications & App Events**
- [ ] **Milestone 9 — Accessibility & UI Interaction**
- [ ] **Milestone 10 — Advanced System Features**
- [ ] **Milestone 11 — Polish, Backup, Settings**
- [ ] **Milestone 12 — Community, Plugins, Wear OS**

---

## Milestone Details

### Milestone 0 — Project Scaffolding ✅
**Goal:** Working Android project that builds and runs.
- Multi-module Gradle setup with version catalog (`libs.versions.toml`)
- Hilt DI, Compose theme (Material 3, light/dark), single-Activity + Compose Navigation
- Placeholder home screen ("No macros yet")

### Milestone 1 — Domain Model & Database
**Goal:** Data foundation.
- Domain models: `Macro`, `TriggerConfig`, `ActionConfig`, `ConstraintConfig`, `MacroCategory`
- Sealed classes for `TriggerType`, `ActionType`, `ConstraintType`
- Room database with entities, DAOs, relations (`MacroWithDetails`)
- Repository layer with Flow-based queries
- Kotlinx Serialization for config JSON payloads
- **Tests:** Room instrumented tests, serialization round-trips

### Milestone 2 — Macro Engine Core + First Triggers/Actions
**Goal:** End-to-end macro execution working.
- **MacroService** foreground service with persistent notification
- TriggerMonitor framework + TriggerRegistry + TriggerDispatcher
- MacroDispatcher (trigger → macro lookup → action enqueue)
- ActionExecutor + ActionRegistry (sequential execution)
- ExecutionContext, execution logging (`MacroLog` entity + log viewer screen)
- **First 5 triggers:** ScreenOnOff, BatteryLevel, PowerConnected, DayTime, AppLaunch
- **First 5 actions:** DisplayNotification, LaunchApplication, SetVolume, Vibrate, Wait
- Runtime permission request flow
- **Tests:** Dispatcher logic, executor sequencing, integration test (insert macro → trigger → verify notification)

### Milestone 3 — Macro Editor UI
**Goal:** Real user-facing creation/editing experience.
- Macro list screen (grouped by category, enable toggle, search, swipe-delete)
- Macro editor (name, category, trigger/action/constraint sections, drag-reorder actions)
- Trigger/action configuration sheets (type selector grid → type-specific config)
- Shared components: AppPicker, TimePickerDialog, SliderWithLabel, ConfigCard
- Bottom navigation: Macros, Logs, Settings
- **Tests:** Compose UI tests for create/edit flow

### Milestone 4 — Constraints + Variables + Magic Text
**Goal:** Conditional execution and data flow.
- **Constraint evaluator:** tree-based AND/OR/XOR/NOT evaluation
- **8 constraints:** BatteryLevel, TimeOfDay, DayOfWeek, WifiConnected, ScreenState, PowerConnected, AppRunning, VariableValue
- Constraint editor UI (flat list with logic operator, evolve to nested groups later)
- **Variable system:** types (bool, int, decimal, string), VariableStore (memory + Room), global vs local scope
- **3 variable actions:** SetVariable, DeleteVariable, ClearVariables
- Variable manager screen + variable picker component
- **Magic Text resolver:** `{battery_level}`, `{time}`, `{date}`, `{device_name}`, `{v_myvar}`, `{lv_localvar}`
- **Tests:** Constraint tree evaluation combinatorics, variable store CRUD, MagicText edge cases

### Milestone 5 — Connectivity & Communication Expansion
**Goal:** High-value automation categories.
- **10 triggers:** WifiStateChange, WifiSsidTransition, BluetoothEvent, DataConnectivityChange, AirplaneModeChanged, SmsReceived, CallIncoming, CallEnded, CallMissed, RegularInterval
- **10 actions:** WifiConfigure, BluetoothConfigure, AirplaneMode, SendSms, MakeCall, LaunchHomeScreen, OpenWebsite, HttpRequest, SpeakText, FillClipboard
- **4 constraints:** BluetoothConnected, WifiEnabled, AirplaneMode, CallState
- Magic Text: `{ssid}`, `{call_number}`, `{sms_message}`, `{http_response_body}`, etc.
- Permission flow improvements (grouped requests)

### Milestone 6 — Flow Control, Expressions, Action Blocks
**Goal:** Programmable automation engine.
- **7 flow control actions:** IfClause, RepeatActions, IterateDictionaryArray, BreakFromLoop, ContinueLoop, WaitUntilTrigger, CancelMacroActions
- **Expression engine:** math, string, boolean expressions with parser
- **Advanced variable types:** Dictionary, Array + manipulation actions + JsonParse + TextManipulation
- **Action Blocks:** reusable function-like groups with input/output params, local scoping
- Action executor refactor: flat list → action tree with execution stack
- Editor UI: nested action indentation, expression editor, variable autocomplete

### Milestone 7 — Sensors, Location, Device State
**Goal:** Hardware integration.
- **6 sensor triggers:** ShakeDevice, FlipDevice, ProximitySensor, LightSensor, ScreenOrientation, ActivityRecognition
- **Location system:** GeofenceTrigger, LocationTrigger, geofence management UI (map view), FusedLocationProvider
- **8 device state triggers:** DeviceBoot, BatteryTemperature, BatterySaverState, DarkThemeChange, GpsEnabledDisabled, DoNotDisturb, SilentMode, TorchOnOff
- **8 device actions:** Brightness, ScreenOnOff, ForceScreenRotation, AutoRotate, DarkTheme, SetWallpaper, KeepDeviceAwake, GpsEnableDisable
- **4 constraints:** Location, Headphones, DND, SilentMode
- Battery-aware sensor monitoring

### Milestone 8 — Notifications & App Events
**Goal:** Deep notification system integration.
- NotificationListenerService (guided setup flow)
- **4 triggers:** NotificationReceived (with regex filters), NotificationBarButton, ClipboardChange, MediaTrackChanged
- **6 notification actions:** Enhanced DisplayNotification, BubbleNotification, ClearNotifications, NotificationInteraction, NotificationReply, HeadsUpControl
- **4 app triggers:** AppInstallRemoveUpdate, improved AppLaunchedClosed, MediaButtonPressed, MusicPlaying
- **4 app actions:** KillApplication, KillBackgroundProcesses, AppEnableDisable, ClearAppData

### Milestone 9 — Accessibility & UI Interaction
**Goal:** Interact with any app's UI.
- Custom AccessibilityService with node tree traversal
- **5 UI triggers:** UiClick, ScreenContent, FloatingButton, QuickSettingsTile, WidgetButton
- **7 UI actions:** UiInteraction (click/swipe by text/ID/coords), TouchScreen, PressBackButton, CheckTextOnScreen, ReadScreenContents, GetTextFromViewId, CheckPixelColour
- Overlay system: FloatingButton, FloatingText, OverlayBar, dialogs
- Quick Settings TileService (configurable macro trigger)
- Home screen widget (AppWidgetProvider)

### Milestone 10 — Advanced System Features
**Goal:** Power user features.
- Shell script execution (with root support), output capture to variables
- Intent system: IntentReceived trigger + SendIntent action + builder UI
- **6 system triggers:** Webhook, HttpServerRequest, LogcatMessage, SystemSettingChange, MacroFinished, VariableChange
- **8 system actions:** ShellScript, SystemSetting, SecureSettings, Reboot, SetScreenTimeout, SetScreenLock, ExpandCollapseStatusBar, DemoMode
- **4 file actions:** FileOperation, WriteToFile, ExportLog, OpenFile
- Embedded HTTP server for webhooks

### Milestone 11 — Polish, Backup, Settings
**Goal:** Production quality.
- Backup/restore (JSON export/import, auto-backup, cloud backup placeholder)
- Macro import/export as `.omacro` files + share sheet integration
- Comprehensive settings screen (service, permissions dashboard, advanced)
- Onboarding flow + first-macro wizard
- UX polish: animations, empty states, error handling, tablet layouts
- Remaining triggers: CalendarEvent, SunriseSunset, Stopwatch system, SIMCardChange, FoldState
- Remaining actions: PlaySound, TakePicture, TakeScreenshot, OCR (ML Kit), Translate, BiometricAuth, CalendarAddEvent

### Milestone 12 — Community, Plugins, Wear OS
**Goal:** Platform features.
- Community template sharing (browse, upload, rate, report) with backend API
- Plugin system: third-party trigger/action registration + Tasker/Locale plugin compatibility
- Wear OS companion app: macro list, trigger macros, watch complications
- JavaScript code action (embedded JS engine)
- UDP command action, QR code sharing, deep links

---

## Database Schema

```
macros(id, name, categoryId, isEnabled, sortOrder, createdAt, updatedAt)
categories(id, name, color, iconName, sortOrder)
trigger_configs(id, macroId, type, configJson, isEnabled, sortOrder)
action_configs(id, macroId, actionBlockId, type, configJson, sortOrder, isEnabled, parentActionId)
constraint_configs(id, macroId, parentConstraintId, type, configJson, logicOperator, sortOrder)
variables(id, name, type, valueJson, isGlobal)
action_blocks(id, name, description, inputParamsJson, outputParamsJson, createdAt, updatedAt)
geofences(id, name, latitude, longitude, radiusMeters, dwellTimeMs)
macro_logs(id, macroId, macroName, triggerType, startedAt, completedAt, status, errorMessage)
stopwatches(id, name, startTimeMs, accumulatedMs, isRunning)
```

Key: `configJson` blobs avoid migrations for new types. Self-referential FKs on `action_configs.parentActionId` and `constraint_configs.parentConstraintId` enable nesting.

---

## Cross-Cutting Concerns

- **Permissions:** Each type declares required permissions. Editor prompts on add. Settings has a permission dashboard.
- **Foreground Service:** Starts when any macro enabled, restarts on boot, notification shows active macro count.
- **Error handling:** Actions wrapped in try-catch, failures logged. Configurable behavior: stop / skip / retry.
- **Performance:** BroadcastReceivers over polling. Sensor delay NORMAL+. Geofencing over continuous GPS. Max 10 concurrent executions.
