# Getting Started — Salam for Kotlin Juniors

## What Is This Project?

**Salam** (سلام) is an Android app for reading Islamic khatira (sermons/lessons). It's a native Kotlin port of an existing Flutter app with the same name. The app features:

- 34 chapters of khatira content (pre + 1–32 + final)
- A reader with RTL swipe for Arabic text
- Full-text search across all chapters
- Font size controls (21f–37f)
- Share content with others
- WhatsApp integration (contact us)
- Golden amber theme on a light background

## What You Need to Know

### Kotlin Basics
If you're new to Kotlin, see [`01-kotlin-concepts.md`](./01-kotlin-concepts.md) for every language feature used in the codebase.

### Android Basics
- **Activity**: A screen in Android. This app has ONE activity (`MainActivity`).
- **Compose**: Modern Android UI toolkit where you describe your UI with Kotlin functions instead of XML layouts.
- **ViewModel**: Holds UI state and survives screen rotations.
- **Gradle**: The build system.

## Opening the Project

1. Install [Android Studio](https://developer.android.com/studio) (latest version)
2. Open Android Studio → "Open an existing project" → select the `salamkotlin` folder
3. Wait for Gradle to sync (it downloads dependencies automatically)

## Building and Running

```bash
./gradlew assembleDebug       # build APK
./gradlew installDebug        # build + install on connected device
./gradlew testDebugUnitTest   # run unit tests
./gradlew clean assembleDebug # clean build
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Project Directory Structure

```
salamkotlin/
├── app/
│   ├── src/main/
│   │   ├── java/com/islamux/khatir/   ← ALL Kotlin source code
│   │   │   ├── KhatirApp.kt           ← Application class (bare)
│   │   │   ├── MainActivity.kt        ← Entry point (launches the UI)
│   │   │   ├── di/                    ← Dependency injection (AppModule)
│   │   │   ├── navigation/            ← Screen routing
│   │   │   ├── ui/                    ← User interface (Compose screens)
│   │   │   │   ├── home/              ← Home screen + ViewModel
│   │   │   │   ├── reader/            ← Reader screen + ViewModel
│   │   │   │   ├── search/            ← Search screen + ViewModel
│   │   │   │   └── theme/             ← Colors, typography, theme
│   │   │   ├── data/                  ← Data layer
│   │   │   │   ├── model/             ← KhatiraContent, Chapter, Page
│   │   │   │   ├── repository/        ← JsonKhatiraRepository
│   │   │   │   └── static/            ← AppStrings
│   │   │   └── util/                  ← Utilities (diacritics, whatsapp, alerts)
│   │   ├── assets/                    ← khatira_content.json (533 pages, ~855KB)
│   │   └── res/                       ← Resources (images, fonts, theme)
│   └── build.gradle.kts               ← App-level build config
├── build.gradle.kts                   ← Root build config
├── settings.gradle.kts                ← Gradle settings
└── gradlew                            ← Gradle wrapper script
```

## First Things to Read

If you want to understand how the app works from end to end, read these files in order:

1. `MainActivity.kt` — Where the app starts
2. `ui/theme/Color.kt` and `Theme.kt` — The color scheme and typography
3. `navigation/NavGraph.kt` — How all screens connect
4. `di/AppModule.kt` — How dependencies are wired
5. `data/repository/JsonKhatiraRepository.kt` — Where all content lives
6. `ui/home/HomeViewModel.kt` — Home screen state
7. `ui/reader/ReaderViewModel.kt` — Reader logic (font size, page nav, share)
8. `ui/home/HomeScreen.kt` — The main menu
9. `ui/reader/ReaderScreen.kt` — The reader with HorizontalPager

## Key Gradle Config

- **compileSdk = 37**, **targetSdk = 36**, **minSdk = 24**
- **Kotlin 2.2.10** + Jetpack Compose BOM 2026.02.01
- **Package**: `com.islamux.khatir`
- **App name**: `Salam`
- **kotlinx.serialization** 1.8.1 for JSON parsing
- No network libraries, no Hilt/Dagger — manual dependency injection

## Next Docs

- [`01-kotlin-concepts.md`](./01-kotlin-concepts.md) — Every Kotlin feature used here
- [`02-flutter-to-compose.md`](./02-flutter-to-compose.md) — Mapping Flutter concepts to Jetpack Compose
- [`03-architecture-overview.md`](./03-architecture-overview.md) — MVVM and data flow
- [`04-viewmodel-deep-dive.md`](./04-viewmodel-deep-dive.md) — How ViewModels manage state
- [`05-ui-layer.md`](./05-ui-layer.md) — Compose screens and components
- [`06-data-layer.md`](./06-data-layer.md) — JSON data, repository, models
- [`07-navigation-and-di.md`](./07-navigation-and-di.md) — Screen routing and manual DI
- [`08-project-audit.md`](./08-project-audit.md) — Current status and known issues
- [`09-unit-testing-guide.md`](./09-unit-testing-guide.md) — How to write local JVM unit tests
