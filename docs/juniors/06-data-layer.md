# Data Layer

## Overview

Salam has a single data source: a JSON file in `assets/`. No network, no database, no SharedPreferences.

```
khatira_content.json (assets/)
    │
    ▼
JsonKhatiraRepository (cached in memory)
    │
    ▼
ViewModels (HomeViewModel, ReaderViewModel, SearchViewModel)
```

---

## 1. Data Models

**Files**: `data/model/` (3 files)

### KhatiraContent — Root
```kotlin
@Serializable
data class KhatiraContent(
    val chapters: List<Chapter> = emptyList()
)
```

### Chapter
```kotlin
@Serializable
data class Chapter(
    val id: String,          // "pre", "1", "2", ..., "32", "final"
    val orderIndex: Int,      // 0–33 (display order)
    val title: String,        // Generic title from JSON (e.g. "الخاطرة 1")
    val pages: List<Page>
)
```

### Page
```kotlin
@Serializable
data class Page(
    val titles: List<String> = emptyList(),
    val subtitles: List<String> = emptyList(),
    val texts: List<String> = emptyList(),
    val ayahs: List<String> = emptyList(),
    val footer: String? = null,
    val order: List<String> = emptyList()   // Field display order
)
```

Each page has 5 content fields plus an `order` list that defines the display sequence. The `footer` is nullable.

---

## 2. KhatiraRepository — Interface

**File**: `data/repository/KhatiraRepository.kt`

```kotlin
interface KhatiraRepository {
    suspend fun getContent(): KhatiraContent
    suspend fun getChapter(chapterId: String): Chapter?
    suspend fun getAllChapters(): List<Chapter>
}
```

A clean interface so the ViewModels never depend on the JSON implementation directly. `getContent()` returns everything; `getChapter()` and `getAllChapters()` are convenience methods.

---

## 3. JsonKhatiraRepository — Implementation

**File**: `data/repository/JsonKhatiraRepository.kt`

```kotlin
class JsonKhatiraRepository(private val context: Context) : KhatiraRepository {

    private var cachedContent: KhatiraContent? = null
    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    override suspend fun getContent(): KhatiraContent {
        cachedContent?.let { return it }
        val jsonString = context.assets
            .open("khatira_content.json")
            .bufferedReader()
            .use { it.readText() }
        val content = jsonDecoder.decodeFromString<KhatiraContent>(jsonString)
        cachedContent = content
        return content
    }

    override suspend fun getChapter(chapterId: String): Chapter? {
        return getContent().chapters.find { it.id == chapterId }
    }

    override suspend fun getAllChapters(): List<Chapter> {
        return getContent().chapters
    }
}
```

**Pattern:**
1. Check cache first (`cachedContent?.let { return it }`)
2. If not cached, open the JSON file from `assets/` using `Context.assets.open()`
3. Decode with `kotlinx.serialization` (`Json { ignoreUnknownKeys = true }`)
4. Cache and return

The `ignoreUnknownKeys = true` flag ensures backwards compatibility — if the JSON adds new fields, old code won't crash.

---

## 4. AppStrings — Arabic UI Strings

**File**: `data/static/AppStrings.kt`

```kotlin
object AppStrings {
    const val homeAppBarTitle = "خواطر إيمانية"
    const val searchHint = "ابحث في الخواطر..."
    const val searchPrompt = "ابحث عن المحتوى المطلوب"
    // ... 40+ constants

    fun chapterTitle(id: String): String = when (id) {
        "pre" -> "خواطر متفرقة حول الدين والحياة"
        "1" -> "(1) عوامل تفكك وفشل الأسرة والقبيلة"
        // ... 32 more
        "final" -> " (خاتمة)  المصير المحتوم "
        else -> "الخاطرة $id"
    }

    fun resultCount(count: Int) = "$count نتيجة"
}
```

Centralized Arabic strings matching the Flutter app exactly. The `chapterTitle()` function returns the descriptive titles that appear on home buttons and reader AppBars.

---

## 5. JSON File Format

**File**: `assets/khatira_content.json` (848KB, ~532 pages)

```json
{
  "chapters": [
    {
      "id": "pre",
      "orderIndex": 0,
      "title": "المقدمة",
      "pages": [
        {
          "titles": ["خواطر متفرقة حول الدين والحياة"],
          "subtitles": [],
          "texts": ["بسم الله الرحمن الرحيم..."],
          "ayahs": [],
          "footer": null,
          "order": ["titles", "texts"]
        }
      ]
    }
  ]
}
```

34 chapters total: `pre` + `1`–`32` + `final`. Each chapter has 1–30+ pages. Each page lists its fields and their display order.

---

## Data Flow Summary

```
                    ┌─────────────────────────┐
                    │  khatira_content.json    │
                    │  (assets/, 848KB)        │
                    └───────────┬─────────────┘
                                │
                                ▼
                    ┌─────────────────────────┐
                    │  JsonKhatiraRepository   │
                    │  (cached in memory)      │
                    └──────┬──────────┬───────┘
                           │          │
              ┌────────────┘          └────────────┐
              ▼                                    ▼
┌─────────────────────────┐      ┌─────────────────────────┐
│    HomeViewModel        │      │   ReaderViewModel       │
│  (loads all chapters)   │      │  (loads one chapter)    │
└─────────────────────────┘      └─────────────────────────┘
                                            │
                                            ▼
                                  ┌─────────────────────────┐
                                  │   SearchViewModel        │
                                  │  (searches all chapters) │
                                  └─────────────────────────┘
```

All data is loaded once and cached. The JSON is read from assets only on first access — subsequent calls hit the in-memory cache.
