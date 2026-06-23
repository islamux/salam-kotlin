# Project Audit — Salam Kotlin

Full codebase analysis covering pros, bugs, performance, over-engineering, and architecture.

---

## Pros

- **Clean MVVM architecture** — clear separation, ViewModels own state via StateFlow
- **Manual DI keeps it simple** — `AppModule` object, no Hilt/Dagger complexity
- **Arabic-first RTL** — `reverseLayout = true`, `AutoMirrored` icons, `CompositionLocalProvider` for RTL
- **Single Activity** — modern best practice
- **Reactive state** — all state through `StateFlow` → `collectAsState()`, UI recomposes automatically
- **Compile-safe navigation** — `Routes` object prevents route string typos
- **No network dependencies** — everything is local
- **Compose-only UI** — no XML layout files
- **Single JSON data source** — simpler than multiple text constant files
- **Diacritic-insensitive search** — proper Arabic tashkeel removal for matching
- **Flutter-parity** — all 20 features match the Flutter version exactly

---

## Bugs

| # | Bug | File:Line | Impact |
|---|-----|-----------|--------|
| 1 | **No empty state for empty search results** — search shows "لا توجد نتائج للبحث" but the condition might not handle all edge cases | `SearchScreen.kt` | Minor — cosmetic |
| 2 | **ReaderViewModel.navigateToPage doesn't clamp to bounds** — if index < 0 or >= pages.size, it silently does nothing | `ReaderViewModel.kt:44-48` | Minor — UI prevents out-of-bounds via pager state |

---

## Performance

| # | Issue | File | Impact |
|---|-------|------|--------|
| 1 | **Search recomputes diacritic removal on every keystroke** — all 532 pages are scanned and normalized on every `onValueChange`. No memoization. | `SearchViewModel.kt:61-99` | Noticeable on low-end devices with rapid typing |
| 2 | **JSON parsed eagerly in memory** — 825KB `khatira_content.json` is fully loaded and cached on first access | `JsonKhatiraRepository.kt:15-23` | Negligible (< 5MB heap) |
| 3 | **Background images at full resolution** — `bg_home.jpg` and `bg_reader.jpg` loaded without downsampling | HomeScreen, ReaderScreen | Minor memory impact |

---

## Over-engineering / Overkill

| # | Issue | Why |
|---|-------|-----|
| 1 | **KhatiraRepository interface** — only one implementation (`JsonKhatiraRepository`). The interface adds a layer with no current benefit. | Could be inlined if no second implementation is planned |
| 2 | **HomeViewModel.Factory inner class** — same pattern repeated in all 3 ViewModels. The factory is a 5-line boilerplate in each file. | Could be centralized in `AppModule` with `viewModel { }` |
| 3 | **AppStrings.chapterTitle(id) `when` expression** — 34 hardcoded strings that duplicate the Flutter home.dart. If Flutter titles change, both must be updated. | Source-of-truth concern — but no better option without a shared data format |

---

## Architecture Issues

| # | Issue | Why it matters |
|---|-------|----------------|
| 1 | **Zero unit tests** — no `src/test/` directory exists | Any regression goes undetected |
| 2 | **No process death handling** — ViewModels are created fresh on process restart (not using `SavedStateHandle`) | User loses reading position if Android kills the app |
| 3 | **No loading state in ReaderScreen** — `ReaderUiState` has `isLoading: Boolean` but the screen doesn't show a progress indicator | Brief blank screen while JSON loads |
| 4 | **ViewModels survive via `viewModel()` Compose function but not `SavedStateHandle`** — rotation is fine, process death is not | Minor — most users don't experience process death mid-reading |

---

## Code Quality Issues

| # | Issue | File:Line | Severity |
|---|-------|-----------|----------|
| 1 | **`getShareText()` returns empty string with no user feedback** — if current page is empty, user shares nothing silently | `ReaderViewModel.kt:65` | Low |
| 2 | **`WhatsAppUtil` swallows all exceptions silently** — if both WhatsApp and Play Store fail, user gets no feedback | `WhatsAppUtil.kt` | Medium |
| 3 | **Some `key = chapterId` lines lack documentation** — the reason for the key param is non-obvious to juniors | `ReaderScreen.kt:66` | Low |
| 4 | **Hardcoded magic numbers** — font size range (21f–37f, step 2f) is not extracted to constants | `ReaderViewModel.kt:53,59` | Low |

---

## Summary

**Ready for production?** Yes, for the current feature set.

**Top 3 improvements:**
1. Add unit tests (see `09-unit-testing-guide.md`)
2. Add memoization to search diacritic removal
3. Add loading indicator to ReaderScreen

**Dead code to check:** None currently identified (all files are used).

**Feature parity with Flutter:** 100% (all 20 features match).
