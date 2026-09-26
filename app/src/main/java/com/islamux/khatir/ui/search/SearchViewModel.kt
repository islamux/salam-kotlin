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

/**
 * One search hit, with everything the UI needs to describe it: which chapter and
 * page it came from, which kind of field matched ("texts", "titles", ...) and the
 * matching text itself.
 *
 * [matchedText] is the ORIGINAL string from the content file, harakat and all —
 * only the COMPARISON used normalized text. The reader therefore sees the text
 * exactly as it is written in the book.
 */
data class SearchResult(
    val chapter: Chapter,
    val page: Page,
    /** 0-based position of the page inside its chapter. */
    val pageIndex: Int,
    /** The `order` field name that matched — see Page.order. */
    val matchedField: String,
    val matchedText: String
)

/**
 * What SearchScreen draws.
 *
 * Note what is NOT here: there is no `error` field, unlike HomeUiState and
 * ReaderUiState. A failed content load therefore leaves the list empty, so the
 * screen shows its usual prompt and then "no results" once the user types —
 * search degrades quietly instead of showing an error (see loadContent below).
 */
data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val allChapters: List<Chapter> = emptyList()
)

/**
 * Maps a `Page.order` field name to the Arabic label shown under a result, using
 * the field* constants in AppStrings. An unknown name yields "" rather than
 * crashing, so a content file with a new field still renders.
 *
 * A top-level function rather than a SearchViewModel member: it needs no state,
 * so it can be called directly from the composable.
 */
fun fieldLabel(field: String): String = when (field) {
    "titles" -> AppStrings.fieldTitle
    "subtitles" -> AppStrings.fieldSubtitle
    "texts" -> AppStrings.fieldText
    "ayahs" -> AppStrings.fieldAyah
    "footer" -> AppStrings.fieldFooter
    else -> ""
}

/**
 * SearchScreen's brain, following the MVVM patterns documented in HomeViewModel
 * (backing property, viewModelScope, nested Factory) — read that file first.
 *
 * The interesting part is the matching strategy, documented on [search].
 */
class SearchViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    // Currently written in loadContent but never read: the search itself works off
    // uiState.allChapters. Kept because it is the natural place to hold the book
    // if a future feature needs it, and because the repository already caches the
    // same object — so removing it would not change behaviour.
    private var cachedContent: KhatiraContent? = null

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            // The exception below is swallowed on purpose: the chapters simply stay
            // empty, so a broken or missing content file can never crash the app on
            // the search screen. The trade-off is that the user sees "no results"
            // instead of a real error message.
            try {
                val content = repository.getContent()
                cachedContent = content
                // `copy()` publishes a new state carrying the chapters over while
                // keeping the current query and results.
                _uiState.value = _uiState.value.copy(allChapters = content.chapters)
            } catch (_: Exception) { }
        }
    }

    /**
     * Searches the whole book for [query].
     *
     * How it works, step by step:
     *  1. The query and the `isSearching` flag are published SYNCHRONOUSLY, before
     *     the coroutine starts, so the UI can show its "searching..." line at once.
     *  2. A blank (empty or whitespace-only) query clears the results and returns —
     *     `return@launch` exits the coroutine, not the function.
     *  3. The query is normalized ONCE: lowercase plus diacritic removal.
     *  4. Three nested loops walk chapter -> page -> each field name in
     *     `page.order` (the same contract the reader uses, see Page.kt), so a field
     *     the page does not declare is never searched.
     *  5. Every candidate value is normalized the SAME way before comparing, which
     *     is what makes the search diacritic-insensitive: the query "سلام" matches
     *     the content "السَّلَام عليكم". See removeSearchDiacritics.
     *  6. `firstOrNull` keeps only the FIRST matching value per field, so a page
     *     with three matching paragraphs yields one result, not three near
     *     identical rows.
     *
     * @param query raw text as typed; stored unchanged in the state so the text
     * field keeps showing exactly what the user typed.
     */
    fun search(query: String) {
        _uiState.value = _uiState.value.copy(query = query, isSearching = true)
        viewModelScope.launch {
            val chapters = _uiState.value.allChapters
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
                return@launch
            }
            // Normalize the query ONCE, outside the loops, instead of per comparison.
            val normalizedQuery = removeSearchDiacritics(query.lowercase())
            val results = mutableListOf<SearchResult>()
            for (chapter in chapters) {
                // `withIndex()` gives the position together with the element, so
                // each result can report which page it came from.
                for ((pageIndex, page) in chapter.pages.withIndex()) {
                    for (field in page.order) {
                        // Resolve the field name to the values to search in. The
                        // footer is a single String, so it is wrapped in a one-item
                        // list to fit the same shape as the other fields.
                        val values = when (field) {
                            "titles" -> page.titles
                            "subtitles" -> page.subtitles
                            "texts" -> page.texts
                            "ayahs" -> page.ayahs
                            "footer" -> if (page.footer != null) listOf(page.footer) else emptyList()
                            else -> emptyList()
                        }
                        // `contains` is a substring test, so partial words match.
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

    /**
     * Builds this ViewModel for the Android system. No chapterId here — unlike
     * ReaderViewModel, one search ViewModel serves the whole app.
     */
    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}
