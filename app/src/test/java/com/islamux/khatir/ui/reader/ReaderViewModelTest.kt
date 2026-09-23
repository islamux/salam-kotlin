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

    private fun chapter(vararg pages: Page) = Chapter(
        id = "c1",
        orderIndex = 0,
        title = "عنوان الفصل",
        pages = pages.toList()
    )

    @Test
    fun `init loads chapter pages`() = runTest(dispatcher) {
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
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter(
            Page(index = 0, order = listOf("texts")),
            Page(index = 1, order = listOf("texts"))
        )
        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        vm.navigateToPage(1)
        assertEquals(1, vm.uiState.value.currentPageIndex)

        vm.navigateToPage(5)
        assertEquals(1, vm.uiState.value.currentPageIndex)

        vm.navigateToPage(-1)
        assertEquals(1, vm.uiState.value.currentPageIndex)
    }

    @Test
    fun `fontSize stays within bounds`() = runTest(dispatcher) {
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
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("c1") } returns chapter()

        val vm = ReaderViewModel(repository, "c1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.getShareText())
    }
}
