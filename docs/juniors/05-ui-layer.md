# UI Layer — Compose Screens and Components

## Overview

The UI is built entirely with **Jetpack Compose**. The entry point:

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

**Key aspects:**
- `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` — app-wide RTL
- `SalamTheme` — golden/amber theme on light background
- `NavGraph` — all screen routing

---

## Theme

### AppColors — Color Constants

**File**: `ui/theme/Color.kt` (defines `AppColors` object)

```kotlin
object AppColors {
    val golden = Color(0xFFFFE082)       // Amber/golden — primary accent
    val purple = Color(0xFF6033B4)       // Purple — used for ayahs/hadith
    val grey = Color(0xFF9E9E9E)         // Grey — used for footer text
    val black = Color(0xFF000000)        // Black — text and app bars
    val white = Color(0xFFFFFFFF)        // White
    val background = Color(0xFFF8F9FD)   // Light background
    val cardBackground = Color(0xFFFFF8E1) // Light amber for cards
}
```

Colors are defined in a single `object` for consistency. The palette exactly matches the Flutter version:

| Usage | Color | Hex |
|-------|-------|-----|
| Accent / icons | Amber/Golden | `#FFE082` |
| Ayahs / hadith | Purple | `#6033B4` |
| Footer / citations | Grey | `#9E9E9E` |
| App bars | Black | `#000000` |
| Background | Light grey | `#F8F9FD` |

### ContentStyles — Text Styles

**File**: `ui/theme/Type.kt`

```kotlin
object ContentStyles {
    val title = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )
    val subtitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
    val ayahHadith = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = AppColors.purple,
        textAlign = TextAlign.Center
    )
    val footer = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = AppColors.grey,
        textAlign = TextAlign.Center
    )
}
```

### SalamTheme — Material3 Light Theme

```kotlin
@Composable
fun SalamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,  // ← always light mode
        typography = AppTypography,
        content = content,
    )
}
```

Unlike Athkarix (dark theme), Salam uses a light background (`#F8F9FD`) with amber accents.

---

## Screen-by-Screen Tour

### HomeScreen

The main menu with a drawer. Structure:

```
ModalNavigationDrawer
├── Drawer: WhatsApp (Contact Us) + Share App tiles (golden bg)
└── Scaffold
    ├── TopAppBar: Black bg, golden "خواطر إيمانية", search + share actions
    ├── Background image (bg_home.jpg)
    ├── Scroll hint: "إسحب للأعلى للمزيد" (top-right)
    └── LazyColumn of ChapterButton widgets
```

Each chapter is a `ChapterButton`:

```kotlin
@Composable
fun ChapterButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.golden),
        shape = RoundedCornerShape(20.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.MenuBook, tint = AppColors.black)
        Text(title, fontFamily = AmiriFontFamily, fontWeight = Bold, fontSize = 17.sp)
    }
}
```

**Exit behavior:** On back press, an `AlertExitDialog` appears:
- "هل تريد الخروج من التطبيق؟" (Do you want to exit?)
- نعم / لا (Yes / No)

### ReaderScreen

The reader with RTL swipe. Structure:

```
Scaffold
├── Background image (bg_reader.jpg)
├── TopAppBar
│   ├── Share icon (golden)
│   ├── Title: chapter title (golden, from AppStrings.chapterTitle)
│   ├── Font decrease (-)
│   ├── "الخط" (Font label)
│   └── Font increase (+)
└── Content
    ├── HorizontalPager(reverseLayout = true)  ← RTL swipe
    │   └── Page content: titles, subtitles, texts, ayahs, footer
    └── Slider (page indicator)
```

The page content is rendered per-field with styles:

```kotlin
// Inside the pager page:
Column {
    page.titles.forEach { Text(it, style = ContentStyles.title) }
    page.subtitles.forEach { Text(it, style = ContentStyles.subtitle) }
    page.texts.forEach { Text(it, style = ...) }
    page.ayahs.forEach { Text(it, style = ContentStyles.ayahHadith, color = purple) }
    page.footer?.let { Text(it, style = ContentStyles.footer) }
}
```

**State flow:**
```kotlin
val uiState by viewModel.uiState.collectAsState()

// Pager sync
LaunchedEffect(uiState.currentPageIndex) {
    viewModel.navigateToPage(pagerState.currentPage)
}
```

### SearchScreen

Full-text search across all chapters. Structure:

```
Scaffold
├── TopAppBar: Black bg, golden "بحث" title, back arrow
└── Content
    ├── OutlinedTextField (search input)
    └── Results
        ├── If blank: Prompt "ابحث عن المحتوى المطلوب"
        ├── If searching: "جارٍ البحث..."
        ├── If no results: "لا توجد نتائج للبحث"
        └── LazyColumn of results
            └── Each result: matched text (title), chapter + field (subtitle), arrow
```

---

## State Flow Pattern

Every screen follows this exact pattern:

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel(factory = ...)) {
    val uiState by viewModel.uiState.collectAsState()

    // Render based on state
    Text(uiState.someField)

    // User action → ViewModel
    Button(onClick = { viewModel.someAction() })
}
```

The flow:
```
ViewModel.StateFlow ──collectAsState()──► Composable (renders)
                                           │
ViewModel.action()  ◀───────onClick────────┘
```
