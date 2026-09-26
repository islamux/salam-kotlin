package com.islamux.khatir.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3's standard text scale, mapped onto the Amiri font.
 *
 * These styles carry FIXED sizes, so they suit app chrome: app bar titles, labels,
 * buttons — anything whose size does not depend on the reader's preference.
 * [ContentStyles] below is the counterpart used for the actual reading text.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

/**
 * The app's own text styles, one per content type in the reader: page titles,
 * subtitles, verses/hadith and the footer. PageContent.kt picks the right one for
 * each field it renders, which is what keeps the reading hierarchy consistent
 * across every page of the book.
 *
 * THE KEY DIFFERENCE from [Typography]: these styles deliberately carry NO font
 * size. The reader lets the user choose the text size (21f..37f, owned by
 * ReaderViewModel), so PageContent applies it with `style.copy(fontSize = ...)` —
 * `copy()` takes everything from the style (font, weight, colour) and overrides
 * only the size. If a size were baked in here, the A+ / A- buttons could not
 * work.
 */
object ContentStyles {
    val title = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        color = AppColors.title
    )
    val subtitle = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        color = AppColors.subtitle
    )
    /** Verses and hadith: bold and violet, deliberately unlike the body text. */
    val ayahHadith = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        color = AppColors.ayahHadith
    )
    /** The closing note of a page — muted grey so it never competes with the text. */
    val footer = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        color = AppColors.footer
    )
}
