package com.islamux.khatir.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import com.islamux.khatir.data.repository.ReaderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: KhatiraRepository,
    private val chapterId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState

    init {
        loadChapter()
    }

    private fun loadChapter() {
        viewModelScope.launch {
            try {
                val chapter = repository.getChapter(chapterId)
                if (chapter != null) {
                    _uiState.value = ReaderUiState(
                        chapter = chapter,
                        pages = chapter.pages,
                        isLoading = false
                    )
                } else {
                    _uiState.value = ReaderUiState(error = "Chapter not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = ReaderUiState(error = e.message, isLoading = false)
            }
        }
    }

    fun navigateToPage(index: Int) {
        val pages = _uiState.value.pages
        if (index in pages.indices) {
            _uiState.value = _uiState.value.copy(currentPageIndex = index)
        }
    }

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

    class Factory(
        private val repository: KhatiraRepository,
        private val chapterId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(repository, chapterId) as T
        }
    }
}
