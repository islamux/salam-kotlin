package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.Page

data class ReaderUiState(
    val chapter: Chapter? = null,
    val pages: List<Page> = emptyList(),
    val currentPageIndex: Int = 0,
    val fontSize: Float = 21f,
    val isLoading: Boolean = true,
    val error: String? = null
)
