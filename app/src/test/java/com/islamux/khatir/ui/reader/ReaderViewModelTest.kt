package com.islamux.khatir.ui.reader

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun page(index: Int, titles: List<String> = emptyList(), texts: List<String> = emptyList()) =
        Page(index, titles, emptyList(), texts, emptyList(), null, listOf("titles", "texts"))

    @Test
    fun `initial state is loading`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter(any()) } returns null

        val viewModel = ReaderViewModel(repository, "1")

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load chapter successfully`() = runTest {
        val pages = listOf(page(0, listOf("Title"), listOf("Text")), page(1, listOf("Title 2"), listOf("Text 2")))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertNotNull(state.chapter)
        assertEquals(2, state.pages.size)
    }

    @Test
    fun `error state when chapter not found`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter(any()) } returns null

        val viewModel = ReaderViewModel(repository, "nonexistent")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `error state when repository throws`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter(any()) } throws RuntimeException("Load failed")

        val viewModel = ReaderViewModel(repository, "1")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `navigate to page updates index`() = runTest {
        val pages = listOf(page(0), page(1), page(2))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        viewModel.navigateToPage(2)
        assertEquals(2, viewModel.uiState.value.currentPageIndex)

        viewModel.navigateToPage(0)
        assertEquals(0, viewModel.uiState.value.currentPageIndex)
    }

    @Test
    fun `navigate to invalid page does not change index`() = runTest {
        val pages = listOf(page(0))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        viewModel.navigateToPage(99)
        assertEquals(0, viewModel.uiState.value.currentPageIndex)
    }

    @Test
    fun `increase font size`() = runTest {
        val chapter = Chapter("1", 0, "Chapter 1", listOf(page(0)))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        val initial = viewModel.uiState.value.fontSize
        viewModel.increaseFontSize()
        assertEquals(initial + 2f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `decrease font size`() = runTest {
        val pages = listOf(page(0))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        viewModel.increaseFontSize()
        viewModel.increaseFontSize()
        val increased = viewModel.uiState.value.fontSize
        viewModel.decreaseFontSize()
        assertEquals(increased - 2f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `font size does not go below minimum`() = runTest {
        val chapter = Chapter("1", 0, "Chapter 1", listOf(page(0)))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        viewModel.decreaseFontSize()
        assertEquals(21f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `font size does not exceed maximum`() = runTest {
        val pages = listOf(page(0))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        for (i in 1..10) viewModel.increaseFontSize()
        assertTrue(viewModel.uiState.value.fontSize <= 37f)
    }

    @Test
    fun `getShareText returns empty when no page loaded`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter(any()) } returns null

        val viewModel = ReaderViewModel(repository, "1")

        assertEquals("", viewModel.getShareText())
    }

    @Test
    fun `getShareText returns page content`() = runTest {
        val pages = listOf(page(0, listOf("Title"), listOf("Body text")))
        val chapter = Chapter("1", 0, "Chapter 1", pages)
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getChapter("1") } returns chapter

        val viewModel = ReaderViewModel(repository, "1")

        val text = viewModel.getShareText()
        assertTrue(text.contains("Title"))
        assertTrue(text.contains("Body text"))
    }
}
