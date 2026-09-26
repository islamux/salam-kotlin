package com.islamux.khatir.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.islamux.khatir.R

/**
 * The app's typeface: Amiri, a font designed for Arabic typesetting, bundled in
 * the APK under res/font so it renders identically on every device with no
 * network download.
 *
 * A FontFamily is a SET of weights for one typeface. Registering both weights
 * here is what lets a single `fontFamily = AmiriFontFamily` produce correct bold
 * text whenever something asks for FontWeight.Bold — otherwise the platform would
 * fake-bold it by smearing the glyphs, which looks poor for Arabic.
 *
 * This is a top-level `val`, not an `object` member, because it is a single value
 * rather than a group of related members.
 */
val AmiriFontFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)
