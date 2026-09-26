package com.islamux.khatir.data.repository

import android.content.Context
import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
import kotlinx.serialization.json.Json

/**
 * [KhatiraRepository] implementation that reads assets/khatira_content.json once
 * and keeps the decoded [KhatiraContent] in memory for the rest of the app's life.
 *
 * Why caching is safe AND correct here: the asset ships inside the APK and cannot
 * change while the app runs, so the second and later calls are pure memory reads.
 */
class JsonKhatiraRepository(private val context: Context) : KhatiraRepository {

    // Null means "not loaded yet" — the cache is filled on the first getContent().
    private var cachedContent: KhatiraContent? = null

    // `Json { ignoreUnknownKeys = true }`: if the content file later gains new keys,
    // parsing still succeeds instead of throwing — forward compatibility.
    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    override suspend fun getContent(): KhatiraContent {
        // `?.let`: if cachedContent is non-null, run this block (return it) and skip
        // everything below. Concise equivalent of a null check + early return.
        cachedContent?.let { return it }

        // Open the asset as a text stream. `.use { }` closes the stream AUTOMATICALLY
        // even if reading throws (Java's try-with-resources equivalent) — forget to
        // close it and we leak a file handle.
        val jsonString = context.assets
            .open("khatira_content.json")
            .bufferedReader()
            .use { it.readText() }

        // decodeFromString<T> is kotlinx.serialization reading the JSON straight into
        // our data classes — no manual parsing, no intermediate maps.
        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        // Cache only AFTER a successful decode: if parsing throws, the next call must
        // retry the file instead of handing out a half-built object.
        cachedContent = content
        return content
    }

    // `find` returns the first match or null; the ViewModels turn that null into an
    // error state, so an unknown id never crashes the reader.
    override suspend fun getChapter(chapterId: String): Chapter? {
        return getContent().chapters.find { it.id == chapterId }
    }

    override suspend fun getAllChapters(): List<Chapter> {
        return getContent().chapters
    }
}
