package com.islamux.khatir.data.model

import kotlinx.serialization.Serializable

/**
 * One page inside a [Chapter], split into per-field lists rather than one blob.
 *
 * RENDERING CONTRACT: the UI walks [order] and, on each name, renders the next unused
 * element of that field's list. So the same lists with a different [order] render
 * differently — never assume alphabetical or any other ordering. Implemented in
 * PageContent (rendering), ReaderViewModel.buildShareText (sharing) and SearchViewModel
 * (searching).
 */
@Serializable
data class Page(
    /** 0-based position of this page inside its chapter. */
    val index: Int,
    /** Section headings; a name may repeat in [order]. */
    val titles: List<String> = emptyList(),
    /** Secondary headings, rendered smaller than [titles]. */
    val subtitles: List<String> = emptyList(),
    /** Body paragraphs. */
    val texts: List<String> = emptyList(),
    /** Qur'anic verses and hadith, rendered centered. */
    val ayahs: List<String> = emptyList(),
    /** Optional closing note; absent on most pages. */
    val footer: String? = null,
    /** Exact render sequence; every value is one of "titles", "subtitles", "texts", "ayahs", "footer". */
    val order: List<String>
)
