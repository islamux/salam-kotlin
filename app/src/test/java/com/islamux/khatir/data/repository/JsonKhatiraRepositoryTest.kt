package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.KhatiraContent
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * JVM unit test (src/test — no emulator). The ONE place loading the real
 * khatira_content.json is correct: the thing under test IS that file's parsing, and
 * Android's build puts the app's assets on the unit-test classpath.
 */
class JsonKhatiraRepositoryTest {

    // `lateinit` defers the not-initialized check; @Before guarantees it is set.
    private lateinit var jsonDecoder: Json

    @Before
    fun setUp() {
        jsonDecoder = Json { ignoreUnknownKeys = true }
    }

    @Test
    fun `parse full JSON from assets`() {
        // given: the raw content file from the classpath
        val jsonString = this::class.java.classLoader
            ?.getResourceAsStream("khatira_content.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("khatira_content.json not found in test resources")

        // when: it is decoded into our model
        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        // then: metadata, chapter count and known chapters are intact. The version
        // assert is a tripwire for a regenerated content file in a new format.
        assertEquals(1, content.version)
        assertEquals(34, content.chapters.size)

        // `?.` because assertNotNull does not smart-cast a local `val`.
        val pre = content.chapters.find { it.id == "pre" }
        assertNotNull("pre chapter should exist", pre)
        assertEquals("المقدمة", pre?.title)

        val final = content.chapters.find { it.id == "final" }
        assertNotNull("final chapter should exist", final)

        val first = content.chapters.find { it.id == "1" }
        assertNotNull("chapter 1 should exist", first)

        // No chapter may be empty, and the total page count must stay in range.
        var totalPages = 0
        for (chapter in content.chapters) {
            assertTrue(chapter.pages.isNotEmpty())
            totalPages += chapter.pages.size
        }
        assertTrue("total pages should be around 532", totalPages > 500)
    }

    @Test
    fun `chapter order matches ID mapping`() {
        // given: the same real asset
        val jsonString = this::class.java.classLoader
            ?.getResourceAsStream("khatira_content.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("khatira_content.json not found")

        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        // when: we ask for the ids in file order
        val expectedOrder = listOf("pre") + (1..32).map { it.toString() } + listOf("final")
        val actualIds = content.chapters.map { it.id }

        // then: exactly pre, 1..32, final — the reader and routes depend on that order.
        assertEquals("Chapter IDs should match pre + 1-32 + final", expectedOrder, actualIds)
    }
}
