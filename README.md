# Salamm — خواطر إيمانية

An Islamic spiritual content reader for Android. Browse and search a collection of Arabic spiritual reflections (خواطر) organized into chapters and pages, with adjustable font sizes and sharing.

## Features

- **Chapter-based reading** — 34 chapters of spiritual content, presented page by page
- **Ordered rendering** — Each page renders titles, subtitles, body text, and Quranic verses in a defined sequence
- **Diacritic-insensitive search** — Find content by keyword, ignoring Arabic tashkeel
- **Font size controls** — Increase or decrease text size for comfortable reading
- **Share** — Share any page's content via other apps
- **RTL support** — Full right-to-left layout for Arabic text

## Screenshots

<!-- TODO: Add screenshots -->

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM (Model-View-ViewModel) |
| State management | `StateFlow` / `MutableStateFlow` |
| Navigation | Jetpack Navigation Compose |
| Serialization | kotlinx.serialization |
| Dependency Injection | Manual (`AppModule`) |
| Testing | JUnit 4, MockK, kotlinx-coroutines-test |

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17+
- Android API 24+

### Build & Run

```bash
# Clone
git clone https://github.com/islamux/salam-kotlin.git
cd salam-kotlin

# Build
./gradlew assembleDebug

# Install on emulator or device
./gradlew installDebug

# Run tests
./gradlew testDebugUnitTest
```

## Project Structure

```
app/src/main/java/com/islamux/khatir/
├── data/
│   ├── model/              — Data classes: Page, Chapter, KhatiraContent
│   ├── repository/         — KhatiraRepository interface + JsonKhatiraRepository impl
│   └── static/             — Arabic UI strings (AppStrings)
├── di/                     — Manual dependency injection (AppModule)
├── navigation/             — Routes and NavGraph
├── ui/
│   ├── home/               — Home screen with chapter list
│   ├── reader/             — Page-by-page reader with font controls
│   ├── search/             — Full-text search across all content
│   └── theme/              — Colors, typography, fonts
└── util/                   — Diacritics utility, share helper, exit dialog
```

## Data

Content is stored in `assets/khatira_content.json` (~850KB) and parsed at runtime. The JSON contains 34 chapters with over 500 pages of Arabic text, including Quranic verses (ayahs). Each page has a `order` field that defines the rendering sequence of its elements.

## Content Updates

To update the app's content, replace `app/src/main/assets/khatira_content.json` with a new file matching the same schema. No code changes are needed.

## Learning Resources

If you're new to Kotlin or Android development, see the [junior learning guides](docs/juniors/) for a step-by-step introduction to the codebase, testing, and architecture.

## License

<!-- TODO: Add license information -->
