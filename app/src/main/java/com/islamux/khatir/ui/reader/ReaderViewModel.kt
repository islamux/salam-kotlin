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

/** ReaderScreen's brain: owns page navigation, the font-size bounds, and the share text. */
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
                    // Unresolved id (e.g. a stale deep link) so the screen can show a
                    // message instead of an endless spinner.
                    _uiState.value = ReaderUiState(error = "Chapter not found", isLoading = false)
                }
            } catch (e: Exception) {
                // A message-less exception publishes `error = null`, and because the
                // screen's error branch is guarded by `error != null` the reader then
                // shows "no content" instead.
                _uiState.value = ReaderUiState(error = e.message, isLoading = false)
            }
        }
    }

    /** Moves to [index], silently ignoring anything out of range. */
    fun navigateToPage(index: Int) {
        val pages = _uiState.value.pages
        if (index in pages.indices) {
            _uiState.value = _uiState.value.copy(currentPageIndex = index)
        }
    }

    /**
     * Grows the body text by 2sp, stopping at 37sp. The bound lives here rather than
     * in the button, so both buttons cannot disagree about the limit.
     */
    fun increaseFontSize() {
        val current = _uiState.value.fontSize
        if (current < 37f) {
            _uiState.value = _uiState.value.copy(fontSize = current + 2f)
        }
    }

    /** Shrinks the body text by 2sp, stopping at 21sp. The floor mirrors the default in ReaderUiState. */
    fun decreaseFontSize() {
        val current = _uiState.value.fontSize
        if (current > 21f) {
            _uiState.value = _uiState.value.copy(fontSize = current - 2f)
        }
    }

    /**
     * The current page as plain text, or "" if the pages have not arrived yet — a
     * share pressed early skips the sheet instead of crashing.
     */
    fun getShareText(): String {
        val state = _uiState.value
        val page = state.pages.getOrNull(state.currentPageIndex) ?: return ""
        return buildShareText(page)
    }

    /**
     * Flattens a page by walking its [Page.order], the same contract the screen
     * renders with, so shared text keeps the on-screen order.
     */
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

    /** Builds the ViewModel with the repository and the chapter this reader was opened for. */
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
