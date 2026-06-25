# Flutter to Jetpack Compose Cheat Sheet

A mapping guide for developers coming from Flutter to this project's Jetpack Compose codebase.

## Core UI Building Blocks

| Flutter | Jetpack Compose | Salam Example |
|---------|----------------|---------------|
| `StatelessWidget` | `@Composable fun` | `HomeScreen(...)` |
| `MaterialApp` | `MaterialTheme { ... }` | `SalamTheme { ... }` in `MainActivity.kt` |
| `Scaffold` | `Scaffold { ... }` | Every screen wraps with `Scaffold(topBar = { ... })` |
| `PageView(reverse: true)` | `HorizontalPager(reverseLayout = true)` | ReaderScreen's page swiper |
| `Text('...')` | `Text("...")` | Used throughout |
| `Column` / `Row` | `Column` / `Row` | Used throughout |
| `ListView.builder` | `LazyColumn` | HomeScreen chapter list |
| `Image.asset` | `Image(painter = painterResource(...))` | Background image in reader |
| `IconButton` | `IconButton(onClick = { ... })` | Share, font controls, back |
| `AlertDialog` | `AlertDialog(onDismissRequest = { ... })` | Exit confirmation dialog |
| `Navigator.pushNamed` | `navController.navigate(...)` | `navController.navigate(Routes.readerRoute(chapterId))` |
| `Padding` / `EdgeInsets` | `Modifier.padding(...)` | `Modifier.padding(16.dp)` |

## State Management

| Flutter | Jetpack Compose | In Salamm |
|---------|----------------|-----------|
| `Cubit` / `Bloc` | `ViewModel` + `StateFlow` | `HomeViewModel`, `ReaderViewModel`, `SearchViewModel` |
| `BlocBuilder` | `collectAsState()` | `val uiState by viewModel.uiState.collectAsState()` |
| `context.watch()` | `collectAsState()` | Same pattern |
| `context.read<Cubit>()` | ViewModel from factory | `viewModel()`, provided by `AppModule` |
| `emit(state)` | `_uiState.value = newState` | `_uiState.value = HomeUiState(chapters = chapters)` |
| `state.copyWith(...)` | `.copy(...)` | `_uiState.value.copy(fontSize = current + 2f)` |

## Real Salam Examples

### Flutter: Screen with Scaffold

```dart
class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('الخواطر')),
      body: ListView.builder(
        itemCount: chapters.length,
        itemBuilder: (context, index) => ListTile(
          title: Text(chapters[index].title),
          onTap: () => Navigator.pushNamed(context, '/reader/${chapters[index].id}'),
        ),
      ),
    );
  }
}
```

### Compose equivalent (HomeScreen)

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel, onChapterClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(AppStrings.homeAppBarTitle) }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.chapters) { chapter ->
                ListItem(
                    headlineContent = { Text(chapter.title) },
                    modifier = Modifier.clickable { onChapterClick(chapter.id) }
                )
            }
        }
    }
}
```

### Flutter: PageView and BlocBuilder

```dart
PageView(
  reverse: true,
  controller: pageController,
  children: pages.map((page) => ReaderPageView(page: page)).toList(),
)
```

### Compose equivalent (ReaderScreen)

```kotlin
HorizontalPager(
    state = pagerState,
    reverseLayout = true
) { index ->
    ReaderPageView(
        page = uiState.pages[index],
        pageIndex = index,
        fontSize = uiState.fontSize
    )
}
```

### Flutter: State with Cubit

```dart
class ReaderCubit extends Cubit<ReaderState> {
  void increaseFont() => emit(state.copyWith(fontSize: state.fontSize + 2));
  void decreaseFont() => emit(state.copyWith(fontSize: state.fontSize - 2));
}
```

### Compose equivalent (ReaderViewModel)

```kotlin
class ReaderViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState

    fun increaseFontSize() {
        val current = _uiState.value.fontSize
        if (current < 37f) _uiState.value = _uiState.value.copy(fontSize = current + 2f)
    }
}
```

## Project-Specific Differences

| Area | Flutter (original salam) | Kotlin (salamkotlin) |
|------|------------------------|---------------------|
| Data source | 34 generated Dart list files | Single `khatira_content.json` (850KB) |
| State | `BasePageCubit` | `ReaderViewModel` |
| DI | `RepositoryProvider` in `main.dart` | `AppModule` in `di/` |
| Routing | Named routes per chapter (35 routes) | Single `/reader/{chapterId}?initialPage={page}` route |
| Background | Image per screen | Single `bg_reader.jpg` |
| Font | Amiri via Google Fonts | Bundled Amiri in `res/font/` |
| Search | Case-insensitive only | Diacritic-insensitive (ignores tashkeel) |

## Key Mindset Shifts

### Modifiers instead of Widget wrappers

In Flutter, you nest widgets for styling:

```dart
Padding(
  padding: EdgeInsets.all(16),
  child: Container(color: Colors.amber, child: Text("Hello"))
)
```

In Compose, you chain Modifiers on the same element:

```kotlin
Text(
    "Hello",
    modifier = Modifier.padding(16.dp).background(amberColor)
)
```

> **Order matters!** `Modifier.padding(16.dp).background(amber)` ≠ `Modifier.background(amber).padding(16.dp)` — the first adds padding around the background, the second backgrounds the padded area.

### State is not mutable class members

In Flutter you can write `setState(() => _count++)`. In Compose, state is hoisted into `ViewModel` and exposed as `StateFlow`:

```
Flutter:  StatefulWidget with setState()
Compose:  ViewModel with MutableStateFlow → collectAsState() in UI
```

### No `build()` method

In Flutter, every widget has a `build(BuildContext context)` method. In Compose, you write `@Composable` functions — they're just Kotlin functions, not class methods.

```dart
class MyWidget extends StatelessWidget {
  Widget build(BuildContext context) { return Text("Hi"); }
}
```

```kotlin
@Composable
fun MyWidget() {
    Text("Hi")
}
```

### `initState` → `init` block

Flutter's `initState()` runs when the widget enters the tree. In Compose, initialization goes in the `ViewModel`'s `init` block or a `LaunchedEffect`.

```kotlin
class HomeViewModel(private val repository: KhatiraRepository) : ViewModel() {
    init {
        loadChapters()  // runs when ViewModel is created
    }
}
```
