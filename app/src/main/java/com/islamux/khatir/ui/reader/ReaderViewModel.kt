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

/**
 * ReaderScreen's brain. It follows the exact MVVM patterns documented in
 * HomeViewModel.kt (backing property, viewModelScope, nested Factory) — read that
 * file first and this one needs no further explanation of the basics.
 *
 * What is unique here, and why the reader needs a ViewModel of its own:
 *  - it takes a chapterId, so each opened chapter gets its OWN ViewModel instance
 *    (see AppModule.provideReaderViewModelFactory and the `key = chapterId` in
 *    ReaderScreen),
 *  - it owns page navigation and the font-size bounds, so those rules live in one
 *    testable place instead of in button handlers,
 *  - it builds the share text, because turning a Page into plain text is logic,
 *    not UI.
 */
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
                    // The id did not resolve (a stale deep link, say). Publishing an
                    // error state lets the screen show a message instead of an
                    // endless spinner.
                    _uiState.value = ReaderUiState(error = "Chapter not found", isLoading = false)
                }
            } catch (e: Exception) {
                // Unlike HomeViewModel there is no `?:` fallback here, so an
                // exception carrying no message publishes `error = null`. Because
                // ReaderScreen's error branch is guarded by `error != null`, the
                // reader then falls through to its "no content" branch instead —
                // which is also why that screen's `?:` fallback is unreachable.
                _uiState.value = ReaderUiState(error = e.message, isLoading = false)
            }
        }
    }

    /**
     * Moves to [index], ignoring anything out of range.
     *
     * `index in pages.indices` is the bounds check: swiping past the last page, or
     * a bad page number arriving from a deep link, is silently dropped rather than
     * crashing on an out-of-bounds access. Verified by the test
     * `navigateToPage clamps to valid indices` — despite its name, it asserts the
     * page is IGNORED, not clamped.
     *
     * `copy()` publishes a new state with only this one field changed — the
     * previously loaded chapter and pages are carried over untouched.
     */
    fun navigateToPage(index: Int) {
        val pages = _uiState.value.pages
        if (index in pages.indices) {
            _uiState.value = _uiState.value.copy(currentPageIndex = index)
        }
    }

    /**
     * Grows the body text by 2sp, stopping at 37sp.
     *
     * The bound is enforced HERE, not in the button: the UI can call this freely
     * and a request at the limit simply does nothing, so there is no "too big"
     * state to render and no way for two different buttons to disagree about the
     * limit. ReaderViewModelTest pins the exact numbers (21f, 37f, step 2f).
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
     * The current page flattened into plain text for sharing.
     *
     * `getOrNull` instead of `[]`: a share pressed before the pages arrive returns
     * an empty string, and the screen then skips the share sheet instead of
     * crashing on an index that does not exist yet.
     */
    fun getShareText(): String {
        val state = _uiState.value
        val page = state.pages.getOrNull(state.currentPageIndex) ?: return ""
        return buildShareText(page)
    }

    /**
     * Walks the page's [Page.order] — the same rendering contract the screen uses
     * (see Page.kt and PageContent.kt) — so the shared text keeps the on-screen
     * order of titles, subtitles, texts, ayahs and footer.
     *
     * One source of truth for ordering: if the layout and the share text each
     * walked the lists in their own hardcoded order, a page would read correctly
     * on screen but be garbled when shared.
     *
     * - `mutableListOf<String>()` builds the result piece by piece, because the
     *   number of pieces is only known once `order` has been walked.
     * - `page.footer?.let { ... }` adds the footer only when it exists, since it
     *   is the one optional field on a page.
     *
     * The test `getShareText builds from page order` proves the order is honoured,
     * not the declaration order of the properties.
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

    /**
     * Builds this ViewModel for the Android system, injecting BOTH the repository
     * and the chapterId this reader was opened for. See HomeViewModel.Factory for
     * why a Factory exists at all and what the @Suppress is acknowledging.
     */
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
