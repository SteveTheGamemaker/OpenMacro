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
- After completing a milestone, update the progress checklist in `ROADMAP.md` (check the box).
- Commit and push at the end of each milestone.
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
