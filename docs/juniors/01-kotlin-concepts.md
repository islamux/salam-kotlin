# Kotlin Concepts Masterclass

A summary of every Kotlin language feature used in this codebase, with real Salamm examples.

## Salam-Specific Examples

### `data class` — Model Layer
```kotlin
// data/model/KhatiraContent.kt
@Serializable
data class KhatiraContent(
    val chapters: List<Chapter> = emptyList()
)

@Serializable
data class Chapter(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val pages: List<Page>
)

@Serializable
data class Page(
    val titles: List<String> = emptyList(),
    val subtitles: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val ayahs: List<String> = emptyList(),
    val footer: String? = null,
    val order: List<String> = emptyList()
)
```

### `object` — Singletons
```kotlin
// di/AppModule.kt — dependency injection
object AppModule {
    private var repository: KhatiraRepository? = null

    fun provideRepository(context: Context): KhatiraRepository {
        if (repository == null) {
            repository = JsonKhatiraRepository(context.applicationContext)
        }
        return repository!!
    }
}

// data/static/AppStrings.kt — all Arabic strings in one place
object AppStrings {
    const val homeAppBarTitle = "خواطر إيمانية"
    // ... 40+ constants
}
```

### `StateFlow` + Backing Property Pattern
Every ViewModel follows the exact same pattern:

```kotlin
class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())          // private mutable
    val uiState: StateFlow<HomeUiState> = _uiState                  // public read-only
}
```

This is explained in detail in [`04-viewmodel-deep-dive.md`](./04-viewmodel-deep-dive.md).

### Scope Functions — `let`, `apply`
```kotlin
// WhatsAppUtil.kt — share intent
val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, text)
    type = "text/plain"
}

// SearchViewModel.kt — safe null handling
cachedContent?.let { content ->
    _uiState.value = _uiState.value.copy(allChapters = content.chapters)
}
```

### Lambdas / Higher-Order Functions
```kotlin
// HomeScreen.kt — button click handlers
ChapterButton(
    title = AppStrings.chapterTitle(chapter.id),
    onClick = { onChapterClick(chapter.id) }   // ← lambda
)
```

### `when` Expression
```kotlin
// SearchViewModel.kt — field label mapping
fun fieldLabel(field: String): String = when (field) {
    "titles" -> AppStrings.fieldTitle
    "subtitles" -> AppStrings.fieldSubtitle
    "texts" -> AppStrings.fieldText
    "ayahs" -> AppStrings.fieldAyah
    "footer" -> AppStrings.fieldFooter
    else -> ""
}
```

### Extension Functions
```kotlin
// NavGraph.kt — `composable` is an extension function from AndroidX
NavHost(navController, startDestination = Routes.HOME) {
    composable(Routes.HOME) { /* ... */ }
}
```
