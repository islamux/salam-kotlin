# Unit Testing Learning Plan (for Juniors)

> Builds on `09-unit-testing-guide.md` and `10-unit-testing-implementation-guide.md` — read both first.

## Goal

Write real test files for the Salamm app, ordered by difficulty. Each file teaches a new skill. By the end, you'll have tested pure functions, ViewModels, mocks, coroutines, and search logic.

---

## Step 0: Add Missing Test Dependencies

The project has JUnit 4 but is missing three libraries needed for ViewModel and coroutine tests.

**File:** `app/build.gradle.kts` (inside `dependencies { }`)

```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.9")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

Or (preferred) add them to `gradle/libs.versions.toml` first, then reference as `libs.xxx`.

**Verify:**
```bash
./gradlew testDebugUnitTest
```
→ Should pass (no tests yet, but build succeeds).

---

## Step 1: `RemoveSearchDiacriticsTest` — Pure Function Tests

**File:** `app/src/test/java/com/islamux/khatir/util/RemoveSearchDiacriticsTest.kt`

**What it teaches:**
- `@Test` annotation and JUnit 4 basics
- Arrange → Act → Assert pattern
- Testing edge cases (null, empty, already-clean, non-Arabic)

**Full code:** See `10-unit-testing-implementation-guide.md` Lesson 1.

**Run:**
```bash
./gradlew testDebugUnitTest --tests "*RemoveSearchDiacriticsTest*"
```

---

## Step 2: `ReaderViewModelTest` — ViewModel State + Mocking

**File:** `app/src/test/java/com/islamux/khatir/ui/reader/ReaderViewModelTest.kt`

**What it teaches:**
- Mocking a repository with MockK (`mockk`, `coEvery`)
- `Dispatchers.setMain` with `StandardTestDispatcher`
- Reading `StateFlow.value` to verify state
- Testing bounds (min 21, max 37) and step behavior (increment by 2)

**Full code:** See `10-unit-testing-implementation-guide.md` Lesson 2.

**Run:**
```bash
./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*"
```

---

## Step 3: `HomeViewModelTest` — Coroutine ViewModel Tests

**File:** `app/src/test/java/com/islamux/khatir/ui/home/HomeViewModelTest.kt`

**What it teaches:**
- `runTest { }` to control coroutine execution
- Testing a ViewModel that launches work in `init`
- Happy path, empty data, and error scenarios

**Full code:** See `10-unit-testing-implementation-guide.md` Lesson 3.

**Run:**
```bash
./gradlew testDebugUnitTest --tests "*HomeViewModelTest*"
```

---

## Step 4: `SearchViewModelTest` — Search Logic Tests

**File:** `app/src/test/java/com/islamux/khatir/ui/search/SearchViewModelTest.kt`

**What it teaches:**
- Testing diacritic-insensitive search
- Testing `fieldLabel()` pure function separately
- Multiple mock scenarios with one repository

**Full code:** See `10-unit-testing-implementation-guide.md` Lesson 4.

**Run:**
```bash
./gradlew testDebugUnitTest --tests "*SearchViewModelTest*"
```

---

## Step 5: `JsonKhatiraRepositoryTest` — Data Layer Test

**File:** `app/src/test/java/com/islamux/khatir/data/repository/JsonKhatiraRepositoryTest.kt`

**What it teaches:**
- Reading a JSON file from `test/resources/`
- Deserialization with kotlinx.serialization
- Testing real data structure (chapters, pages, IDs)
- Assertions on collection size, item properties, ordering

✅ **Already written.** Read it as a reference example.

---

## Step 6: Run Everything

```bash
./gradlew testDebugUnitTest
```

Expected: **BUILD SUCCESSFUL** — all tests pass.

---

## What You'll Have Learned

| Step | Skill | New Concepts |
|------|-------|-------------|
| 0 | Setup | Gradle dependencies, version catalog |
| 1 | Pure function tests | `@Test`, `assertEquals`, edge cases |
| 2 | ViewModel state + mocks | `mockk`, `coEvery`, `StateFlow.value`, bounds |
| 3 | Coroutine ViewModels | `runTest`, `StandardTestDispatcher`, error handling |
| 4 | Search logic | Diacritic normalization, multi-field matching |
| 5 | Real data parsing | JSON deserialization, collection assertions |

## Checklist for Each Test

- [ ] Read the corresponding lesson in `10-unit-testing-implementation-guide.md`
- [ ] Create the test file in the correct package directory
- [ ] Copy and understand the code (don't just paste)
- [ ] Run the test — watch it pass
- [ ] Break the test intentionally — watch it fail (prove the test works)
- [ ] Fix it back

## Reference

| Doc | What It Covers |
|-----|---------------|
| `09-unit-testing-guide.md` | Unit testing concepts, Arrange-Act-Assert, how to run tests |
| `10-unit-testing-implementation-guide.md` | Full code for all 4 test files, step-by-step |
| This plan | Learning path to follow in order |
