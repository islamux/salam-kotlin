package com.islamux.khatir.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The app's palette, in one place — change a colour here and every screen follows.
 *
 * Kotlin `object` = a single shared instance (see di/AppModule.kt for the same
 * idea), which is exactly what a palette of constants wants.
 *
 * How to read a colour literal: `0xFF6033B4` is AARRGGBB, so the first two hex
 * digits are ALPHA (FF = fully opaque) and the rest are red, green, blue. That is
 * why `black87` is 0xDE000000: DE is 222/255 ≈ 87% opacity on pure black.
 *
 * `title` and `subtitle` are intentionally the same black today; they are separate
 * entries so the design can diverge them later without touching call sites.
 */
object AppColors {
    val title = Color(0xFF000000)
    val subtitle = Color(0xFF000000)
    /** Qur'anic verses and hadith get a violet accent so they stand apart from body text. */
    val ayahHadith = Color(0xFF6033B4)
    val footer = Color(0xFF757575)
    val golden = Color(0xFFFFE082)
    val goldenLight = Color(0xFFFFF3D4)
    val background = Color(0xFFF8F9FD)
    val grey = Color(0xFF8E8E8E)
    /** 87% opaque black — softer than pure black for large surfaces. */
    val black87 = Color(0xDE000000)
    val teal = Color(0xFF00897B)
    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)
}
