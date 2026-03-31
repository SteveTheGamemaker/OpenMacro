# Contributing to OpenMacro

Thanks for your interest in contributing to OpenMacro! This guide covers the architecture, conventions, and patterns used throughout the codebase. Please read it before submitting a PR.

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17 (bundled with Android Studio)
- An Android device or emulator running API 28+ (Android 9+)

### Building
```bash
JAVA_HOME="/path/to/android-studio/jbr" ./gradlew assembleDebug
```

### Project Structure
OpenMacro is a multi-module Gradle project:

```
:app                    — Activity, navigation graph, DI wiring
:core:common            — Shared utilities
:core:model             — Pure Kotlin domain models (no Android deps)
:core:database          — Room database, DAOs, entities, repositories
:core:engine            — Macro execution engine (triggers, actions, constraints)
:core:ui                — Shared Compose components and theme
:feature:macros         — Macro list + editor screens
:feature:logs           — Execution log viewer
:feature:variables      — Variable manager screen
:feature:actionblocks   — Action block list + editor screens
:feature:settings       — App settings
```

Dependencies flow downward: `:app` → `:feature:*` → `:core:*`. Feature modules should not depend on each other unless necessary (`:feature:actionblocks` depends on `:feature:macros` to reuse editor components — this is the only exception).

## Architecture

### Registry Pattern
The core architecture is built on registries with Hilt multibindings. Adding a new trigger, action, or constraint is purely additive:

1. Implement the interface (`TriggerMonitor`, `ActionHandler`, or `ConstraintChecker`)
2. Add a `@Binds @IntoSet` binding in `EngineModule`
3. Add the type to the corresponding enum (`TriggerType`, `ActionType`, `ConstraintType`)
4. Add a `@Serializable` config data class
5. Add a config editor composable
6. Add it to the `AVAILABLE_*` list in the editor screen

No existing code needs to be modified beyond these registration points.

### Config JSON
All type-specific configuration is stored as a JSON string in `configJson` columns. This means adding new types never requires a database migration. Each type has a `@Serializable` data class with sensible defaults:

```kotlin
@Serializable
data class MyNewConfig(
    val someField: String = "",
    val threshold: Int = 50,
    val enabled: Boolean = true,
)
```

### Event Flow
```
TriggerMonitor → TriggerEvent → MacroDispatcher → ConstraintEvaluator → ActionExecutor
```

### Action Tree
Actions form a tree via `parentActionId` for flow control (if/else, loops). The executor walks the tree recursively. `FlowResult` sealed class handles break/continue/cancel without exceptions.

## Code Conventions

### Naming

| Thing | Convention | Example |
|-------|-----------|---------|
| Action handlers | `{Action}Handler` | `SendSmsHandler` |
| Trigger monitors | `{Trigger}Monitor` | `WifiStateChangeMonitor` |
| Constraint checkers | `{Constraint}Checker` | `BatteryLevelChecker` |
| Config data classes | `{Type}Config` | `SendSmsConfig` |
| Config editors | `{Type}ConfigEditor` | `SendSmsConfigEditor` |
| Screen composables | `{Feature}Screen` | `MacrosScreen` |
| Type IDs | `snake_case` string | `"send_sms"` |
| Magic text tokens | `snake_case` | `battery_level`, `sms_message` |
| Log tags | `TAG` in companion object | `private const val TAG = "SendSmsHandler"` |
| Constants | `SCREAMING_SNAKE_CASE` | `AVAILABLE_TRIGGERS` |

### File Organization

- **One config file per domain:** `ActionConfigs.kt` has all action configs, `TriggerConfigs.kt` has all trigger configs, etc.
- **One handler per file:** Each `ActionHandler`, `TriggerMonitor`, or `ConstraintChecker` gets its own file.
- **Config editors grouped by milestone/domain:** e.g., `ConnectivityActionEditors.kt`, `FlowControlConfigEditors.kt`.

### Kotlin Style

- **Indentation:** 4 spaces
- **Trailing commas:** Always use them on multi-line parameter lists and collection literals
- **Formatting:** Follow standard Kotlin conventions (Android Studio defaults)
- **Java target:** 17
- **No wildcard imports**

### Compose Conventions

**Parameter ordering:**
1. Navigation/business callbacks first
2. ViewModel (with default `hiltViewModel()`)
3. `Modifier` last (with default `Modifier`)

```kotlin
@Composable
fun MyScreen(
    onNavigateBack: () -> Unit,
    onItemSelected: (Long) -> Unit,
    viewModel: MyViewModel = hiltViewModel(),
) {
```

**State collection:** Always use `collectAsStateWithLifecycle()`, not `collectAsState()`.

**Config editors** follow this pattern:
```kotlin
@Composable
fun MyConfigEditor(
    configJson: String,
    onConfigChanged: (String) -> Unit,
) {
    val config = remember(configJson) {
        runCatching { json.decodeFromString<MyConfig>(configJson) }
            .getOrDefault(MyConfig())
    }
    // UI that calls onConfigChanged(json.encodeToString(...)) on changes
}
```

**MagicTextField:** Use `MagicTextField` instead of `OutlinedTextField` for any text field in action config editors where users might want to insert magic text tokens.

### Hilt / Dependency Injection

All injectable classes use `@Inject constructor()` with zero arguments (dependencies come from Hilt modules). Bindings in `EngineModule` use `@Binds @IntoSet` on a single line:

```kotlin
@Binds @IntoSet
abstract fun bindMyHandler(impl: MyHandler): ActionHandler
```

Group bindings by category with comment headers:
```kotlin
// ── Trigger Monitors ──
// ── Action Handlers ──
// ── Constraint Checkers ──
```

### Permissions

If your trigger/action/constraint requires a dangerous runtime permission, add it to `PermissionHelper`:
- `triggerPermissions()` for triggers
- `actionPermissions()` for actions
- `constraintPermissions()` for constraints

Also declare the permission in `core/engine/src/main/AndroidManifest.xml`.

### Database

- Use the existing `configJson` pattern — avoid adding new columns or tables unless absolutely necessary
- If a migration is needed, increment the version in `OpenMacroDatabase` and add a `Migration` object
- `MacroRepository.saveMacroWithDetails()` uses delete-and-reinsert for child entities — child IDs are not stable across saves

## Adding a New Trigger

Here's the full checklist for adding a new trigger type:

1. **`core/model/.../TriggerType.kt`** — Add enum entry: `MY_TRIGGER("my_trigger", "My Trigger")`
2. **`core/model/.../config/TriggerConfigs.kt`** — Add `@Serializable data class MyTriggerConfig(...)`
3. **`core/engine/.../trigger/MyTriggerMonitor.kt`** — Implement `TriggerMonitor` interface
4. **`core/engine/.../di/EngineModule.kt`** — Add `@Binds @IntoSet` binding
5. **`core/engine/.../PermissionHelper.kt`** — Add required dangerous permissions (if any)
6. **`core/engine/src/main/AndroidManifest.xml`** — Declare permissions (if any)
7. **`feature/macros/.../trigger/*.kt`** — Add `MyTriggerConfigEditor` composable
8. **`feature/macros/.../MacroEditorScreen.kt`** — Add to `AVAILABLE_TRIGGERS` list, add import, add case to `TriggerConfigContent`
9. **`core/ui/.../MagicTextPickerSheet.kt`** — Add trigger-specific tokens to `triggerTokensFor()` (if the trigger produces data)
10. **`core/engine/.../text/MagicTextResolver.kt`** — Add token resolution for any new magic text tokens

## Adding a New Action

Same pattern — replace "trigger" with "action" in the checklist above. Implement `ActionHandler` (or `FlowControlHandler` for flow control actions). Add to `AVAILABLE_ACTIONS` instead of `AVAILABLE_TRIGGERS`, and to `ActionConfigContent` instead of `TriggerConfigContent`.

## Adding a New Constraint

Implement `ConstraintChecker`. Add to `AVAILABLE_CONSTRAINTS` and `ConstraintConfigContent`. Add permissions to `constraintPermissions()` if needed.

## Version Catalog

All dependencies are managed in the version catalog (`gradle/libs.versions.toml`). Never hardcode dependency versions in `build.gradle.kts` files. If you need a new dependency, add it to the catalog first.

## Pull Requests

- Keep PRs focused — one feature or fix per PR
- Test on a real device when possible (emulators don't support all triggers like SMS, Bluetooth, etc.)
- Make sure `./gradlew assembleDebug` passes before submitting
- Describe what you changed and why in the PR description
- If adding a new trigger/action/constraint, include the full checklist above in your PR description so reviewers can verify nothing was missed

## Questions?

Open an issue on GitHub if anything in this guide is unclear or if you need help getting started.
