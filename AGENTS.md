# Salamm — AI Agent Instructions

## Project Overview

Salamm (Khatir) is a Kotlin Android app: an Islamic spiritual content reader built with Jetpack Compose. Content is stored in a JSON asset file and rendered chapter-by-chapter with Arabic text, search, font controls, and sharing.

## Architecture

- **Pattern:** MVVM (Model-View-ViewModel)
- **UI:** Jetpack Compose (Material 3), no XML layouts
- **State:** `StateFlow` / `MutableStateFlow` with backing-property exposure
- **DI:** Manual via `AppModule` object (no Hilt / Dagger)
- **Navigation:** Jetpack Navigation Compose
- **Serialization:** kotlinx.serialization

## Key Conventions

| Area | Convention |
|------|-----------|
| State exposure | `private val _uiState = MutableStateFlow(...)` + `val uiState: StateFlow<...> = _uiState` |
| ViewModel factory | Inner `Factory` class implementing `ViewModelProvider.Factory` |
| Content data | `assets/khatira_content.json` → `JsonKhatiraRepository` → `KhatiraRepository` interface |
| Search | Diacritic-insensitive via `removeSearchDiacritics()` utility |
| Colors | Defined in `ui/theme/Color.kt` |
| Fonts | Defined in `ui/theme/Fonts.kt` |
| Strings | Arabic strings in `data/static/AppStrings.kt` |
| Content strings | Chapter titles in `AppStrings.chapterTitle()` |

## Directory Structure

```
app/src/main/java/com/islamux/khatir/
├── data/
│   ├── model/          — Page, Chapter, KhatiraContent (serializable)
│   ├── repository/     — KhatiraRepository (interface), JsonKhatiraRepository (impl), ReaderUiState
│   └── static/         — AppStrings (Arabic UI strings)
├── di/                 — AppModule (manual DI)
├── navigation/         — Routes, NavGraph
├── ui/
│   ├── home/           — HomeScreen, HomeViewModel
│   ├── reader/         — ReaderScreen, ReaderViewModel
│   ├── search/         — SearchScreen, SearchViewModel
│   └── theme/          — Color, Theme, Type, Fonts
└── util/               — remove_search_diacritics, whatsapp_util, alert_exit_dialog
```

## Commands

```bash
# Build
./gradlew assembleDebug

# Run all local unit tests
./gradlew testDebugUnitTest

# Run a specific test class
./gradlew testDebugUnitTest --tests "*ClassName*"

# Install on emulator/device
./gradlew installDebug
```

## Data Model — Critical Reading Order

The content JSON has a strict rendering order:

```kotlin
data class Page(
    val index: Int,
    val titles: List<String>,
    val subtitles: List<String>,
    val texts: List<String>,
    val ayahs: List<String>,
    val footer: String?,
    val order: List<String>  // ← determines rendering sequence
)
```

The `order` field on each `Page` tells the reader **in what sequence** to render `titles`, `subtitles`, `texts`, `ayahs`, and `footer`. Do NOT assume alphabetical or any other ordering.

## Testing Rules

- Pure logic and ViewModel tests go in `src/test/java/` (JVM, no emulator)
- UI / Context-dependent tests go in `src/androidTest/java/` (emulator needed)
- Mock repositories with MockK — never load real JSON in ViewModel tests
- Use `Dispatchers.setMain(StandardTestDispatcher())` in ViewModel tests
- Use `runTest { }` for ViewModels with coroutine `init` blocks

## Principles to Follow

1. **Separation of concerns** — Keep content data (JSON) separate from code. Never hardcode content strings in Kotlin files.
2. **Existing patterns** — Match the code style of neighboring files. If the project uses manual DI, don't introduce Hilt.
3. **Clean Code** — Single responsibility, meaningful names, no dead code.
4. **Arabic-first** — All UI strings are in Arabic. Direction is RTL-aware.
5. **No comments** — Don't add comments to code unless explicitly asked.

## References

- `docs/juniors/` — Learning docs for junior developers (architecture, testing, concepts)
- `docs/todo.md` — Current project backlog
