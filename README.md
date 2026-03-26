# NutritionTracker (Sample Android App)

A multi-module Android application demonstrating a clean architecture approach for a simple nutrition tracking experience built with Kotlin and Jetpack Compose. The app displays a diary with consumed food, suggested items, calories overview, and food browsing/details.

Last updated: 2026-03-26

## Key Features
- Modern, Compose-first UI (Material 3)
- Food list and details screens
- Daily diary with calories card and eaten/suggested food
- Date selector with quick navigation between days
- Layered architecture with separate domain, data, and feature modules
- Room-based local database with pre-population for demo content
- Gradle Version Catalog and multi-module setup for scalability

## Tech Stack
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Persistence: Room (DAO/Entity/Database)
- Concurrency: Kotlin Coroutines
- Modularization: Clean-architecture style layering (domain, data, feature, core)
- DI/Composition Root: `AppModule` (application-level wiring)

> Note: The project intentionally focuses on structure and patterns; some integrations are simplified for clarity.

## Project Structure
Root: `nutritiontracker-sample-android`

High-level modules:
- `app/` — Application entry point and navigation host
- `domain/` — Business models and use cases (pure Kotlin)
- `data/` — Data layer implementations
  - `data/diary/`
  - `data/food/`
- `core/` — Shared cross-cutting modules
  - `core/common/` — Common utilities (e.g., application scope)
  - `core/database/` — Room database and DAOs
  - `core/ui/` — Design system, theme, and shared UI components
- `feature/` — Feature-specific presentation layers (Compose screens and ViewModels)
  - `feature/diary/`
  - `feature/food/`


## Architecture Overview
The project follows a modular, clean-ish architecture:

- Domain (business rules)
  - Entities: `Diary`, `DiaryDate`, `Food`, `FoodId`, `GoalCalories`
  - Use case: `GetDiaryStreamForDateUseCase`
  - Interfaces: `DiaryRepository`, `FoodRepository`
- Data (implementation details)
  - Implements domain repositories using Room via `core/database`
  - Mappers between database entities and domain models
- Core (shared)
  - `core/database`: Room database (`NutritionAppDatabase`), DAOs (`DiaryDao`, `FoodDao`), entities, and seeding
  - `core/ui`: Theming and reusable Compose components (e.g., `AppCard`, `PillChip`)
  - `core/common`: app-scope utilities
- Features (presentation)
  - Compose screens and `ViewModel`s per feature (`DiaryScreen`, `FoodListScreen`, `FoodDetailScreen`)
  - UI-specific mappers from domain to UI models/components
- App module
  - Assembles everything and wires dependencies in `AppModule`

Data flow (typical): UI (feature) → Use case (domain) → Repository (domain) → Data repo implementation (data) → DAO (core/database) → Room.

## Getting Started
- Requirements: Android Studio (Giraffe+), JDK 17, Android SDK
- Steps:
  1. Open the project in Android Studio and let Gradle sync.
  2. Select a device/emulator (API 26+ recommended).
  3. Run the `app` configuration.

Optional CLI:
```bash
./gradlew :app:assembleDebug
./gradlew test
```

## Module-by-Module Summary
- `app` — Hosts the app, sets up DI/wiring in `AppModule`, contains the main activity and app theme integration
- `domain` — Pure Kotlin domain model and use cases (no Android dependencies)
- `data/diary` — Diary repository implementation, mapping, and DI wiring for diary data
- `data/food` — Food repository implementation and wiring
- `core/common` — Common utilities (e.g., `ApplicationScope`)
- `core/database` — Room setup: database (`NutritionAppDatabase`), DAOs (`DiaryDao`, `FoodDao`), entities, prepopulation
- `core/ui` — Material 3 theme, colors, typography, and reusable UI components
- `feature/diary` — Diary experience (screens, components like `CaloriesCard`, `DateHeader`, `DaysList`, `EatenFood`, ViewModel)
- `feature/food` — Food list and details (screens and ViewModel)


## Testing
Each module contains its own `test/` and/or `androidTest/` sources. You can run all unit tests with:
```bash
./gradlew test
```
Run connected Android tests (if any) with a device attached:
```bash
./gradlew connectedAndroidTest
```



## License
No license file currently present. By default, all rights reserved by the author. If you plan to use this code in your project, please open an issue to clarify licensing.

---
If something looks off or you need a different README format (e.g., concise vs. detailed, with screenshots, or a Russian version), please open an issue or start a discussion.
