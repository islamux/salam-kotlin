package com.islamux.khatir.data.repository

import com.islamux.khatir.data.model.KhatiraContent
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * JVM unit test (src/test — no emulator, runs in milliseconds).
 *
 * This is the ONE place where loading the real khatira_content.json is correct:
 * the thing under test IS the parsing of that file, so a fake would prove nothing.
 * Android's build puts the app's assets on the unit-test classpath, which is why
 * `classLoader.getResourceAsStream("khatira_content.json")` finds the very same
 * file the app ships.
 *
 * Backtick test names are legal in Kotlin: wrap the whole sentence in backticks
 * after `fun` — `fun parses the real JSON asset` becomes
 * fun `parses the real JSON asset`. They read far better than camelCase when a
 * test fails in CI output.
 */
class JsonKhatiraRepositoryTest {

    // `lateinit`: "initialized later" — the compiler skips its not-initialized check,
    // and @Before below guarantees it IS set before any test uses it.
    private lateinit var jsonDecoder: Json

    @Before
    fun setUp() {
        jsonDecoder = Json { ignoreUnknownKeys = true }
    }

    @Test
    fun `parse full JSON from assets`() {
        // given: the raw content file, straight from the classpath
        val jsonString = this::class.java.classLoader
            ?.getResourceAsStream("khatira_content.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("khatira_content.json not found in test resources")

        // when: it is decoded into our model
        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)

        // then: metadata, chapter count and the known chapters are all intact.
        // The version assert is a tripwire: if someone regenerates the content
        // file with a new format, THIS test fails loudly instead of the app
        // quietly misbehaving on a user's phone.
        assertEquals(1, content.version)
        assertEquals(34, content.chapters.size)

        // `?.` on `pre` below: assertNotNull does not smart-cast a local `val`,
        // so the safe call keeps the compiler happy without a null check.
        val pre = content.chapters.find { it.id == "pre" }
        assertNotNull("pre chapter should exist", pre)
        assertEquals("المقدمة", pre?.title)

        val final = content.chapters.find { it.id == "final" }
        assertNotNull("final chapter should exist", final)

        val first = content.chapters.find { it.id == "1" }
        assertNotNull("chapter 1 should exist", first)

        // Sanity-check the whole book in one pass: no chapter may be empty, and
        // the total page count must stay in the expected ballpark.
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

        // then: they must be exactly pre, 1..32, final — the reader and the
        // navigation routes both depend on this order being stable.
        assertEquals("Chapter IDs should match pre + 1-32 + final", expectedOrder, actualIds)
    }
}
