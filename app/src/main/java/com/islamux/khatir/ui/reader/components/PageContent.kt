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
