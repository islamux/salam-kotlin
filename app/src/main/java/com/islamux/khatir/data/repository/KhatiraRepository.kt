package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent

interface KhatiraRepository {
    suspend fun getContent(): KhatiraContent
    suspend fun getChapter(chapterId: String): Chapter?
    suspend fun getAllChapters(): List<Chapter>
}
