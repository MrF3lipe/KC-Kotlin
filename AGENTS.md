# AGENTS.md — Kitchen Cabinet (Kotlin)

## Project

Native Android app for recipe management, pantry tracking, and shopping lists.
Single module (`:app`), Kotlin + Jetpack Compose + Room (SQLite).

## Build & Run

```bash
# Debug build (Windows)
gradlew.bat assembleDebug

# Release build (needs KEYSTORE_PATH env var)
gradlew.bat assembleRelease

# Output
app/build/outputs/apk/debug/app-debug.apk
```

No `local.properties` in repo — the build script auto-detects `ANDROID_HOME` or falls back to `C:\android-sdk`.
First build downloads Gradle; subsequent builds use cache.

## Stack

- **Kotlin 2.0.21** / **AGP 8.7.3** / **Gradle 8.9** / **Java 17**
- **Compose BOM 2024.12.01** — all Compose deps come from the BOM, never pin individual versions
- **Room 2.7.1** with **KSP** (not kapt) for annotation processing
- **compileSdk 35**, **minSdk 26**, **targetSdk 35**
- Version catalog: `gradle/libs.versions.toml`

## Architecture

```
app/src/main/java/com/kitchencabinet/
├── MainActivity.kt          ← Entry point, NavHost, theme/dark mode wiring
├── data/                    ← Room: entities, DAOs, AppDatabase, Repository, SeedData
├── viewmodel/               ← ViewModels (Recipe, Pantry, Shopping, Settings, MealPlan)
├── i18n/                    ← Translations.kt (ES/EN string maps)
├── notification/            ← WorkManager expiry check + NotificationHelper
└── ui/
    ├── theme/               ← Color, Type, Theme (Material3)
    ├── components/          ← RecipeCard, BottomNavBar, AppShell
    ├── i18n/                ← Strings.kt (Compose CompositionLocal)
    └── screens/             ← 12 screen composables
```

## Key Gotchas

- **No tests exist** — no unit tests, no instrumented tests. If adding tests, use `app/src/test/` and `app/src/androidTest/`.
- **No linting or typecheck commands** configured beyond what Gradle does by default.
- **Room uses `fallbackToDestructiveMigration()`** — schema changes wipe data on upgrade. Database version is in `AppDatabase.kt` (`version = 2`).
- **Seed data** loads on first DB creation via `RoomDatabase.Callback.onCreate` → `SeedData.populate()`.
- **i18n is manual** — two layers: `i18n/Translations.kt` (string maps) and `ui/i18n/Strings.kt` (CompositionLocal). Locale is stored in Room `SettingsEntity`, toggled in Settings screen. No Android resource-based i18n.
- **Keyboard input** — `windowSoftInputMode="adjustNothing"` in manifest to prevent keyboard bounce issues on Pantry screen.
- **Deep links** — `kc-kotlin://recipe/{id}` and `https://kitchencabinet.app/recipe/{id}`.
- **Signing** — release signing reads `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from env vars. No signing config without them.
- **Images** — camera/gallery photos are copied to `filesDir/images/` for persistence. FileProvider authority is `${applicationId}.fileprovider`.
- **README mentions `.github/workflows/android.yml`** but no CI workflow exists in the repo currently.
