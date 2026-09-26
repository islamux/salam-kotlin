package com.islamux.khatir.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root object of assets/khatira_content.json — the entire book in one value.
 *
 * Decoded exactly once by JsonKhatiraRepository and then cached in memory, so
 * every screen sees the same [chapters] instance and no one re-reads the asset.
 *
 * The metadata fields ([version], [generatedAt]) are not read by any screen;
 * [version] is asserted in JsonKhatiraRepositoryTest so an unexpected content
 * file change is noticed, and [generatedAt] records when the file was produced.
 */
@Serializable
data class KhatiraContent(
    val version: Int,
    // Same @SerialName snake_case -> camelCase mapping as Chapter.orderIndex.
    @SerialName("generated_at") val generatedAt: String,
    val chapters: List<Chapter>
)
