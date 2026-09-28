package com.islamux.khatir.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import com.islamux.khatir.data.static.AppStrings
import com.islamux.khatir.util.removeSearchDiacritics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** One hit: its chapter and page, which field matched, and the original text. */
data class SearchResult(
    val chapter: Chapter,
    val page: Page,
    /** 0-based position of the page in its chapter. Currently unused by the UI. */
    val pageIndex: Int,
    /** The [Page.order] field name that matched. */
    val matchedField: String,
    val matchedText: String
)

/** What SearchScreen draws. Deliberately has no `error` field, unlike the other screens. */
data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val allChapters: List<Chapter> = emptyList()
)

/** Arabic label for a [Page.order] field name; unknown names yield "" rather than crashing. */
fun fieldLabel(field: String): String = when (field) {
    "titles" -> AppStrings.fieldTitle
    "subtitles" -> AppStrings.fieldSubtitle
    "texts" -> AppStrings.fieldText
    "ayahs" -> AppStrings.fieldAyah
    "footer" -> AppStrings.fieldFooter
    else -> ""
}

/** SearchScreen's brain; the diacritic-insensitive matching strategy is in [search]. */
class SearchViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    // Written in loadContent but never read: search works off uiState.allChapters.
    private var cachedContent: KhatiraContent? = null

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            // Exception swallowed on purpose so a bad content file cannot crash the
            // app; the user sees "no results" instead of a real error.
            try {
                val content = repository.getContent()
                cachedContent = content
                _uiState.value = _uiState.value.copy(allChapters = content.chapters)
            } catch (_: Exception) { }
        }
    }

    /**
     * Searches every field of every page for [query], matching case- and
     * diacritic-insensitively; only the first hit per field is kept.
     */
    fun search(query: String) {
        _uiState.value = _uiState.value.copy(query = query, isSearching = true)
        viewModelScope.launch {
            val chapters = _uiState.value.allChapters
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
                return@launch
            }
            val normalizedQuery = removeSearchDiacritics(query.lowercase())
            val results = mutableListOf<SearchResult>()
            for (chapter in chapters) {
                for ((pageIndex, page) in chapter.pages.withIndex()) {
                    for (field in page.order) {
                        // The footer is a single String, so it is wrapped in a
                        // one-item list to fit the shape of the other fields.
                        val values = when (field) {
                            "titles" -> page.titles
                            "subtitles" -> page.subtitles
                            "texts" -> page.texts
                            "ayahs" -> page.ayahs
                            "footer" -> if (page.footer != null) listOf(page.footer) else emptyList()
                            else -> emptyList()
                        }
                        val matchedValue = values.firstOrNull { value ->
                            removeSearchDiacritics(value.lowercase()).contains(normalizedQuery)
                        }
                        if (matchedValue != null) {
                            results.add(
                                SearchResult(
                                    chapter = chapter,
                                    page = page,
                                    pageIndex = pageIndex,
                                    matchedField = field,
                                    matchedText = matchedValue
                                )
                            )
                        }
                    }
                }
            }
            _uiState.value = _uiState.value.copy(results = results, isSearching = false)
        }
    }

    /** Builds the ViewModel for the system; no chapterId, one instance serves the app. */
    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}
