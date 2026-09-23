package com.islamux.khatir.ui.home

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
class HomeViewModelErrorStateTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `error state is exposed when repository throws`() = runTest(dispatcher) {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } throws RuntimeException("boom")

        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `success sets chapters and clears error`() = runTest(dispatcher) {
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } returns emptyList()

        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, vm.uiState.value.chapters.size)
        assertNull(vm.uiState.value.error)
        coVerify { repository.getAllChapters() }
    }
}
