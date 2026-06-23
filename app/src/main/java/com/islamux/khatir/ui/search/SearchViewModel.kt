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

fun fieldLabel(field: String): String = when (field) {
    "titles" -> AppStrings.fieldTitle
    "subtitles" -> AppStrings.fieldSubtitle
    "texts" -> AppStrings.fieldText
    "ayahs" -> AppStrings.fieldAyah
    "footer" -> AppStrings.fieldFooter
    else -> ""
}

class SearchViewModel(private val repository: KhatiraRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var cachedContent: KhatiraContent? = null

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            try {
                val content = repository.getContent()
                cachedContent = content
                _uiState.value = _uiState.value.copy(allChapters = content.chapters)
            } catch (_: Exception) { }
        }
    }

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

    class Factory(private val repository: KhatiraRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}
