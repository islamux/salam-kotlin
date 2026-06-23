package com.islamux.khatir.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val index: Int,
    val titles: List<String> = emptyList(),
    val subtitles: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val ayahs: List<String> = emptyList(),
    val footer: String? = null,
    val order: List<String>
)
