# Unit Testing Guide for Juniors (No Emulator Required)

> **Note:** This document is adapted from the Athkarix project's `09-unit-testing-guide.md`. The concepts are identical — only the code examples and test targets differ.

Welcome! As a junior developer working on our Kotlin Android app, testing might seem daunting, especially if you think you always need to boot up an Android emulator or use a real device.

**Good news!** You can test most of your logic *locally* on your computer without any emulator. These are called **Local Unit Tests** (or JVM tests). They are fast, reliable, and run directly on your computer's Java Virtual Machine (JVM).

## Why Local Unit Tests?

- **Lightning Fast:** They run in seconds instead of minutes.
- **No Emulator Hassle:** You don't need a heavy emulator taking up your computer's memory.
- **Immediate Feedback:** Perfect for checking if a specific function, calculation, or `ViewModel` logic works correctly.

---

## Where to Put Your Tests

In Android projects, code is separated into folders.

1. `src/main/java/` → Your actual app code.
2. `src/test/java/` → **This is where your Local Unit Tests go!** (These run on your JVM).
3. `src/androidTest/java/` → These are for UI/Integration tests that *require* an emulator (Ignore these for now!).

For Salam:
```
app/src/test/java/com/islamux/khatir/
```

---

## Writing Your First Test

Let's test a simple utility function from Salam — the diacritic remover used in search.

**The Code (`util/remove_search_diacritics.kt`):**
```kotlin
fun removeSearchDiacritics(text: String?): String {
    if (text == null) return ""
    return text.filter { it in '\u0621'..'\u064A' || it.isWhitespace() }
}
```

**The Test (`src/test/java/.../RemoveSearchDiacriticsTest.kt`):**
```kotlin
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveSearchDiacriticsTest {

    @Test
    fun `remove strips diacritics from Arabic text`() {
        val withDiacritics = "بِسْمِ اللَّهِ"
        val expected = "بسم الله"

        val result = removeSearchDiacritics(withDiacritics)

        assertEquals(expected, result)
    }
}
```

### The "Arrange, Act, Assert" Pattern
- **Arrange:** Setup the data (`withDiacritics`, `expected`).
- **Act:** Execute the function (`removeSearchDiacritics(...)`).
- **Assert:** Verify the outcome (`assertEquals(...)`).

---

## Testing ViewModels

ViewModels expose `StateFlow` objects. You can read `.value` directly in tests:

```kotlin
class ReaderViewModelTest {

    private val viewModel = ReaderViewModel(...)

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
    fun `font size cannot go below 21`() {
        repeat(10) { viewModel.decreaseFontSize() }
        assertEquals(21f, viewModel.uiState.value.fontSize)
    }
}
```

**Getting Started:** See [`10-unit-testing-implementation-guide.md`](./10-unit-testing-implementation-guide.md) for step-by-step implementation walkthroughs with real Salam code.

---

## How to Run Your Tests

### Using Android Studio
1. Open a test file.
2. Click the green "Play" icon ▶ next to a `@Test` function or class name.
3. Select **Run**.

### Using Command Line (Terminal)
```bash
# All local unit tests
./gradlew testDebugUnitTest

# A specific test class
./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*"

# A specific test function
./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*default font size*"
```

---

## Summary

- Put your tests in `src/test/java/`.
- Use the `@Test` annotation.
- Follow the Arrange, Act, Assert pattern.
- If it needs Android components (like Context, Views, Emulator), it belongs in `androidTest`. Otherwise, keep it in `test`!
- Run tests via the green play button in your IDE or `./gradlew testDebugUnitTest` in terminal.
