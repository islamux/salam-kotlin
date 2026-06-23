package com.islamux.khatir.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: String,
    @SerialName("order_index") val orderIndex: Int,
    val title: String,
    val pages: List<Page>
)
