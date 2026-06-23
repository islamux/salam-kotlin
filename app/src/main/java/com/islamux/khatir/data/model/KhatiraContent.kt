package com.islamux.khatir.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KhatiraContent(
    val version: Int,
    @SerialName("generated_at") val generatedAt: String,
    val chapters: List<Chapter>
)
