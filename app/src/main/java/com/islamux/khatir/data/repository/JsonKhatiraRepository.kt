package com.islamux.khatir.data.repository

import android.content.Context
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
import kotlinx.serialization.json.Json

class JsonKhatiraRepository(private val context: Context) : KhatiraRepository {

    private var cachedContent: KhatiraContent? = null

    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    override suspend fun getContent(): KhatiraContent {
        cachedContent?.let { return it }
        val jsonString = context.assets
            .open("khatira_content.json")
            .bufferedReader()
            .use { it.readText() }
        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)
        cachedContent = content
        return content
    }

    override suspend fun getChapter(chapterId: String): Chapter? {
        return getContent().chapters.find { it.id == chapterId }
    }

    override suspend fun getAllChapters(): List<Chapter> {
        return getContent().chapters
    }
}
