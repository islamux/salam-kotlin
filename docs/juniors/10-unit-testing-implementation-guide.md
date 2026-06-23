# Unit Testing Implementation Guide

> Builds on concepts from `09-unit-testing-guide.md` — read that first if you haven't.

This guide walks you through adding unit tests to Salam step by step. Each lesson teaches one new concept by testing real code from the app.

---

## Setup: Add Test Dependencies

Open `app/build.gradle.kts` and add inside `dependencies { }`:

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.9")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

Create the test directory:

```bash
mkdir -p app/src/test/java/com/islamux/khatir
```

---

## Lesson 1: Pure Function Tests

**Target:** `removeSearchDiacritics()` — strips Arabic diacritics (tashkeel) from text.

**Test file:** `app/src/test/java/com/islamux/khatir/util/RemoveSearchDiacriticsTest.kt`

```kotlin
package com.islamux.khatir.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveSearchDiacriticsTest {

    @Test
    fun `remove strips all tashkeel from Arabic text`() {
        val withDiacritics = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val expected = "بسم الله الرحمن الرحيم"
        assertEquals(expected, removeSearchDiacritics(withDiacritics))
    }

    @Test
    fun `remove returns empty string for null input`() {
        assertEquals("", removeSearchDiacritics(null))
    }

    @Test
    fun `remove returns empty string for empty input`() {
        assertEquals("", removeSearchDiacritics(""))
    }

    @Test
    fun `remove leaves plain text unchanged`() {
        assertEquals("الحمد لله", removeSearchDiacritics("الحمد لله"))
    }

    @Test
    fun `remove handles text with no Arabic characters`() {
        assertEquals("Hello 123!", removeSearchDiacritics("Hello 123!"))
    }
}
```

**Key points:**
- `@Test` marks a function as a test.
- `assertEquals(expected, actual)` — order matters! Expected first.
- Test **edge cases**: null, empty, already-clean, non-Arabic.

**Run it:**
```bash
./gradlew testDebugUnitTest --tests "*RemoveSearchDiacriticsTest*"
```

---

## Lesson 2: ViewModel State Tests

**Target:** `ReaderViewModel` font size logic.

ViewModels expose `StateFlow`. Tests read `.value` to verify state.

**Test file:** `app/src/test/java/com/islamux/khatir/ui/reader/ReaderViewModelTest.kt`

```kotlin
package com.islamux.khatir.ui.reader

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReaderViewModelTest {

    private val repository: KhatiraRepository = mockk()
    private lateinit var viewModel: ReaderViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        val mockChapter = Chapter(
            id = "1",
            orderIndex = 1,
            title = "Test Chapter",
            pages = listOf(Page(titles = listOf("Page 1"), texts = listOf("Content 1")))
        )
        coEvery { repository.getChapter("1") } returns mockChapter
        viewModel = ReaderViewModel(repository, "1")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default font size is 21`() {
        assertEquals(21f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `increaseFontSize adds 2`() {
        viewModel.increaseFontSize()
        assertEquals(23f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `decreaseFontSize subtracts 2`() {
        viewModel.increaseFontSize()
        viewModel.decreaseFontSize()
        assertEquals(21f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `font size cannot go below 21`() {
        repeat(10) { viewModel.decreaseFontSize() }
        assertEquals(21f, viewModel.uiState.value.fontSize)
    }

    @Test
    fun `font size cannot go above 37`() {
        repeat(10) { viewModel.increaseFontSize() }
        assertEquals(37f, viewModel.uiState.value.fontSize)
    }
}
```

**Key points:**
- `mockk<T>()` creates a mock repository — no real JSON access needed.
- `coEvery { ... } returns ...` is MockK's coroutine version of `every`.
- `Dispatchers.setMain(StandardTestDispatcher())` ensures coroutines run predictably.
- Tests verify the **bounds** (21–37) and **step** (2) behavior.

**Run it:**
```bash
./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*"
```

---

## Lesson 3: Coroutine ViewModel Tests

**Target:** `HomeViewModel` — loads chapters from repository on init.

Since `HomeViewModel` launches a coroutine in `init`, we need `runTest` to control timing.

**Test file:** `app/src/test/java/com/islamux/khatir/ui/home/HomeViewModelTest.kt`

```kotlin
package com.islamux.khatir.ui.home

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.repository.KhatiraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val repository: KhatiraRepository = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadChapters sets chapters in uiState`() = runTest {
        val mockChapters = listOf(
            Chapter(id = "1", orderIndex = 0, title = "Chapter 1", pages = emptyList()),
            Chapter(id = "2", orderIndex = 1, title = "Chapter 2", pages = emptyList()),
        )
        coEvery { repository.getAllChapters() } returns mockChapters

        val viewModel = HomeViewModel(repository)

        assertEquals(mockChapters, viewModel.uiState.value.chapters)
        assertTrue(viewModel.uiState.value.chapters.isNotEmpty())
    }

    @Test
    fun `loadChapters handles empty list`() = runTest {
        coEvery { repository.getAllChapters() } returns emptyList()

        val viewModel = HomeViewModel(repository)

        assertTrue(viewModel.uiState.value.chapters.isEmpty())
        assertTrue(viewModel.uiState.value.isLoading.not())
    }

    @Test
    fun `loadChapters handles error gracefully`() = runTest {
        coEvery { repository.getAllChapters() } throws RuntimeException("Network error")

        val viewModel = HomeViewModel(repository)

        assertTrue(viewModel.uiState.value.chapters.isEmpty())
    }
}
```

**Key points:**
- `runTest { }` creates a test coroutine scope. Without it, `viewModelScope` calls crash.
- `coEvery` handles suspend functions (returns from coroutines).
- Test **happy path**, **empty data**, and **error** scenarios.

---

## Lesson 4: SearchViewModel Tests (Advanced)

**Target:** `SearchViewModel` — diacritic-insensitive search across chapters.

**Test file:** `app/src/test/java/com/islamux/khatir/ui/search/SearchViewModelTest.kt`

```kotlin
package com.islamux.khatir.ui.search

import com.islamux.khatir.data.model.Chapter
import com.islamux.khatir.data.model.KhatiraContent
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.data.repository.KhatiraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchViewModelTest {

    private val repository: KhatiraRepository = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        val content = KhatiraContent(
            chapters = listOf(
                Chapter(
                    id = "1", orderIndex = 0, title = "Chapter 1",
                    pages = listOf(
                        Page(
                            titles = listOf("بسم الله الرحمن الرحيم"),
                            texts = listOf("الحمد لله رب العالمين"),
                            order = listOf("titles", "texts")
                        )
                    )
                )
            )
        )
        coEvery { repository.getContent() } returns content
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search finds matching text`() = runTest {
        val viewModel = SearchViewModel(repository)
        viewModel.search("الحمد")

        assertTrue(viewModel.uiState.value.results.isNotEmpty())
        assertEquals("الحمد لله رب العالمين", viewModel.uiState.value.results.first().matchedText)
    }

    @Test
    fun `search is diacritic-insensitive`() = runTest {
        val viewModel = SearchViewModel(repository)
        viewModel.search("بِسْمِ")

        assertTrue(viewModel.uiState.value.results.isNotEmpty())
    }

    @Test
    fun `search returns empty for no match`() = runTest {
        val viewModel = SearchViewModel(repository)
        viewModel.search("xyz")

        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `empty query clears results`() = runTest {
        val viewModel = SearchViewModel(repository)
        viewModel.search("")  // should clear results

        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertTrue(viewModel.uiState.value.isSearching.not())
    }
}
```

---

## Running All Tests

```bash
# All tests
./gradlew testDebugUnitTest

# Specific test classes
./gradlew testDebugUnitTest --tests "*RemoveSearchDiacriticsTest*"
./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*"
./gradlew testDebugUnitTest --tests "*HomeViewModelTest*"
./gradlew testDebugUnitTest --tests "*SearchViewModelTest*"
```

Expected output:
```
BUILD SUCCESSFUL in Xs
```

---

## What We've Learned

| Lesson | Skill | Files Tested |
|--------|-------|-------------|
| 1 | Arrange → Act → Assert, edge cases | `RemoveSearchDiacriticsTest` |
| 2 | ViewModel StateFlow state verification | `ReaderViewModelTest` |
| 3 | Coroutine ViewModel with `runTest` + mocking | `HomeViewModelTest` |
| 4 | Search with diacritic-insensitive matching | `SearchViewModelTest` |

## What NOT to Test (Locally)

These need emulator/device tests (`androidTest`):
- Composable functions (UI rendering)
- `JsonKhatiraRepository` (needs `Context` for assets access)
- `WhatsAppUtil` (needs `Context` for `startActivity`)
- `AlertExitDialog` (Compose dialog rendering)

## Checklist for Adding Tests to New Code

1. Can it run on JVM without Android? → `test/` directory
2. Does it have Android deps? → `androidTest/` or refactor to extract pure logic
3. Does it use `viewModelScope`? → use `runTest { }`
4. Does it depend on other classes? → mock them with MockK
5. Did I test edge cases? → empty, null, max, min, boundary values
