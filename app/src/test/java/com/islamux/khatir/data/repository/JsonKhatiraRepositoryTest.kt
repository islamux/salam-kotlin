package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.KhatiraContent
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsonKhatiraRepositoryTest {

    private lateinit var jsonDecoder: Json

    @Before
    fun setUp() {
        jsonDecoder = Json { ignoreUnknownKeys = true }
    }

    @Test
    fun `parse full JSON from assets`() {
        val jsonString = this::class.java.classLoader
            ?.getResourceAsStream("khatira_content.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("khatira_content.json not found in test resources")

        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        assertEquals(1, content.version)
        assertEquals(34, content.chapters.size)

        val pre = content.chapters.find { it.id == "pre" }
        assertNotNull("pre chapter should exist", pre)
        assertEquals("المقدمة", pre?.title)

        val final = content.chapters.find { it.id == "final" }
        assertNotNull("final chapter should exist", final)

        val first = content.chapters.find { it.id == "1" }
        assertNotNull("chapter 1 should exist", first)

        var totalPages = 0
        for (chapter in content.chapters) {
            assertTrue(chapter.pages.isNotEmpty())
            totalPages += chapter.pages.size
        }
        assertTrue("total pages should be around 532", totalPages > 500)
    }

    @Test
    fun `chapter order matches ID mapping`() {
        val jsonString = this::class.java.classLoader
            ?.getResourceAsStream("khatira_content.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("khatira_content.json not found")

        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        val expectedOrder = listOf("pre") + (1..32).map { it.toString() } + listOf("final")
        val actualIds = content.chapters.map { it.id }

        assertEquals("Chapter IDs should match pre + 1-32 + final", expectedOrder, actualIds)
    }
}
