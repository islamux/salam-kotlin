# Navigation and Dependency Injection

## Single-Activity Architecture

Salam uses a **single-Activity** architecture:

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                SalamTheme {
                    NavGraph(navController = rememberNavController())
                }
            }
        }
    }
}
```

Every "screen" is a different Composable shown by the `NavHost`. This is modern Android best practice.

---

## Navigation: Routes

**File**: `navigation/Routes.kt`

```kotlin
object Routes {
    const val HOME = "home"
    const val READER = "reader/{chapterId}?initialPage={initialPage}"
    const val SEARCH = "search"

    fun readerRoute(chapterId: String, initialPage: Int = 0) =
        "reader/$chapterId?initialPage=$initialPage"
}
```

Route constants prevent typos — you write `Routes.HOME` instead of `"home"`.

The `READER` route has:
- **Path parameter**: `{chapterId}` — which chapter to load (`"pre"`, `"1"`, ..., `"final"`)
- **Query parameter**: `{initialPage}` — which page to start on (optional, defaults to 0)

### NavHost

**File**: `navigation/NavGraph.kt`

```kotlin
NavHost(navController = navController, startDestination = Routes.HOME) {
    composable(Routes.HOME) {
        HomeScreen(
            onChapterClick = { chapterId ->
                navController.navigate(Routes.readerRoute(chapterId))
            },
            onSearchClick = { navController.navigate(Routes.SEARCH) }
        )
    }

    composable(
        route = Routes.READER,
        arguments = listOf(
            navArgument("chapterId") { type = NavType.StringType },
            navArgument("initialPage") { type = NavType.IntType; defaultValue = 0 }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
        val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
        ReaderScreen(
            chapterId = chapterId,
            initialPage = initialPage,
            onBackClick = { navController.popBackStack() },
            onSearchClick = { navController.navigate(Routes.SEARCH) }
        )
    }

    composable(Routes.SEARCH) {
        SearchScreen(
            onChapterClick = { chapterId ->
                navController.navigate(Routes.readerRoute(chapterId))
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
```

**Navigation flow:**
```
Home ──chapterClick──► Reader/{chapterId}?initialPage=0
  │                        │
  │                        └── onBackClick ──► popBackStack()
  │
  └──searchClick──► Search
                        │
                        └── chapterClick ──► Reader/{chapterId}
```

---

## Dependency Injection: AppModule

**File**: `di/AppModule.kt`

### What Is Dependency Injection?

"DI" is a fancy term for: instead of creating objects inside the classes that need them, create them in one central place and pass them in.

Without DI:
```kotlin
// BAD: Screen creates its own dependencies
class HomeScreen {
    val repo = JsonKhatiraRepository(context)
    val vm = HomeViewModel(repo)
}
```

With DI:
```kotlin
// GOOD: Screen receives its dependencies
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppModule.provideHomeViewModelFactory(context))
)
```

### AppModule — The Central Factory

```kotlin
object AppModule {
    private var repository: KhatiraRepository? = null

    fun provideRepository(context: Context): KhatiraRepository {
        if (repository == null) {
            repository = JsonKhatiraRepository(context.applicationContext)
        }
        return repository!!
    }

    fun provideHomeViewModelFactory(context: Context): ViewModelProvider.Factory {
        return HomeViewModel.Factory(provideRepository(context))
    }

    fun provideReaderViewModelFactory(
        context: Context,
        chapterId: String
    ): ViewModelProvider.Factory {
        return ReaderViewModel.Factory(provideRepository(context), chapterId)
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return SearchViewModel.Factory(provideRepository(context))
    }
}
```

### Singleton vs. Fresh Instance

| Strategy | Providers | Why? |
|----------|-----------|------|
| **Singleton** (cached) | `JsonKhatiraRepository` | Only one JSON file — cache it once |
| **Fresh factory** | `HomeViewModel`, `ReaderViewModel`, `SearchViewModel` | Each screen has its own state |

### How NavGraph Uses AppModule

```kotlin
composable(Routes.HOME) {
    HomeScreen(
        viewModel = viewModel(
            factory = AppModule.provideHomeViewModelFactory(LocalContext.current)
        ),
        onChapterClick = { ... },
        onSearchClick = { ... }
    )
}

composable(Routes.READER) {
    ReaderScreen(
        chapterId = chapterId,
        initialPage = initialPage,
        viewModel = viewModel(
            key = chapterId,  // ← unique key per chapter
            factory = AppModule.provideReaderViewModelFactory(LocalContext.current, chapterId)
        ),
        ...
    )
}
```

The `key = chapterId` in ReaderScreen ensures that navigating between different chapters creates separate ViewModel instances. Without this, `viewModel()` would return the same instance for all chapters.

### Why Manual DI (No Hilt)?

1. **Simple** — no annotations, no generated code
2. **Small project** — only 3 screens, 1 repository
3. **Transparent** — you can see exactly where every dependency comes from

### How It All Connects

```
MainActivity.kt
    │
    ▼
SalamTheme  ← AppColors.kt, Type.kt, Theme.kt
    │
    ▼
NavGraph  ← Routes.kt, AppModule.kt
    │
    ├── composable(HOME) → HomeScreen(viewModel from AppModule)
    │                        → onChapterClick → readerRoute(chapterId)
    │                        → onSearchClick → SEARCH
    │
    ├── composable(READER/{id}?page=) → ReaderScreen(viewModel from AppModule, key = chapterId)
    │                                    → onBackClick → popBackStack()
    │
    └── composable(SEARCH) → SearchScreen(viewModel from AppModule)
                              → onChapterClick → readerRoute(chapterId)
```

## How to Add a New Screen

1. Add a route constant in `Routes.kt`
2. Create the screen Composable and ViewModel
3. Wire the ViewModel factory in `AppModule.kt`
4. Add a `composable()` entry in `NavGraph.kt`
5. Add navigation from the trigger point (e.g., a button in an existing screen)
