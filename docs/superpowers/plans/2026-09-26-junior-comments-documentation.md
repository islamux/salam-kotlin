# Junior-Onboarding Comments — Full Project Documentation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add teaching-quality KDoc/inline comments to every Kotlin file in the app so a junior developer who is new to Kotlin (and to Android/Compose) can understand the project from A to Z by reading the code top to bottom.

**Architecture:** This is a pure documentation change — no behavior changes, no new files except this plan. Tasks follow the app's actual runtime flow (entry point → data → ViewModels → UI → utilities), which is also the recommended reading order for the junior. Every task ends with a build + test verification so we never accidentally break code while commenting it.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), MVVM, StateFlow, kotlinx.serialization, manual DI, Navigation Compose.

**Spec:** User request (2026-09-26): "add comments to the project… for a new junior who is new to Kotlin… more details… best practices… help him understand the project from A to Z." Plus `AGENTS.md` project conventions.

## Global Constraints

- **This plan is an explicit exception to `AGENTS.md`'s "No comments" rule.** The rule stays in force for all *future* code; do NOT edit `AGENTS.md`.
- Comment language: **English only** (simple vocabulary, short sentences). Never translate or "explain away" the Arabic content strings in `AppStrings.kt` / JSON.
- Comment style: **KDoc (`/** */`) on declarations**, `//` for inline teaching notes. No `/* */` block comments inside function bodies.
- Explain **WHY and the concept**, not a line-by-line echo of WHAT the code does. A comment that just restates the code is noise.
- Every Kotlin comment must teach at most **one** idea, and every non-trivial Kotlin idiom used in this codebase must be explained **once, at its first occurrence in reading order** (see Canonical Idiom Explanations below — later files cross-reference instead of repeating).
- No changes to code logic, formatting, imports, or the JSON asset. `git diff` after each task must show **only added comment lines** (plus blank lines around them).
- Commit message prefix for all tasks: `docs:`.
- Font size limits, page-index behavior, and other numbers mentioned in comments MUST be copied from the actual code (e.g., `ReaderViewModel`: 21f minimum, 37f maximum, step 2f).
- Project builds with `./gradlew assembleDebug`; unit tests run with `./gradlew testDebugUnitTest`. If a Gradle daemon is already warm, use it; do not add new dependencies.

## Review Focus

Failure modes this plan's verifications must catch:

1. **Comments that lie** (wrong numbers, wrong names, wrong "what happens on null"). Every number and behavior claim must be checked against the source file while writing the comment. Verification: task's read-first step + reviewer compares comment claims to code.
2. **Accidental code edits while commenting** (typos in code, deleted lines, reordering). Verification: `git diff` shows only comment-line additions; `./gradlew assembleDebug` + `./gradlew testDebugUnitTest` pass at the end of every task.
3. **Broken KDoc references** — `[SomeName]` links to a class that doesn't exist or is misspelled. Verification: reviewer greps each bracketed name against the codebase; build still passes (KDoc links are advisory, so only review catches this).
4. **Over-commenting noise** — comments on trivial one-liners (`val x = 0`), or repeating the same idiom explanation in five files. Verification: reviewer checks each file against the style guide below; each idiom is explained once, cross-referenced later.
5. **RTL/encoding accidents** — Arabic text or stray bidi control characters inside comments (copy-paste from `AppStrings.kt`). Verification: reviewer scans comments; comments are pure ASCII English. (Quoting an Arabic *value* in a comment is allowed only in `AppStrings.kt`'s file header, which quotes none.)

---

## Canonical Idiom Explanations (single source of truth)

When commenting a file, these idioms are explained **in the file listed in "First explained in"**. Later files that use the same idiom get a one-line cross-reference (`// Same StateFlow backing-property pattern as HomeViewModel`) instead of a full explanation. This keeps the codebase DRY even in comments.

| Idiom / concept | First explained in | Canonical wording anchor |
|---|---|---|
| `data class` + `copy()` | Task 2 (`Page.kt`) | "a Kotlin class that auto-generates equals/hashCode/toString/copy" |
| Nullable types `String?`, safe call `?.`, elvis `?:` | Task 2 (`Page.kt`) | "`?:` returns the left side if it is not null, otherwise the right side" |
| `@Serializable` + `@SerialName` (kotlinx.serialization) | Task 2 (`Chapter.kt`) | "marks the class for JSON (de)serialization; `@SerialName` maps a JSON key to this property" |
| Default parameter values | Task 2 (`Page.kt`) | "used when JSON omits the key" |
| `object` (singleton) | Task 4 (`AppModule.kt`) | "Kotlin `object` = a class with exactly one instance, created lazily on first access" |
| Manual DI + why `applicationContext` | Task 4 (`AppModule.kt`) | "app-level context survives config changes; an Activity context here would leak it" |
| `MutableStateFlow`/`StateFlow` backing-property pattern | Task 7 (`HomeViewModel.kt`) | "private writable + public read-only so the UI can observe but never set state directly" |
| `viewModelScope.launch` + coroutines + `try/catch` → error state | Task 7 (`HomeViewModel.kt`) | "coroutines: lightweight threads; `viewModelScope` is auto-cancelled when the ViewModel dies" |
| `ViewModelProvider.Factory` + `@Suppress("UNCHECKED_CAST")` | Task 7 (`HomeViewModel.kt`) | "the Android system recreates ViewModels by reflection; a Factory is how we inject constructor arguments" |
| `@Composable` functions + recomposition | Task 7 (`HomeScreen.kt`) | "a function that describes UI; Compose re-runs (recomposes) it whenever state it reads changes" |
| `collectAsState()` + `by` delegate | Task 7 (`HomeScreen.kt`) | "collects the flow into Compose state; `by` lets us write `uiState.foo` instead of `uiState.value.foo`" |
| State hoisting via lambda callbacks (`onChapterClick`) | Task 7 (`HomeScreen.kt`) | "child reports events up; parent decides navigation — keeps the composable reusable and testable" |
| `remember` / `rememberScrollState` | Task 7 (`HomeScreen.kt`) | "survives recomposition; without it the value would reset on every re-render" |
| `LaunchedEffect` | Task 9 (`ReaderScreen.kt`) | "runs a suspend block when it enters composition / keys change — the Compose side-effect bridge" |
| `HorizontalPager` + `key()` | Task 9 (`ReaderScreen.kt`) | "swipeable pages; `key(chapterId)` forces a fresh pager when the reader opens a different chapter" |
| `apply` / `also` / `let` scope functions | Task 4 (`AppModule.kt`) for `also`, Task 11 (`ShareUtil.kt`) for `apply`, Task 2 (`Page.kt`) file header for `let` usage note | short one-liners per scope function |
| `.use {}` (auto-close) | Task 3 (`JsonKhatiraRepository.kt`) | "closes the stream automatically, even if reading throws" |
| `object`-based util + Android `Intent` system | Task 11 (`ShareUtil.kt`) | "an Intent is Android's message system for asking another app to do something" |

---

## The Reading Order (what "A to Z" means)

Tasks are numbered in the order a junior should read the code — this is also execution order, because later comments cross-reference earlier ones:

```
1. KhatirApp / MainActivity        (how Android starts the app)
2. data/model/                     (what the content IS)
3. data/repository/                (how content is loaded)
4. di/AppModule                    (how pieces are wired together)
5. navigation/                     (how screens connect)
6. data/static/AppStrings          (where Arabic UI text lives)
7. ui/home/                        (first screen, MVVM in miniature)
8. ui/reader/ + components/        (the core reading experience)
9. ui/search/                      (diacritic-insensitive search)
10. ui/theme/                      (colors, fonts, typography)
11. util/                          (sharing, WhatsApp, exit dialog)
12. full verification pass
```

---

### Task 1: Branch + App entry points

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/KhatirApp.kt`
- Modify: `app/src/main/java/com/islamux/khatir/MainActivity.kt`

**Depends on:** nothing. **Produces:** branch `docs/junior-comments`; the terms "single-Activity architecture", "entry point", "edge-to-edge" established for later cross-references.

- [ ] **Step 1: Create the working branch (never work on main)**

```bash
git checkout main && git pull --ff-only && git checkout -b docs/junior-comments
```

- [ ] **Step 2: Read both files fully before commenting** (they are 5 and 27 lines)

- [ ] **Step 3: Add file-level and declaration comments to `KhatirApp.kt`**

```kotlin
/**
 * Custom Application class — the FIRST code the Android system runs for this app,
 * before any Activity exists.
 *
 * It is registered in app/src/main/AndroidManifest.xml via android:name=".KhatirApp".
 * We keep it empty because this project does not need app-startup logic:
 * dependency wiring lives in di/AppModule.kt instead.
 *
 * Kotlin note: `class KhatirApp : Application()` — the parentheses mean we call the
 * superclass constructor with no arguments. Kotlin has no separate "extends" keyword;
 * the colon IS inheritance.
 */
class KhatirApp : Application()
```

- [ ] **Step 4: Add comments to `MainActivity.kt`**

Add a file-level KDoc above the class:

```kotlin
/**
 * The app's only Activity (single-Activity architecture).
 *
 * Android starts here after KhatirApp. Everything the user sees afterwards is
 * Jetpack Compose UI swapped inside this one Activity by navigation/NavGraph.kt.
 *
 * Reading path from here:
 *   setContent -> KhatirTheme (ui/theme) -> NavGraph (navigation) -> HomeScreen (ui/home).
 */
```

Inline notes (place directly above the matching lines):

```kotlin
// Draws the app behind the system status/navigation bars (no colored strips).
enableEdgeToEdge()

// Replaces XML layouts: this lambda builds the whole UI in Kotlin.
setContent {
```

And above `val navController = rememberNavController()`:

```kotlin
// The navigation brain: knows the screen back stack. remember* = survive recomposition.
```

- [ ] **Step 5: Verify build passes**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Verify diff is comments-only, then commit**

```bash
git diff --stat   # only the two files above
git add app/src/main/java/com/islamux/khatir/KhatirApp.kt app/src/main/java/com/islamux/khatir/MainActivity.kt
git commit -m "docs: comment app entry points for junior onboarding"
```

---

### Task 2: Data models — `Page`, `Chapter`, `KhatiraContent`

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/data/model/Page.kt`
- Modify: `app/src/main/java/com/islamux/khatir/data/model/Chapter.kt`
- Modify: `app/src/main/java/com/islamux/khatir/data/model/KhatiraContent.kt`

**Depends on:** nothing. **Produces:** the canonical explanations of `data class`, nullables (`String?`, `?.`, `?:`), `@Serializable`/`@SerialName`, default parameter values, and the **critical `order`-driven rendering contract** that Tasks 8 and 9 cross-reference.

- [ ] **Step 1: Read all three files** (14 + 12 + 11 lines)

- [ ] **Step 2: Comment `Page.kt`**

```kotlin
/**
 * One page of a [Chapter]. This is the heart of the content model.
 *
 * A page deliberately does NOT store one big HTML/text blob. It stores each kind
 * of content in its own list, and [order] decides the sequence in which the
 * reader screen renders them (see ui/reader/components/PageContent.kt).
 *
 * Rendering contract: the UI walks [order] from first to last element. Every time
 * it meets "titles" it renders the NEXT unused element of [titles], then advances
 * an internal index; same for "subtitles", "texts", "ayahs". "footer" renders
 * [footer] once, if present. Consequence: the SAME lists with a different [order]
 * produce a visibly different page — never reorder or assume alphabetical order.
 *
 * Kotlin concepts in this file:
 *  - `data class`: the compiler auto-generates equals(), hashCode(), toString(),
 *    and copy() (copy builds a new object changing only the fields you name).
 *  - Default values (`= emptyList()`): used automatically when the JSON omits the key.
 *  - `String?` (nullable): [footer] may legitimately be absent in the JSON.
 *    Reading a nullable needs `?.` (safe call) or `?:` (elvis: left side if not
 *    null, otherwise right side).
 *
 * @property index 1-based position of the page inside its chapter.
 * @property order field names in exact rendering sequence. Values must be exactly
 * one of: "titles", "subtitles", "texts", "ayahs", "footer".
 */
@Serializable
data class Page(
```

Property notes on their own lines:

```kotlin
    /** Repeated elements may appear multiple times in [order] — each occurrence renders the next list element. */
    val titles: List<String> = emptyList(),
```

- [ ] **Step 3: Comment `Chapter.kt`**

```kotlin
/**
 * A themed group of [Page]s shown together in the reader.
 *
 * `@Serializable` (kotlinx.serialization) lets the JSON decoder in
 * JsonKhatiraRepository create instances of this class directly from the
 * khatira_content.json asset.
 */
```

Inline note for the mapped property:

```kotlin
    // The JSON key is "order_index" (snake_case); @SerialName maps it to this
    // camelCase Kotlin property so we follow Kotlin style while parsing the file as-is.
    @SerialName("order_index") val orderIndex: Int,
```

And one line above `val pages`: `/** Pages rendered in list order by the reader's pager. */`

- [ ] **Step 4: Comment `KhatiraContent.kt`**

File header: explain it is the decoded root of `assets/khatira_content.json` — the whole book in one object; one short KDoc suffices (`/** Root object of assets/khatira_content.json — the entire book. Decoded once and cached by [JsonKhatiraRepository]. */`). Keep it brief; do not pad.

- [ ] **Step 5: Build + commit**

Run: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/islamux/khatir/data/model/
git commit -m "docs: explain content data model and order-driven rendering contract"
```

---

### Task 3: Repository layer — interface, JSON implementation, ReaderUiState

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/data/repository/KhatiraRepository.kt`
- Modify: `app/src/main/java/com/islamux/khatir/data/repository/JsonKhatiraRepository.kt`
- Modify: `app/src/main/java/com/islamux/khatir/data/repository/ReaderUiState.kt`
- Modify: `app/src/test/java/com/islamux/khatir/data/repository/JsonKhatiraRepositoryTest.kt`

**Depends on:** Task 2 (models). **Produces:** canonical explanations of interfaces-as-abstraction, `.use {}`, in-memory caching, `ignoreUnknownKeys`, and the UiState pattern (cross-referenced by Tasks 7–9).

- [ ] **Step 1: Read all four files**

- [ ] **Step 2: Comment `KhatiraRepository.kt`**

```kotlin
/**
 * The data contract every content source must fulfill.
 *
 * Why an interface? ViewModels depend on this abstraction, not on the JSON
 * implementation — so unit tests can inject a fake repository (see
 * HomeViewModelErrorStateTest) and a future database/remote source could
 * replace JsonKhatiraRepository without touching any ViewModel.
 */
interface KhatiraRepository {
    /** Whole book. Suspend = runs off the main thread; may do IO. */
```

- [ ] **Step 3: Comment `JsonKhatiraRepository.kt`**

Class KDoc:

```kotlin
/**
 * [KhatiraRepository] implementation that reads assets/khatira_content.json once
 * and keeps the decoded [KhatiraContent] in memory for the rest of the app's life
 * (the asset never changes while the app runs, so caching is safe and fast).
 */
```

Inline notes at the exact lines:

```kotlin
    // `Json { ignoreUnknownKeys = true }`: if the JSON gains new keys later,
    // parsing still succeeds instead of crashing — forward compatibility.
    private val jsonDecoder = Json { ignoreUnknownKeys = true }
```

```kotlin
        // `?:`-free cache check: if cachedContent is non-null return it immediately.
        cachedContent?.let { return it }
```

```kotlin
        // `.use {}` closes the input stream automatically even if reading throws
        // (Java try-with-resources equivalent) — forget to close and we leak a file handle.
        val jsonString = context.assets
```

One line above `cachedContent = content`: `// Cache only AFTER a successful decode — a failed parse must retry next call.`

- [ ] **Step 4: Comment `ReaderUiState.kt`**

```kotlin
/**
 * Everything the reader screen needs to draw itself, in ONE immutable object.
 *
 * This is the "state" of MVVM: the ViewModel owns it and publishes new copies
 * (via StateFlow); the UI observes and renders. `copy()` builds each new version
 * changing only the fields that changed. Defaults = the loading state, which is
 * why [isLoading] starts true: no data yet, nothing has failed yet.
 */
```

Add `/** In sp units. Bounded 21f..37f by ReaderViewModel. */` above `fontSize`, and `/** Non-null only when loading failed; shown by the UI as an error view. */` above `error`.

- [ ] **Step 5: Comment `JsonKhatiraRepositoryTest.kt`** — brief teaching KDoc on the class (`/** JVM unit test (no emulator). Loads the REAL asset from the repo — the one place tests may touch real JSON, because the thing under test IS the JSON parsing. */`) plus one `// given / when / then` marker comment per test method.

- [ ] **Step 6: Build, run repository tests, commit**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest --tests "*JsonKhatiraRepositoryTest*"`
Expected: `BUILD SUCCESSFUL`, all tests pass.

```bash
git add app/src/main/java/com/islamux/khatir/data/repository/ app/src/test/java/com/islamux/khatir/data/repository/
git commit -m "docs: explain repository layer, caching, and UiState pattern"
```

---

### Task 4: Dependency injection — `AppModule`

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/di/AppModule.kt`

**Depends on:** Task 3. **Produces:** canonical explanations of `object` singletons, `also`, manual DI, `applicationContext`.

- [ ] **Step 1: Read the file** (32 lines)

- [ ] **Step 2: Add comments**

Class KDoc:

```kotlin
/**
 * Hand-written dependency-injection container (no Hilt/Dagger framework in this
 * project — deliberately, to keep the build simple and the magic at zero).
 *
 * Kotlin `object` = a class with exactly ONE instance, created lazily the first
 * time it is touched. Perfect for a service locator like this.
 *
 * Screens call these provide* functions when they create their ViewModel:
 *   viewModel(factory = AppModule.provideHomeViewModelFactory(LocalContext.current))
 */
```

Inline notes:

```kotlin
    // Nullable so it can start as "not created yet".
    private var repository: KhatiraRepository? = null
```

```kotlin
    // `?:` + `.also` = the classic lazy-singleton one-liner:
    // if repository exists use it, otherwise build one; `.also` stores the new
    // instance back into the field AND returns it. applicationContext (not the
    // Activity context) so the repository never leaks a destroyed screen.
    fun provideRepository(context: Context): KhatiraRepository =
```

One-liners above the three factory providers, e.g.:
`// Each screen needs a different Factory because each ViewModel takes different constructor arguments (ReaderViewModel also needs chapterId).`

- [ ] **Step 3: Build + commit**

Run: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/islamux/khatir/di/AppModule.kt
git commit -m "docs: explain manual DI, object singleton, and applicationContext"
```

---

### Task 5: Navigation — `Routes`, `NavGraph`

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/navigation/Routes.kt`
- Modify: `app/src/main/java/com/islamux/khatir/navigation/NavGraph.kt`

**Depends on:** Task 1 (MainActivity cross-reference). **Produces:** route-pattern and argument-extraction explanations used by Tasks 7–9 comments.

- [ ] **Step 1: Read both files**

- [ ] **Step 2: Comment `Routes.kt`**

```kotlin
/**
 * All navigation destinations as constants — one place to rename a screen.
 *
 * READER shows the pattern for routes WITH arguments:
 *   "reader/{chapterId}?initialPage={initialPage}"
 * {chapterId} is a mandatory path segment; ?initialPage= is an optional
 * query parameter with a default (see NavGraph's defaultValue = 0).
 */
```

```kotlin
    /** Builds a real navigable URL by filling the placeholders, e.g. "reader/pre?initialPage=3". */
    fun readerRoute(chapterId: String, initialPage: Int = 0) =
```

- [ ] **Step 3: Comment `NavGraph.kt`**

Above `fun NavGraph(...)`:

```kotlin
/**
 * Declares every screen and wires screen-to-screen events (the lambdas passed to
 * each screen) to navController.navigate(...). Screens themselves never hold the
 * navController — they only call `onChapterClick`/`onBackClick`; NavGraph decides
 * what that means. This "hoisted navigation" keeps screens reusable.
 */
```

Inline notes:

```kotlin
        // popBackStack() = the system Back button: drop the current screen, return to the previous one.
```

```kotlin
        // Extract the arguments the route promised. `?: return@composable` = bail out
        // if chapterId is missing (corrupt deep link) rather than crash on null.
```

- [ ] **Step 4: Build + commit**

Run: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/islamux/khatir/navigation/
git commit -m "docs: explain navigation routes and hoisted navigation callbacks"
```

---

### Task 6: Static strings — `AppStrings`

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/data/static/AppStrings.kt`

**Depends on:** nothing. **Produces:** the "Arabic-first, strings live here not in composables" rule referenced by Tasks 7–11.

- [ ] **Step 1: Read the file** (82 lines, Arabic values — comments stay English, do NOT quote Arabic in comments)

- [ ] **Step 2: Add comments**

```kotlin
/**
 * Every user-visible Arabic string in the app, as compile-time constants.
 *
 * Project rule: composables never hardcode UI text — they reference
 * AppStrings.something. This keeps wording in one reviewable place and ready
 * for future localization. const val = value copied into callers at compile
 * time (no object lookup at runtime).
 *
 * Grouped by screen/purpose: home*, search*, field*, alert*, drawer*, plus
 * generic labels. chapterTitle(id) maps a chapter id to its display title.
 */
```

Above `chapterTitle`:

```kotlin
    /** Maps a chapter id from the JSON ("pre", ...) to its Arabic display title; empty string for unknown ids. */
```

- [ ] **Step 3: Build + commit**

Run: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/islamux/khatir/data/static/AppStrings.kt
git commit -m "docs: document Arabic string centralization policy"
```

---

### Task 7: Home feature — ViewModel, Screen, and its test

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/home/HomeScreen.kt` (304 lines — biggest UI file, comment the skeleton: state collection, drawer, chapter list composable; do NOT comment every `Text`)
- Modify: `app/src/test/java/com/islamux/khatir/ui/home/HomeViewModelErrorStateTest.kt`

**Depends on:** Tasks 3, 4, 6. **Produces:** THE canonical MVVM/Compose explanations (StateFlow backing property, viewModelScope, Factory, `@Composable`, `collectAsState` + `by`, hoisting, `remember`) — Tasks 8–9 cross-reference these instead of re-explaining.

- [ ] **Step 1: Read all three files fully**

- [ ] **Step 2: Comment `HomeViewModel.kt`**

Above `data class HomeUiState`: `/** What HomeScreen draws: the chapter list plus loading/error flags. Same shape idea as ReaderUiState. */`

Above `class HomeViewModel`:

```kotlin
/**
 * HomeScreen's brain (the "VM" of MVVM). Fetches chapters from the repository
 * and exposes them as observable state. Contains ZERO UI code.
 *
 * Canonical patterns explained here (other ViewModels follow the same shape):
 *  1. Backing property: [_uiState] is private+mutable; [uiState] is public+read-only.
 *     The UI can observe but can never set state directly — one-way data flow.
 *  2. viewModelScope: a coroutine scope that dies with the ViewModel, so a
 *     screen leaving cancels its loading automatically (no leaks).
 *  3. init { loadChapters() }: start loading the moment the ViewModel is created.
 */
```

Inside `loadChapters`, above `viewModelScope.launch`:

```kotlin
        // Coroutines look sequential but run off the main thread; the suspend
        // repository call may do IO. Everything inside try/catch so ANY failure
        // becomes error state instead of a crash. `e.message ?: fallback`:
        // message is nullable, elvis swaps null for a generic Arabic message.
```

Above `class Factory`:

```kotlin
    /**
     * Android recreates ViewModels itself and needs a no-argument constructor —
     * ours takes a repository. A Factory bridges that: it is the recipe the
     * system calls to BUILD our ViewModel with its dependencies.
     * @Suppress("UNCHECKED_CAST") acknowledges the generic cast we perform is safe here.
     */
```

- [ ] **Step 3: Comment `HomeScreen.kt`**

Above `fun HomeScreen(...)`:

```kotlin
/**
 * The chapter list screen — the app's home.
 *
 * Canonical Compose patterns explained here:
 *  - @Composable: a function that DESCRIBES UI. Compose re-executes it
 *    (recomposition) whenever state it reads changes — so we read state and
 *    emit UI, never mutate views directly.
 *  - `val uiState by viewModel.uiState.collectAsState()`: subscribes to the
 *    ViewModel's flow; `by` delegates so we write uiState.chapters (not .value).
 *    Every emission triggers recomposition of whatever reads uiState.
 *  - State hoisting: this screen receives onChapterClick/onSearchClick lambdas
 *    from NavGraph instead of navigating itself (see NavGraph.kt).
 *  - viewModel(factory = AppModule.provideHomeViewModelFactory(...)): manual DI
 *    hookup (no Hilt). LocalContext.current = the surrounding Android Context.
 */
```

Inline notes at key lines inside the body:

```kotlin
    // rememberCoroutineScope: gives coroutines a scope tied to this composable's
    // lifetime — used to open/close the drawer (suspend calls).
```

```kotlin
    // rememberDrawerState persists drawer open/closed across recompositions.
```

On the chapter row / list composable (whatever the inner composable rendering one `Chapter` is called after reading the file): add a short KDoc `/** One tappable chapter card in the list. `clickable` makes ANY composable tappable. */`. For the loading/error branches add one-liners: `// UI has three states: loading spinner, error message, chapter list — driven entirely by uiState flags.` Do not comment individual Text/Spacer lines.

- [ ] **Step 4: Comment `HomeViewModelErrorStateTest.kt`** — class KDoc: `/** Tests the failure path with a FAKE repository (MockK) — proving the interface abstraction pays off: no JSON, no Android, runs on the JVM in milliseconds. Dispatchers.setMain swaps the Android main dispatcher for a test one. */`

- [ ] **Step 5: Build, run home tests, commit**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest --tests "*HomeViewModelErrorStateTest*"`
Expected: `BUILD SUCCESSFUL`, tests pass.

```bash
git add app/src/main/java/com/islamux/khatir/ui/home/ app/src/test/java/com/islamux/khatir/ui/home/
git commit -m "docs: teach MVVM and core Compose patterns via Home feature"
```

---

### Task 8: Reader feature — ViewModel, Screen, PageContent, ShinyBlackThumb

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/ui/reader/ReaderViewModel.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/reader/ReaderScreen.kt` (275 lines)
- Modify: `app/src/main/java/com/islamux/khatir/ui/reader/components/PageContent.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/reader/components/ShinyBlackThumb.kt`
- Modify: `app/src/test/java/com/islamux/khatir/ui/reader/ReaderViewModelTest.kt`

**Depends on:** Tasks 2, 7. **Produces:** `LaunchedEffect`, `HorizontalPager`, `key()`, per-element index counters in `PageContent`, `buildShareText` order walkthrough.

- [ ] **Step 1: Read all five files fully**

- [ ] **Step 2: Comment `ReaderViewModel.kt`**

Class KDoc: same shape as HomeViewModel — say so and cross-reference: `/** ReaderScreen's brain. Follows the exact MVVM patterns documented in HomeViewModel (backing property, viewModelScope, Factory) — read that file first. Unique here: page navigation, font-size bounds, and share-text building. */`

Inline notes:

```kotlin
    // `if (index in pages.indices)` guards the bounds — swiping past the last
    // page or a bad deep-link page number is silently ignored instead of crashing.
    fun navigateToPage(index: Int) {
```

```kotlin
    // Font limits: 21f..37f, step 2f. Bounded HERE so the UI can render A+/A-
    // buttons without re-checking; hitting a limit simply does nothing.
```

Above `buildShareText`:

```kotlin
    /**
     * Flattens the CURRENT page into plain shareable text.
     * Walks [Page.order] (the rendering contract documented on Page) so the
     * shared text has the same piece order as the on-screen page — one source
     * of truth for ordering. `footer?.let` adds the footer only when present;
     * "\n\n" joins pieces with a blank line between them.
     */
```

- [ ] **Step 3: Comment `ReaderScreen.kt`**

Above `fun ReaderScreen(...)`:

```kotlin
/**
 * The reading screen: one chapter, swipeable pages, font controls, share.
 * MVVM identical to HomeScreen (read its comments first). New concepts here:
 *  - HorizontalPager: built-in swipeable page container driven by pagerState.
 *  - key(chapterId): discards and rebuilds pager state when the reader is
 *    reopened with a DIFFERENT chapter (navigating reader/A -> reader/B reuses
 *    this composable; without key() the old chapter's state would linger).
 *  - viewModel(key = chapterId): separate ViewModel instance per chapter.
 */
```

Inline notes:

```kotlin
    // initialPage.coerceIn(...): clamps the deep-link page number into the valid
    // range; coerceAtLeast(0) handles the empty-pages edge case before data loads.
```

Above every `LaunchedEffect(...)` found in the file:

```kotlin
    // LaunchedEffect: runs its suspend body when it ENTERS composition (or when
    // its key changes) and CANCELS it when it leaves — Compose's bridge for
    // one-shot side effects like "scroll the pager to page N now".
```

Brief one-liners on the font A+/A− buttons and the share button (name the ViewModel methods they call).

- [ ] **Step 4: Comment `PageContent.kt`**

Above `fun PageContent(...)`:

```kotlin
/**
 * Renders ONE [Page] by executing its [Page.order] contract (see Page.kt docs):
 * walk order top-to-bottom, and for each field name render the NEXT unused
 * element from the matching list. That is why we keep the four `*Index` counters.
 *
 * The counters use `by remember { mutableIntStateOf(0) }`:
 *  - remember: value survives recomposition (e.g., font-size change re-renders
 *    this function; without remember every counter would reset to 0 mid-render
 *    and the page would render titles over and over).
 *  - `var x by ...`: property delegate sugar for x = x + 1 instead of x.value++.
 *
 * Each content type gets its own style: titles/subtitles/ayahs centered, texts
 * justified with the Amiri font, footer smaller — offsets (fontSize + 4 / + 2 /
 * - 4) keep the visual hierarchy at ANY user font size.
 */
```

Inline note above `"texts" ->` branch: `// Justify + 1.6em line height: long Arabic paragraphs stay readable.`

- [ ] **Step 5: Comment `ShinyBlackThumb.kt`** — short class/func KDoc: `/** Custom Slider thumb (the draggable circle) styled glossy black to match the app's design system. Purely visual — the slider logic stays in Material's Slider. */` plus one-liners on any `drawBehind`/canvas modifiers found while reading.

- [ ] **Step 6: Comment `ReaderViewModelTest.kt`** — same teaching style as Task 7's test: fake repository, `StandardTestDispatcher`, one `// given / when / then` marker per test.

- [ ] **Step 7: Build, run reader tests, commit**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest --tests "*ReaderViewModelTest*"`
Expected: `BUILD SUCCESSFUL`, tests pass.

```bash
git add app/src/main/java/com/islamux/khatir/ui/reader/ app/src/test/java/com/islamux/khatir/ui/reader/
git commit -m "docs: document reader feature, pager, LaunchedEffect, order rendering"
```

---

### Task 9: Search feature — ViewModel, Screen, test

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/ui/search/SearchViewModel.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/search/SearchScreen.kt`
- Modify: `app/src/test/java/com/islamux/khatir/ui/search/SearchViewModelTest.kt`

**Depends on:** Tasks 2, 7. **Produces:** diacritic-insensitive search explanation, `fieldLabel` mapping, first-result-per-field rule.

- [ ] **Step 1: Read all three files fully**

- [ ] **Step 2: Comment `SearchViewModel.kt`**

Above `data class SearchResult`: `/** One hit: WHERE it was found (chapter, page, field, text snippet) so the UI can offer a "jump to page" action. */`

Above `fun search(query: String)`:

```kotlin
    /**
     * Full-content search, diacritic-insensitive:
     *  1. Blank query -> clear results, done.
     *  2. Normalize the query (lowercase + removeSearchDiacritics) ONCE.
     *  3. Walk every chapter -> every page -> every field in page.order
     *     (again the Page.order contract) and keep the FIRST matching value
     *     per field (`firstOrNull`) — one hit per page-section, not five
     *     duplicates when several lines match.
     *  4. Both sides are normalized identically, so "كتاب" matches "كِتَاب".
     */
```

Note above `private var cachedContent`: `// Search keeps its own handle to the whole book (already cached inside the repository anyway) so queries never re-read the asset.`

`fieldLabel`: one-liner mapping field names to the Arabic labels from AppStrings for display.

- [ ] **Step 3: Comment `SearchScreen.kt`** — same conventions as HomeScreen (Task 7): KDoc on the main composable referencing HomeScreen's canonical explanations; one-liners on query input, results list, and the tap-through that navigates via `Routes.readerRoute(chapterId, pageIndex)`. Note anywhere `onChapterClick` carries the page index if the code shows it.

- [ ] **Step 4: Comment `SearchViewModelTest.kt`** — same as prior test tasks; highlight any test that proves diacritic insensitivity with a `// THE core promise: harakat in content must not break search` one-liner.

- [ ] **Step 5: Build, run search tests, commit**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest --tests "*SearchViewModelTest*"`
Expected: `BUILD SUCCESSFUL`, tests pass.

```bash
git add app/src/main/java/com/islamux/khatir/ui/search/ app/src/test/java/com/islamux/khatir/ui/search/
git commit -m "docs: explain diacritic-insensitive search pipeline"
```

---

### Task 10: Theme — Color, Fonts, Type, Theme

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/theme/Fonts.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/islamux/khatir/ui/theme/Theme.kt`

**Depends on:** nothing. **Produces:** design-system vocabulary (`AppColors`, `AmiriFontFamily`, `ContentStyles`, `KhatirTheme`) referenced by Tasks 7–9 comments.

- [ ] **Step 1: Read all four files**

- [ ] **Step 2: Comment each** — file-level KDoc per file, short and factual:

`Color.kt`: `/** The app palette as a single object — one place to change any color. Hex literals are #AARRGGBB (alpha first). */`

`Fonts.kt`: `/** Loads the bundled Amiri font from res/font and exposes it as a FontFamily composables pass to Text. */`

`Type.kt`: `/** Named text styles (ContentStyles.title/subtitle/ayahHadith/footer...) that PageContent applies per content type — the single source of the visual hierarchy. */`

`Theme.kt`: `/** Root theme wrapper: colors + typography applied once in MainActivity; every screen underneath inherits it. Notes: RTL is on (Arabic-first app); dark-color scheme values are fixed by design, not derived. */`

Add property one-liners only where a color/style name is not self-explanatory.

- [ ] **Step 3: Build + commit**

Run: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/islamux/khatir/ui/theme/
git commit -m "docs: document theme and design-system files"
```

---

### Task 11: Utilities — Share, WhatsApp, diacritics, exit dialog

**Files:**
- Modify: `app/src/main/java/com/islamux/khatir/util/ShareUtil.kt`
- Modify: `app/src/main/java/com/islamux/khatir/util/WhatsAppUtil.kt`
- Modify: `app/src/main/java/com/islamux/khatir/util/RemoveSearchDiacritics.kt`
- Modify: `app/src/main/java/com/islamux/khatir/util/AlertExitDialog.kt`

**Depends on:** Task 9 (search cross-reference). **Produces:** Intent system, `apply`, graceful fallback, the Unicode range trick, BackPress interception.

- [ ] **Step 1: Read all four files** (AlertExitDialog is 105 lines — read fully, comment the composable and its BackHandler)

- [ ] **Step 2: Comment `ShareUtil.kt`**

```kotlin
/**
 * Shares plain text with ANY app the user picks (WhatsApp, Telegram, SMS...).
 * An Intent is Android's message system for asking another app to do something;
 * createChooser() shows the picker sheet. `apply { }` configures the new Intent
 * and returns it in one expression (scope function: "configure this, then give it back").
 */
```

- [ ] **Step 3: Comment `WhatsAppUtil.kt`**

```kotlin
/**
 * Opens a WhatsApp chat with the hardcoded contact number; if WhatsApp is not
 * installed, ActivityNotFoundException is caught and the Play Store page opens
 * instead — graceful degradation, never a crash.
 */
```

One-liner above the `whatsapp://send?phone=` URI: `// Custom app scheme: Android routes it to WhatsApp if installed.`

- [ ] **Step 4: Comment `RemoveSearchDiacritics.kt`** — the smallest and cleverest file:

```kotlin
/**
 * Makes search diacritic-insensitive by keeping ONLY Arabic letters in the
 * '\u0621'..'\u064A' block (hamza through yaa) plus whitespace. Harakat
 * (fatha/kasra/damma...) live in a different Unicode block, so they — and any
 * punctuation or symbols — simply fall away. Used on BOTH the query and the
 * content before comparing (see SearchViewModel.search).
 */
```

- [ ] **Step 5: Comment `AlertExitDialog.kt`** — KDoc on the dialog composable and the back-press handler composable it exposes (`BackPressHandlerWithExitDialog`, referenced by HomeScreen): explain `BackHandler` intercepts the system Back key, shows the confirm dialog instead of exiting immediately; state-driven dialog visibility.

- [ ] **Step 6: Build + full unit-test run + commit**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all tests pass.

```bash
git add app/src/main/java/com/islamux/khatir/util/
git commit -m "docs: explain sharing intents, WhatsApp fallback, diacritic filter, exit dialog"
```

---

### Task 12: Final verification pass

**Files:**
- No modifications planned. Fix-ups only if a check below fails.

**Depends on:** all previous tasks.

- [ ] **Step 1: Whole-project build + tests**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 2: Diff hygiene check**

Run: `git diff main --stat`
Expected: only the 24 files listed across Tasks 1–11 (20 main-source + 4 test files), nothing else.

Run: `git diff main | grep -E '^-[^-]' | head -50`
Expected: empty or trivial — comment-only changes should not REMOVE code lines (the grep excludes `---` file-header lines).

- [ ] **Step 3: Spot-check comment accuracy**

For each of these claims-vs-code pairs, open the file and confirm the comment matches:
- `ReaderViewModel`: comment says 21f/37f/step 2f — matches `increaseFontSize`/`decreaseFontSize`.
- `Page.order` docs: valid values list matches the `when` branches in `PageContent.kt` AND `ReaderViewModel.buildShareText` AND `SearchViewModel.search`.
- `AppModule`: comment names the three factory providers that actually exist.
- `KhatiraRepository`: `getContent`/`getChapter`/`getAllChapters` signatures quoted in comments match the interface.

- [ ] **Step 4: Commit any fix-ups**

```bash
git add -A && git commit -m "docs: fix comment inaccuracies found in verification pass"
```

(Skip this step if Step 3 found nothing to fix.)

---

## Self-Review (completed)

- **Spec coverage:** every `.kt` file under `app/src` (20 main + 4 test files = 24) is listed in exactly one task; JSON asset and build files intentionally untouched (comments are for Kotlin learning; JSON already documented via `Page.order` docs). A-to-Z reading order is the task order.
- **Placeholders:** none — every step carries the actual comment text or an exact rule for producing it.
- **Name consistency:** all class/function/property names (`Page.order`, `ReaderUiState.fontSize`, `provideReaderViewModelFactory`, `BackPressHandlerWithExitDialog`, `Routes.readerRoute`, `AppStrings.unknownError`, `AmiriFontFamily`, `ContentStyles`) verified against the files read on 2026-09-26.
- **Review Focus:** each failure mode has a verification step (build/test per task, diff hygiene in Task 12, accuracy spot-check in Task 12 Step 3).
