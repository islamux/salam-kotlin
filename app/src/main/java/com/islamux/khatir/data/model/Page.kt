package com.islamux.khatir.data.model

import kotlinx.serialization.Serializable

/**
 * One page inside a [Chapter] — the heart of the content model.
 *
 * A page deliberately does NOT store one big text blob. It keeps each kind of
 * content in its own list, and [order] decides the sequence in which the reader
 * screen renders them (see ui/reader/components/PageContent.kt).
 *
 * THE RENDERING CONTRACT (memorize this one):
 *   The UI walks [order] from first element to last. Every time it meets
 *   "titles" it renders the NEXT unused element of [titles] and advances an
 *   internal index; the same for "subtitles", "texts", "ayahs". "footer"
 *   renders [footer] once, if present. So the SAME lists with a DIFFERENT
 *   [order] produce a visibly different page — never assume alphabetical order
 *   or any other ordering. Three places implement this walk: PageContent
 *   (rendering), ReaderViewModel.buildShareText (sharing), SearchViewModel
 *   (searching).
 *
 * Kotlin concepts used in this file:
 *  - `data class`: the compiler auto-generates equals(), hashCode(), toString()
 *    and copy(). `copy()` builds a NEW object changing only the fields you name
 *    — that is how the ViewModels publish updated state (see ReaderUiState).
 *  - Default values (`= emptyList()`): applied automatically when the JSON
 *    omits the key, so old content files keep parsing.
 *  - `String?` (nullable): [footer] may legitimately be absent in the JSON.
 *    Reading a nullable needs `?.` (safe call — null instead of a crash) or
 *    `?:` (elvis — left side if not null, otherwise the right side).
 */
@Serializable
data class Page(
    /** 0-based position of this page inside its chapter (the first page is 0). */
    val index: Int,
    /** Section headings. A name may repeat in [order]; each occurrence renders the next element. */
    val titles: List<String> = emptyList(),
    /** Secondary headings, rendered slightly smaller than [titles]. */
    val subtitles: List<String> = emptyList(),
    /** Body paragraphs — the bulk of the reading experience. */
    val texts: List<String> = emptyList(),
    /** Qur'anic verses and hadith, rendered centered in a distinct style. */
    val ayahs: List<String> = emptyList(),
    /** Optional closing note. Nullable because most pages have none. */
    val footer: String? = null,
    /**
     * Field names in exact rendering sequence. Every value must be exactly one
     * of: "titles", "subtitles", "texts", "ayahs", "footer".
     */
    val order: List<String>
)
