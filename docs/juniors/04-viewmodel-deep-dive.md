# ViewModel Deep Dive

## What Is a ViewModel?

A `ViewModel` holds UI state and survives configuration changes (screen rotation). Without it, rotating your phone would reset the font size, current page, and search results.

Salam has **3 ViewModels**, one per screen. Unlike Athkarix (which has a base ViewModel for 11 similar screens), Salam's screens are heterogeneous — no shared logic to extract.

---

## The Backing Property Pattern

Every ViewModel uses the same backing-property pattern:

```kotlin
class ExampleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleState())
    val uiState: StateFlow<ExampleState> = _uiState
}
```

| # | Variable | Type | Visibility | Purpose |
|---|----------|------|------------|---------|
| 1 | `_uiState` | `MutableStateFlow<State>` | `private` | ViewModel writes to this |
| 2 | `uiState` | `StateFlow<State>` | `public` | UI reads this (read-only) |
| Type annotation | — | — | — | `StateFlow<>` type prevents `set` on the public side |

**Why?** The UI cannot corrupt state accidentally:
```kotlin
viewModel.increaseFontSize()          // ✅ ViewModel controls the change
// viewModel.uiState.value = ...      // ❌ Compile error — read-only!
```

---

## HomeViewModel

**File**: `ui/home/HomeViewModel.kt` (43 lines)

```kotlin
data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadChapters() }

    private fun loadChapters() {
        viewModelScope.launch {
            val chapters = repository.getAllChapters()
            _uiState.value = HomeUiState(chapters = chapters, isLoading = false)
        }
    }
}
```

**What it does:**
- Loads all 34 chapters from `JsonKhatiraRepository` on init
- Exposes a simple `isLoading` / `chapters` state
- No mutation methods — the screen is static (no user actions change the chapter list)

---

## ReaderViewModel

**File**: `ui/reader/ReaderViewModel.kt` (94 lines)

The most complex ViewModel. It handles:
- Loading a single chapter by ID
- Page navigation (via pager state)
- Font size adjustment (21f–37f, step 2)
- Building share text from the current page

```kotlin
data class ReaderUiState(
    val chapter: Chapter? = null,
    val pages: List<Page> = emptyList(),
    val currentPageIndex: Int = 0,
    val fontSize: Float = 21f,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ReaderViewModel(
    private val repository: KhatiraRepository,
    private val chapterId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState

    init { loadChapter() }
}
```

### Font Size Methods
```kotlin
fun increaseFontSize() {
    val current = _uiState.value.fontSize
    if (current < 37f) {
        _uiState.value = _uiState.value.copy(fontSize = current + 2f)
    }
}

fun decreaseFontSize() {
    val current = _uiState.value.fontSize
    if (current > 21f) {
        _uiState.value = _uiState.value.copy(fontSize = current - 2f)
    }
}
```

Both methods:
1. Read the current value from state
2. Check the bounds (21–37)
3. Emit a **new copy** of the state with the updated value

> The `copy()` function is generated automatically by Kotlin's `data class`. It creates a new instance with only the specified fields changed — the rest are copied from the original.

### Page Navigation
```kotlin
fun navigateToPage(index: Int) {
    val pages = _uiState.value.pages
    if (index in pages.indices) {
        _uiState.value = _uiState.value.copy(currentPageIndex = index)
    }
}
```

### Share Text Builder
```kotlin
fun getShareText(): String {
    val state = _uiState.value
    val page = state.pages.getOrNull(state.currentPageIndex) ?: return ""
    return buildShareText(page)
}

private fun buildShareText(page: Page): String {
    val parts = mutableListOf<String>()
    for (field in page.order) {
        when (field) {
            "titles" -> page.titles.forEach { parts.add(it) }
            "subtitles" -> page.subtitles.forEach { parts.add(it) }
            "texts" -> page.texts.forEach { parts.add(it) }
            "ayahs" -> page.ayahs.forEach { parts.add(it) }
            "footer" -> page.footer?.let { parts.add(it) }
        }
    }
    return parts.joinToString("\n\n")
}
```

Iterates the `page.order` list (which defines the field display order) and concatenates all content.

---

## SearchViewModel

**File**: `ui/search/SearchViewModel.kt` (109 lines)

```kotlin
data class SearchResult(
    val chapter: Chapter,
    val page: Page,
    val pageIndex: Int,
    val matchedField: String,
    val matchedText: String
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val allChapters: List<Chapter> = emptyList()
)
```

### Search Logic
```kotlin
fun search(query: String) {
    _uiState.value = _uiState.value.copy(query = query, isSearching = true)
    viewModelScope.launch {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
            return@launch
        }
        val normalizedQuery = removeSearchDiacritics(query.lowercase())
        val results = mutableListOf<SearchResult>()
        for (chapter in chapters) {
            for ((pageIndex, page) in chapter.pages.withIndex()) {
                for (field in page.order) {
                    // check each field for matches using diacritic-insensitive comparison
                }
            }
        }
        _uiState.value = _uiState.value.copy(results = results, isSearching = false)
    }
}
```

**Search algorithm:**
1. Strip Arabic diacritics (tashkeel) from the query
2. For every chapter → every page → every field, strip diacritics from content
3. Check if the normalized query appears in the normalized content
4. Return all matches as `SearchResult` objects

---

## ViewModel Patterns Summary

| Pattern | Description | Examples |
|---------|-------------|---------|
| **Load-on-init** | Load data in `init` block, show loading state | All 3 ViewModels |
| **Simple state holder** | Expose state with mutation methods | `ReaderViewModel` (font size) |
| **Computed state** | Build derived data on demand | `getShareText()` |
| **Search/filter** | Iterate all data, filter matches | `SearchViewModel.search()` |

## What Salam Does NOT Have (vs Athkarix)

- **No base ViewModel** — only 3 screens, no shared behavior
- **No SharedFlow** — no one-shot events (navigation is handled via callbacks)
- **No tap counter / advance logic** — pages are navigated via swipe only
- **No loading/error/content three-state** — simplified to loading/done
