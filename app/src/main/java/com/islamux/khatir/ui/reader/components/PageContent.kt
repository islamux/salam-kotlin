package com.islamux.khatir.ui.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.islamux.khatir.data.model.Page
import com.islamux.khatir.ui.theme.AmiriFontFamily
import com.islamux.khatir.ui.theme.AppColors
import com.islamux.khatir.ui.theme.ContentStyles

/**
 * Renders ONE [Page] by executing the page's `order` contract (documented on
 * Page.kt): walk `order` from top to bottom and, for each field name, render the
 * NEXT unused element from the matching list.
 *
 * That is why this function keeps four `*Index` counters. They are the "how many
 * have I already drawn from this list?" memory of the walk, and they are what let
 * a name appear twice in `order` (drawing two different titles) or not at all
 * (drawing none).
 *
 * Compose details worth knowing:
 *  - `by remember { mutableIntStateOf(0) }` is three ideas in one line:
 *    `remember` keeps the value across recompositions (a font-size change
 *    re-runs this function; without remember the counters would reset and the
 *    page would re-render its first title over and over),
 *    `mutableIntStateOf` makes it observable state so a change triggers
 *    recomposition, and `by` is the delegate that lets us write `titleIndex++`
 *    instead of `titleIndex.value++`.
 *  - The counters therefore only make sense for a FRESH instance. ReaderScreen
 *    wraps this composable in `key(uiState.fontSize)` so a font change builds a
 *    new one instead of resuming stale counters.
 *
 * Each content type gets its own style: titles, subtitles, ayahs and the footer
 * are centered, body texts are justified, and the offsets from [fontSize]
 * (+4, +2, 0, -4) keep that visual hierarchy at ANY size the reader picks.
 */
@Composable
fun PageContent(page: Page, fontSize: Float) {
    val scrollState = rememberScrollState()
    var titleIndex by remember { mutableIntStateOf(0) }
    var subtitleIndex by remember { mutableIntStateOf(0) }
    var textIndex by remember { mutableIntStateOf(0) }
    var ayahIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 60.dp)
    ) {
        for (field in page.order) {
            when (field) {
                "titles" -> {
                    // Guard before indexing: a name may appear in `order` more
                    // times than the list has elements, and `[]` on a missing
                    // index would crash the app.
                    if (titleIndex < page.titles.size) {
                        Text(
                            text = page.titles[titleIndex],
                            style = ContentStyles.title.copy(fontSize = (fontSize + 4).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        titleIndex++
                    }
                }
                "subtitles" -> {
                    if (subtitleIndex < page.subtitles.size) {
                        Text(
                            text = page.subtitles[subtitleIndex],
                            style = ContentStyles.subtitle.copy(fontSize = (fontSize + 2).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                        subtitleIndex++
                    }
                }
                "texts" -> {
                    if (textIndex < page.texts.size) {
                        Text(
                            // Body text: the Amiri font, justified alignment and a
                            // 1.6em line height. Justify plus generous line height
                            // is what keeps long Arabic paragraphs readable — `em`
                            // means the size is RELATIVE to the text size, so the
                            // spacing grows with the reader's font setting.
                            text = page.texts[textIndex],
                            fontFamily = AmiriFontFamily,
                            fontSize = fontSize.sp,
                            lineHeight = 1.6f.em,
                            color = AppColors.black,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        textIndex++
                    }
                }
                "ayahs" -> {
                    if (ayahIndex < page.ayahs.size) {
                        Text(
                            // Verses and hadith get their own style from the
                            // theme, so they stand apart from ordinary paragraphs.
                            text = page.ayahs[ayahIndex],
                            style = ContentStyles.ayahHadith.copy(fontSize = (fontSize + 2).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        ayahIndex++
                    }
                }
                "footer" -> {
                    // Unlike the lists, the footer is a single optional String, so
                    // `?.let` renders it only when it exists — and it never
                    // advances any counter, because it can appear only once.
                    page.footer?.let { footer ->
                        Text(
                            text = footer,
                            style = ContentStyles.footer.copy(fontSize = (fontSize - 4).sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
