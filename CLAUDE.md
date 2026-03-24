# OpenMacro — Claude Code Instructions

## Project Overview
OpenMacro is a free, open-source Android macro automation app (like Tasker/MacroDroid). Users create macros with triggers, actions, and constraints to automate their device.

## Developer Context
- The user (Jonah) has no Android/mobile dev experience but understands systems and web programming
- Claude is the lead developer — make architecture decisions and implement directly
- Frame Android concepts in terms of web dev analogues when explaining

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, single-Activity architecture
- **DI:** Hilt
- **Database:** Room
- **Async:** Kotlin Coroutines/Flows
- **Serialization:** Kotlinx Serialization
- **Navigation:** Compose Navigation
- **Min SDK:** 28 (Android 9), Target SDK: 35

## Project Structure
Multi-module Gradle project:
- `:app` — Activity, navigation, DI wiring
- `:core:common` — shared utilities
- `:core:model` — pure Kotlin domain models
- `:core:database` — Room database, DAOs, entities
- `:core:engine` — macro execution engine
- `:core:ui` — shared Compose components, theme
- `:feature:macros` — macro list + editor screens
- `:feature:logs` — execution log viewer
- `:feature:settings` — app settings

## Development Approach
- **One milestone per session.** Read `ROADMAP.md` for the full plan and current progress.
- Check the progress checklist in `ROADMAP.md` to see which milestone is next.
- After completing a milestone, update the progress checklist in `ROADMAP.md` (check the box) and add any important notes as a new section in CLAUDE.md.
- Commit and push at the end of each milestone after the user has tested the build and any new features on their android device using android studio.
- `feature_list.txt` has the full end-goal feature set for reference — don't try to implement it all at once.

## Building
```
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

## Key Architecture Decisions
- **Registry pattern** for triggers/actions/constraints — adding new types is purely additive
- **configJson** columns store type-specific parameters as JSON blobs — no DB migrations for new types
- **Foreground Service** (`MacroService`) keeps the engine alive in the background
- **Event-bus pattern:** TriggerMonitors → MacroDispatcher → ConstraintEvaluator → ActionExecutor

## Milestone 1 Notes
*Notes taken after finishing the first milestone (Domain Model & Database).*

- `MacroRepository.saveMacroWithDetails()` does a delete-then-reinsert for child entities (triggers, actions, constraints). This means child IDs change on every save. If we ever need stable IDs — e.g., for undo/redo, referencing specific actions from flow control, or syncing — we'll want to switch to a diff-based upsert instead. Fine for now.

## Milestone 2 Notes
*Notes taken after finishing Milestone 2 (Macro Engine Core + First Triggers/Actions).*

- **Engine architecture:** `MacroDispatcher` is the central orchestrator. It observes enabled macros via Flow, starts/stops `TriggerMonitor` instances as macros are enabled/disabled, and routes `TriggerEvent`s to the `ActionExecutor`.
- **Registry pattern works well:** `TriggerRegistry` and `ActionRegistry` use Hilt multibindings (`Set<TriggerMonitor>`, `Set<ActionHandler>`). Adding a new trigger/action = implement the interface + add one `@Binds @IntoSet` line in `EngineModule`.
- **BroadcastReceiver lifecycle:** Screen, power, and battery monitors register receivers in `start()` but unregistration relies on the service stopping (context destruction). If we ever need to unregister explicitly, we'll need to hold the context reference in each monitor.
- **DayTime trigger uses AlarmManager** with `setRepeating()`. This won't be exact on newer Android versions (Doze). If precision matters, we'll need `setExactAndAllowWhileIdle()` + re-scheduling after each alarm.
- **AppLaunch trigger polls UsageStatsManager** every 2 seconds. Requires the user to grant PACKAGE_USAGE_STATS via system settings (not a runtime permission dialog). We'll need a guided setup flow for this in a future milestone.
- **MacroService** is a foreground service with `specialUse` type. It self-stops when no macros are enabled.
- **No constraint evaluation yet** — that's Milestone 4. All enabled macros fire when their trigger matches.

## Milestone 3 Notes
*Notes taken after finishing Milestone 3 (Macro Editor UI).*

- **Editor architecture:** `MacroEditorViewModel` holds all state in-memory via `MutableStateFlow<MacroEditorUiState>`. Nothing is persisted until the user taps Save, which calls `saveMacroWithDetails()`. This avoids DB orphans from abandoned edits.
- **Navigation:** `MacroEditorRoute(macroId: Long = -1L)` — `-1` means new macro, any other value loads existing. The ViewModel reads `macroId` from `SavedStateHandle`.
- **Type picker pattern:** `TypePickerSheet` is a reusable bottom sheet grid that works for both triggers and actions. Each type is a `TypeItem(id, displayName, icon)`. Only M2-implemented types are shown.
- **Config editors use JSON round-tripping:** Each editor decodes `configJson` → typed config data class, renders form controls, and re-encodes on every change. Uses `remember(configJson)` so re-decode only happens when the JSON actually changes.
- **Shared components in `:core:ui`:** `ConfigCard`, `SliderWithLabel`, `DayOfWeekSelector`, `TimePickerDialog`, `AppPickerSheet`. These are reusable for future milestones.
- **AppPickerSheet** queries launchable apps via `PackageManager.queryIntentActivities()` with `CATEGORY_LAUNCHER`. Includes search filtering and shows app icon + name + package.
- **Swipe-to-delete** on macro list uses M3 `SwipeToDismissBox` (end-to-start only).
- **Constraints section** is a visible stub in the editor — users can see it exists but can't add constraints yet (M4).

## Milestone 4 Notes
*Notes taken after finishing Milestone 4 (Constraints + Variables + Magic Text).*

- **Constraint engine follows registry pattern:** `ConstraintChecker` interface + `ConstraintRegistry` + `ConstraintEvaluator`, same as triggers/actions. Adding a new constraint = implement `ConstraintChecker` + add `@Binds @IntoSet` in `EngineModule`.
- **ConstraintEvaluator uses flat sequential logic:** Constraints are sorted by `sortOrder`. First constraint seeds the result, subsequent ones combine using their `logicOperator` (AND/OR/XOR/NOT). NOT inverts the individual result then ANDs with the running result. Empty constraint list returns `true`.
- **MacroDispatcher now evaluates constraints** before executing actions. If constraints aren't met, the macro is logged with `CONSTRAINT_NOT_MET` status and actions are skipped.
- **VariableStore** is a singleton with an in-memory `ConcurrentHashMap` backed by Room via `VariableRepository`. Initialized when `MacroDispatcher.start()` is called. Global variables persist; local variables (`lv_` prefix) live only in `ExecutionContext`.
- **Database migration 1→2** adds the `variables` table with a unique index on `name`.
- **Magic Text** resolves `{token}` patterns in action `configJson` before handlers see it. Built-in tokens: `battery_level`, `time`, `date`, `device_name`, `macro_name`, `trigger_type`. Variable tokens: `{v_name}` (global), `{lv_name}` (local). Trigger data: `{trigger_key}`. Unknown tokens left as-is.
- **Magic Text resolves at the JSON level** in `ActionExecutor` — the entire `configJson` string is passed through the resolver before the handler decodes it. This means variable values containing JSON special characters could theoretically break parsing, but for typical string values this works fine.
- **New `:feature:variables` module** with `VariableManagerScreen` — accessible from the bottom nav as a 4th tab (Macros | Logs | Variables | Settings). CRUD for global variables with name, type, and value.
- **3 new action handlers:** `SetVariableHandler`, `DeleteVariableHandler`, `ClearVariablesHandler`. Set/Delete use `lv_` prefix convention to distinguish local vs global.
- **Constraint editor UI** replaces the M3 stub. Each constraint card shows a logic operator dropdown (AND/OR/XOR/NOT) between constraints. All 8 M4 constraint types have config editors.
- **WiFi constraint uses deprecated APIs** (`WifiManager.connectionInfo`) for SSID matching. Android 10+ restricts SSID access — will need ACCESS_FINE_LOCATION + location enabled for reliable SSID matching. Good enough for now.

## Milestone 5 Notes
*Notes taken after finishing Milestone 5 (Connectivity & Communication Expansion).*

- **10 new triggers:** WifiStateChange, WifiSsidTransition, BluetoothEvent, DataConnectivityChange, AirplaneModeChanged, SmsReceived, CallIncoming, CallEnded, CallMissed, RegularInterval. All follow the established BroadcastReceiver or NetworkCallback patterns.
- **10 new actions:** WifiConfigure, BluetoothConfigure, AirplaneMode, SendSms, MakeCall, LaunchHomeScreen, OpenWebsite, HttpRequest, SpeakText, FillClipboard.
- **4 new constraints:** BluetoothConnected, WifiEnabled, AirplaneMode, CallState.
- **CallStateTracker** is a shared singleton that manages the telephony listener. The three call monitors (Incoming, Ended, Missed) register callbacks on it to avoid duplicate `PhoneStateListener`/`TelephonyCallback` registrations. Uses API 31+ `TelephonyCallback` with fallback to deprecated `PhoneStateListener`.
- **WiFi/Bluetooth toggle actions are restricted on modern Android.** `WifiConfigureHandler` opens a settings panel on Android 10+. `BluetoothConfigureHandler` falls back to Bluetooth settings on newer versions. `AirplaneModeHandler` requires `WRITE_SECURE_SETTINGS` (ADB-grantable) or opens settings.
- **HttpRequestHandler** uses `java.net.HttpURLConnection` (no OkHttp dependency). Runs on `Dispatchers.IO`. Stores response in `localVariables["http_response_body"]` and optionally in a named variable.
- **SpeakTextHandler** creates a `TextToSpeech` instance per invocation, waits for `onInit`, speaks, then shuts down. Uses `suspendCancellableCoroutine` to bridge the async TTS callback.
- **FillClipboardHandler** posts to main thread via `Handler(Looper.getMainLooper())` since `ClipboardManager` requires main thread access.
- **New magic text tokens:** `{ssid}`, `{call_number}`, `{sms_message}`, `{sms_sender}`, `{http_response_body}`, `{http_response_code}`. The first reads current WiFi SSID directly; the others resolve from trigger data or local variables.
- **RegularIntervalMonitor** uses coroutine-based `delay` loop rather than `AlarmManager`, which is simpler but means intervals may drift slightly under Doze.
- **WifiSsidTransitionMonitor** uses `ConnectivityManager.NetworkCallback` rather than polling. SSID access on Android 10+ requires `ACCESS_FINE_LOCATION` permission + location services enabled.
- **New permissions added:** ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, BLUETOOTH (pre-31), BLUETOOTH_ADMIN (pre-31), BLUETOOTH_CONNECT, BLUETOOTH_SCAN, RECEIVE_SMS, SEND_SMS, READ_PHONE_STATE, CALL_PHONE, INTERNET.
- **No database migration needed** — all new types use the existing `configJson` pattern.
- **Regex fix in MagicTextResolver:** The token pattern `\{([^}]+)}` crashed on Android's ICU regex engine due to an unescaped closing `}`. Fixed to `\{([^}]+)\}`. This was a pre-existing M4 bug surfaced when the service tried to create the singleton.
- **BroadcastReceiver/NetworkCallback monitors don't unregister in `stop()`** — this is a known leak shared by both M2 and M5 monitors. `stop()` nulls the reference but doesn't call `unregisterReceiver()`/`unregisterNetworkCallback()`. Works for now because the service context destruction handles cleanup, but will need fixing if we ever need clean stop/restart cycles without service restart.
