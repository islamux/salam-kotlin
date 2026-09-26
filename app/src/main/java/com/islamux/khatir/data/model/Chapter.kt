package com.islamux.khatir.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A themed group of [Page]s that the reader shows together.
 *
 * `@Serializable` (kotlinx.serialization) is what lets the JSON decoder in
 * JsonKhatiraRepository build instances of this class straight from the
 * assets/khatira_content.json file — no manual JSON parsing anywhere.
 */
@Serializable
data class Chapter(
    /** Stable id used in navigation routes and content links (e.g. "pre"). */
    val id: String,
    // The JSON key is "order_index" (snake_case). @SerialName maps it onto this
    // camelCase Kotlin property, so the asset file can stay snake_case while our
    // code follows Kotlin naming rules.
    @SerialName("order_index") val orderIndex: Int,
    /** Arabic display title for this chapter. */
    val title: String,
    /** The chapter's pages, in reading order. */
    val pages: List<Page>
)
