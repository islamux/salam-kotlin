package com.islamux.khatir.ui.search

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests SearchViewModel on the JVM against a small in-memory book, with the
 * standard dispatcher setup explained in HomeViewModelErrorStateTest (read that
 * file first for setMain / advanceUntilIdle / resetMain).
 *
 * The star of this suite is `search matches diacritic-insensitively`, which pins
 * the feature the whole screen exists for.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // A tiny hand-built book: one chapter, two pages. Note page 0's text carries
    // harakat ("السَّلَام") while page 1's does not — that contrast is what makes
    // the diacritic test below meaningful.
    private fun content() = KhatiraContent(
        version = 1,
        generatedAt = "2026-01-01",
        chapters = listOf(
            Chapter(
                id = "c1",
                orderIndex = 0,
                title = "عنوان الفصل",
                pages = listOf(
                    Page(index = 0, texts = listOf("السَّلَام عليكم"), order = listOf("texts")),
                    Page(index = 1, titles = listOf("نص آخر"), order = listOf("titles"))
                )
            )
        )
    )

    @Test
    fun `init loads all chapters`() = runTest(dispatcher) {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns content()

        val vm = SearchViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.allChapters.size)
        assertEquals("", vm.uiState.value.query)
        coVerify { repository.getContent() }
    }

    @Test
    fun `search matches diacritic-insensitively`() = runTest(dispatcher) {
        // THE core promise of this screen: the user types the bare word "سلام",
        // without any harakat, and it still finds the content that has them.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns content()
        val vm = SearchViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        vm.search("سلام")
        dispatcher.scheduler.advanceUntilIdle()

        val results = vm.uiState.value.results
        assertEquals(1, results.size)
        assertEquals("texts", results[0].matchedField)
        // The stored match is the ORIGINAL text with its harakat, not the
        // normalized form — normalization is for comparing, never for display.
        assertEquals("السَّلَام عليكم", results[0].matchedText)
        assertEquals(0, results[0].pageIndex)
        // The query is echoed back exactly as typed, harakat-free, so the text
        // field does not fight the user's input.
        assertEquals("سلام", vm.uiState.value.query)
        assertEquals(false, vm.uiState.value.isSearching)
    }

    @Test
    fun `search finds no match when query absent`() = runTest(dispatcher) {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns content()
        val vm = SearchViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        vm.search("غير موجود")
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.results.isEmpty())
        assertEquals(false, vm.uiState.value.isSearching)
    }

    @Test
    fun `blank query clears results`() = runTest(dispatcher) {
        // Proves the isBlank() guard: a whitespace-only query is treated as "no
        // query" (results cleared) while the typed text is still preserved.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns content()
        val vm = SearchViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        vm.search("سلام")
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.uiState.value.results.size)

        vm.search("   ")
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.results.isEmpty())
        assertEquals("   ", vm.uiState.value.query)
    }

    @Test
    fun `content load failure leaves chapters empty without crashing`() = runTest(dispatcher) {
        // Documents the deliberate design choice in loadContent: the exception is
        // swallowed, so a broken content file cannot crash the app. The cost is
        // that the user gets an empty result list rather than an error message.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } throws RuntimeException("boom")

        val vm = SearchViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.allChapters.isEmpty())
        vm.search("سلام")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value.results.isEmpty())
    }
}
