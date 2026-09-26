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

/**
 * Tests HomeViewModel's failure and success paths on the JVM — no emulator, no
 * real JSON, no Android framework.
 *
 * This works BECAUSE HomeViewModel depends on the KhatiraRepository INTERFACE
 * (that is the payoff of the abstraction): MockK builds a fake repository, so
 * the ViewModel runs in isolation in milliseconds.
 *
 * The dispatcher dance below is what makes coroutines testable:
 *  - viewModelScope uses Dispatchers.Main, which does not exist on a plain JVM,
 *    so setMain swaps in a test dispatcher.
 *  - StandardTestDispatcher does NOT run work immediately; it queues it. That is
 *    why each test calls advanceUntilIdle() to let the queued coroutine finish
 *    BEFORE asserting on the state it produced.
 *  - tearDown resets Main so the swap cannot leak into other test classes.
 */
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
        // given: a repository that fails
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } throws RuntimeException("boom")

        // when: the ViewModel is created (its init block starts loading)
        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        // then: loading has stopped and an error is published for the UI to show
        assertEquals(false, vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `success sets chapters and clears error`() = runTest(dispatcher) {
        // given: a repository that answers with no chapters
        val repository = mockk<KhatiraRepository>()
        coEvery { repository.getAllChapters() } returns emptyList()

        // when: the ViewModel loads
        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        // then: the list is published, the error is cleared, and the repository was
        // actually asked — coVerify asserts the call happened, not just the result.
        assertEquals(0, vm.uiState.value.chapters.size)
        assertNull(vm.uiState.value.error)
        coVerify { repository.getAllChapters() }
    }
}
