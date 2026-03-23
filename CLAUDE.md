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
- **Test macro flow:** The macros screen has a temporary FAB that creates a "Screen On → Notify" test macro. This will be replaced by the full editor in Milestone 3.
- **No constraint evaluation yet** — that's Milestone 4. All enabled macros fire when their trigger matches.
