package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.Page

/**
 * Everything the reader screen needs to draw itself, bundled into ONE immutable
 * object. This is the "state" half of MVVM.
 *
 * How it flows: ReaderViewModel OWNS this state and publishes a new copy through
 * its StateFlow whenever something changes; ReaderScreen observes and renders it.
 * Nothing mutates a ReaderUiState in place — `copy()` builds each new version
 * changing only the fields that changed (e.g. `copy(currentPageIndex = 2)`).
 *
 * The default values ARE the initial loading state: no chapter yet, no pages,
 * nothing failed, still loading. That is why [isLoading] starts true — so the
 * very first frame draws a spinner instead of an empty screen.
 */
data class ReaderUiState(
    /** The chapter being read; null until the repository answers. */
    val chapter: Chapter? = null,
    /** Its pages — the pager renders exactly this list. */
    val pages: List<Page> = emptyList(),
    /** Which page is showing, 0-based (matches Page.index). */
    val currentPageIndex: Int = 0,
    /** Body text size in sp. Bounded to 21f..37f by ReaderViewModel. */
    val fontSize: Float = 21f,
    /** True while the repository call is in flight. */
    val isLoading: Boolean = true,
    /** Non-null only when loading failed; the UI shows it as an error view. */
    val error: String? = null
)
