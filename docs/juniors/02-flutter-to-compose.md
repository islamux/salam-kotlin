# Flutter to Jetpack Compose Cheat Sheet

> This document is **identical** to the Athkarix project's `02-flutter-to-compose.md` — both apps are native Kotlin ports of Flutter apps and use the same Compose concepts.
>
> Read the full version at:
> **[`../../../../Athkarix-android/docs/juniros/02-flutter-to-compose.md`](../../../../Athkarix-android/docs/juniros/02-flutter-to-compose.md)**
>
> Below is the Salam-specific mapping table with examples from this codebase.

## Core UI Building Blocks

| Flutter | Jetpack Compose | Salam Example |
|---------|----------------|---------------|
| `StatelessWidget` | `@Composable fun` | `HomeScreen(...)` |
| `MaterialApp` | `MaterialTheme { ... }` | `SalamTheme { ... }` in `MainActivity.kt` |
| `Scaffold` | `Scaffold { ... }` | `Scaffold(topBar = { ... })` in every screen |
| `PageView(reverse: true)` | `HorizontalPager(reverseLayout = true)` | ReaderScreen's pager |
| `Text('...')` | `Text("...")` | Used throughout |
| `Column` / `Row` | `Column` / `Row` | Used throughout |
| `ListView.builder` | `LazyColumn` | HomeScreen's chapter list |
| `Navigator.pushNamed` | `navController.navigate(...)` | `navController.navigate(Routes.readerRoute(chapterId))` |
| `Padding` / `EdgeInsets` | `Modifier.padding(...)` | `Modifier.padding(16.dp)` |

## State Management Comparison

| Flutter | Jetpack Compose | In Salam |
|---------|----------------|----------|
| `Cubit` / `Bloc` | `ViewModel` + `StateFlow` | `HomeViewModel`, `ReaderViewModel`, `SearchViewModel` |
| `BlocBuilder` | `collectAsState()` | `val uiState by viewModel.uiState.collectAsState()` |
| `context.watch()` | `collectAsState()` | Same pattern |
| `context.read<Cubit>()` | ViewModel injected via AppModule | `AppModule.provideHomeViewModelFactory(context)` |
| `emit(state)` | `_uiState.value = newState` | `_uiState.value = HomeUiState(chapters = chapters)` |

## Salam-Specific Differences from Flutter

| Feature | Flutter (salam) | Kotlin (salamkotlin) |
|---------|----------------|----------------------|
| Data source | 34 generated Dart list files | Single `khatira_content.json` (825KB) |
| State | `BasePageCubit` | `ReaderViewModel` |
| DI | `RepositoryProvider` in `main.dart` | `AppModule` in `di/` |
| Routing | Named routes per chapter (35 routes) | Single `/reader/{chapterId}?initialPage={page}` route |
| Background | Image per screen | Single `bg_reader.jpg` |
| Font family | Amiri (via Google Fonts) | Amiri (bundled `res/font/`) |

## Key Mindset Shift

In Flutter, you wrap widgets inside widgets to add padding, color, etc.:

```dart
Padding(
  padding: EdgeInsets.all(16),
  child: Container(color: Colors.amber, child: Text("Hello"))
)
```

In Compose, you chain Modifiers:

```kotlin
Text(
    "Hello",
    modifier = Modifier.padding(16.dp).background(amberColor)
)
```

> **Order matters in Modifiers!** `Modifier.padding(16.dp).background(amber)` ≠ `Modifier.background(amber).padding(16.dp)`
