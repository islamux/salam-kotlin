package com.islamux.khatir.ui.search

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", emptyList<Chapter>())

        val viewModel = SearchViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertFalse(state.isSearching)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `empty query clears results`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", emptyList<Chapter>())

        val viewModel = SearchViewModel(repository)

        viewModel.search("")

        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun `search finds matching text in titles`() = runTest {
        val pages = listOf(
            Page(0, listOf("عنوان مميز", "عنوان"), emptyList<String>(), emptyList<String>(), emptyList<String>(), null, listOf("titles"))
        )
        val chapters = listOf(Chapter("1", 0, "الفصل الأول", pages))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", chapters)

        val viewModel = SearchViewModel(repository)

        viewModel.search("مميز")

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("عنوان مميز", state.results[0].matchedText)
        assertEquals("titles", state.results[0].matchedField)
    }

    @Test
    fun `search finds matching text in texts`() = runTest {
        val pages = listOf(
            Page(0, emptyList<String>(), emptyList<String>(), listOf("نص البحث المطلوب"), emptyList<String>(), null, listOf("texts"))
        )
        val chapters = listOf(Chapter("1", 0, "الفصل الأول", pages))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", chapters)

        val viewModel = SearchViewModel(repository)

        viewModel.search("البحث")

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("نص البحث المطلوب", state.results[0].matchedText)
    }

    @Test
    fun `search is diacritic insensitive`() = runTest {
        val pages = listOf(
            Page(0, listOf("مَرْحَباً"), emptyList<String>(), emptyList<String>(), emptyList<String>(), null, listOf("titles"))
        )
        val chapters = listOf(Chapter("1", 0, "الفصل الأول", pages))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", chapters)

        val viewModel = SearchViewModel(repository)

        viewModel.search("مرحبا")

        assertEquals(1, viewModel.uiState.value.results.size)
    }

    @Test
    fun `no results for non-matching query`() = runTest {
        val pages = listOf(
            Page(0, emptyList<String>(), emptyList<String>(), listOf("بعض النصوص"), emptyList<String>(), null, listOf("texts"))
        )
        val chapters = listOf(Chapter("1", 0, "الفصل الأول", pages))
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getContent() } returns KhatiraContent(1, "2024-01-01", chapters)

        val viewModel = SearchViewModel(repository)

        viewModel.search("كلمة غير موجودة")

        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
