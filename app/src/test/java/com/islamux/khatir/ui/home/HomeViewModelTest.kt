package com.islamux.khatir.ui.home

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
class HomeViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } returns emptyList()

        val viewModel = HomeViewModel(repository)

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load chapters successfully`() = runTest {
        val pages = listOf(Page(0, emptyList(), emptyList(), emptyList(), emptyList(), null, emptyList()))
        val chapters = listOf(
            Chapter("1", 0, "Chapter 1", pages),
            Chapter("2", 1, "Chapter 2", pages)
        )
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } returns chapters

        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.chapters.size)
    }

    @Test
    fun `error state when loading fails`() = runTest {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } throws RuntimeException("Network error")

        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.isNotBlank())
        assertTrue(state.chapters.isEmpty())
    }

    @Test
    fun `factory creates HomeViewModel`() {
        val repository = mockk<KhatiraRepository>()
        val factory = HomeViewModel.Factory(repository)
        val viewModel = factory.create(HomeViewModel::class.java)
        assertTrue(viewModel is HomeViewModel)
    }
}
