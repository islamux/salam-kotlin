# Architecture Overview & MVVM Guide

## The Big Picture: Data Flow

Data flows strictly in one direction (Unidirectional Data Flow):

```
JSON (assets/) ──► JsonKhatiraRepository ──► ViewModels ──► Compose UI
                                                   ▲
                                                   │
                                             User actions
```

1. **Data Layer:** Reads `khatira_content.json` from `assets/`, caches it, provides `Chapter` and `Page` objects.
2. **ViewModel Layer:** Holds UI state as `StateFlow`, handles business logic (font size bounds, page navigation, search filtering).
3. **UI Layer:** Only draws screens based on state and forwards user actions to ViewModels.

## MVVM Layers Breakdown

```
┌──────────────────────────────────────────────────────┐
│  VIEW (Composable functions)                         │
│  - Displays data from ViewModel                      │
│  - Sends user actions to ViewModel                   │
│  - NEVER contains business logic                     │
└─────────────────────┬────────────────────────────────┘
                      │ collects state (collectAsState)
                      │ sends events (onClick → ViewModel)
                      ▼
┌──────────────────────────────────────────────────────┐
│  VIEWMODEL                                           │
│  - Holds UI state as StateFlow                       │
│  - Survives configuration changes (rotation)         │
│  - Private mutable / public read-only split          │
└─────────────────────┬────────────────────────────────┘
                      │ reads data
                      ▼
┌──────────────────────────────────────────────────────┐
│  MODEL (Data Layer)                                  │
│  - JsonKhatiraRepository (interface + impl)          │
│  - KhatiraContent, Chapter, Page (data classes)      │
│  - AppStrings (Arabic UI strings)                    │
└──────────────────────────────────────────────────────┘
```

## Package Map

```
com.islamux.khatir/
├── di/                  ← AppModule (dependency injection)
├── navigation/          ← Routes + NavGraph
├── ui/
│   ├── home/            ← HomeScreen + HomeViewModel
│   ├── reader/          ← ReaderScreen + ReaderViewModel
│   ├── search/          ← SearchScreen + SearchViewModel
│   └── theme/           ← Color (AppColors), Type (ContentStyles), Theme
├── data/
│   ├── model/           ← KhatiraContent, Chapter, Page
│   ├── repository/      ← KhatiraRepository, JsonKhatiraRepository
│   └── static/          ← AppStrings
└── util/                ← removeSearchDiacritics, whatsapp_util, alert_exit_dialog
```

## Key Differences from Athkarix

| Aspect | Athkarix (11 categories) | Salam (34 chapters) |
|--------|--------------------------|---------------------|
| **Base ViewModels** | `BaseAthkarViewModel` (shared logic) | None — 3 heterogeneous screens |
| **SharedFlow** | Navigation events, haptics, completion | Not used yet |
| **Data source** | 10 text constant files + 1 JSON | Single `khatira_content.json` |
| **Screen count** | 11+ screens | 3 screens (Home, Reader, Search) |
| **Counter/advance** | Tap-to-advance with repeat counts | Simple page navigation via pager |

## Architectural Rules

1. **Single Activity** — All screens live in one Activity, swapped via Navigation Compose.
2. **No Network** — All data is local from a JSON asset.
3. **Arabic-First** — RTL layout (`reverseLayout = true` in HorizontalPager), Arabic text throughout.
4. **Never put logic in a `@Composable`** — Composables map state to UI and pass events up.
5. **Never import `android.*` or `compose.*` in a ViewModel** — ViewModels are pure Kotlin logic.
6. **Use `Intent.ACTION_SEND` for sharing** — built share intent with text content. See `WhatsAppUtil.kt`.
