package com.islamux.khatir.ui.reader

import com.islamux.khatir.data.model.Chapter
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Tests ReaderViewModel on the JVM with a MockK fake repository — no emulator and
 * no real content file.
 *
 * The dispatcher setup works exactly as explained in HomeViewModelErrorStateTest
 * (setMain / StandardTestDispatcher / advanceUntilIdle / resetMain): read that file
 * first. What this suite adds is COVERAGE of the reader's own rules — missing
 * chapters, page bounds, the font limits and the share-text order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Tiny helper that builds a Chapter from a list of pages, so each test can
    // describe only the pages it cares about. `vararg` lets a test pass
    // Page(...), Page(...) directly instead of building a list by hand.
    private fun chapter(vararg pages: Page) = Chapter(
        id = "c1",
        orderIndex = 0,
        title = "عنوان الفصل",
        pages = pages.toList()
    )

    @Test
    fun `init loads chapter pages`() = runTest(dispatcher) {
        // given: two pages whose `order` lists name the fields to render
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter(
            Page(index = 0, titles = listOf("t0"), texts = listOf("body0"), order = listOf("titles", "texts")),
            Page(index = 1, texts = listOf("body1"), order = listOf("texts"))
        )

        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.pages.size)
        assertEquals(false, vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
        coVerify { repository.getChapter("c1") }
    }

    @Test
    fun `missing chapter exposes error`() = runTest(dispatcher) {
        // given: a repository that resolves the id to null (e.g. a stale deep link)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns null

        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Chapter not found", vm.uiState.value.error)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `repository failure exposes error`() = runTest(dispatcher) {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } throws RuntimeException("boom")

        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("boom", vm.uiState.value.error)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `navigateToPage clamps to valid indices`() = runTest(dispatcher) {
        // given: a two-page chapter
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter(
            Page(index = 0, order = listOf("texts")),
            Page(index = 1, order = listOf("texts"))
        )
        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        vm.navigateToPage(1)
        assertEquals(1, vm.uiState.value.currentPageIndex)

        // Out-of-range requests in BOTH directions are ignored, and the previous
        // valid page is kept — the ViewModel never publishes a broken index.
        vm.navigateToPage(5)
        assertEquals(1, vm.uiState.value.currentPageIndex)

        vm.navigateToPage(-1)
        assertEquals(1, vm.uiState.value.currentPageIndex)
    }

    @Test
    fun `fontSize stays within bounds`() = runTest(dispatcher) {
        // This test is the executable specification of the reader's font rules:
        // 21f is the floor AND the starting size, 37f is the ceiling, and the step
        // is 2f. `repeat(20)` deliberately overshoots the limit to prove the bound
        // holds no matter how many times the buttons are tapped.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter(Page(index = 0, order = listOf("texts")))
        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        vm.decreaseFontSize()
        assertEquals(21f, vm.uiState.value.fontSize)

        repeat(20) { vm.increaseFontSize() }
        assertEquals(37f, vm.uiState.value.fontSize)

        repeat(20) { vm.decreaseFontSize() }
        assertEquals(21f, vm.uiState.value.fontSize)
    }

    @Test
    fun `getShareText builds from page order`() = runTest(dispatcher) {
        // The two pages below are the interesting part: page 0 declares
        // order = ["titles", "texts"] while page 1 declares
        // order = ["texts", "titles"] — the OPPOSITE order. The expected strings
        // prove the share text follows `order`, not the order the properties
        // happen to be declared in.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter(
            Page(index = 0, titles = listOf("t0"), texts = listOf("body0"), order = listOf("titles", "texts")),
            Page(index = 1, titles = listOf("late-title"), texts = listOf("a1"), order = listOf("texts", "titles"))
        )
        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("t0\n\nbody0", vm.getShareText())

        vm.navigateToPage(1)
        assertEquals("a1\n\nlate-title", vm.getShareText())
    }

    @Test
    fun `getShareText is empty when chapter has no pages`() = runTest(dispatcher) {
        // A chapter with no pages at all: sharing must yield "" rather than crash,
        // because getShareText uses getOrNull and returns early.
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter()

        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.getShareText())
    }
}
