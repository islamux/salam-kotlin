package com.islamux.khatir.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color

/**
 * Material's role-based colour slots. `primary` is the brand colour, while
 * `onPrimary` is the colour that must be readable ON it — that pairing is what
 * guarantees legible buttons, so the two are always chosen together.
 *
 * Both schemes lead with the same gold and black: the app keeps its identity in
 * dark mode instead of inverting into a generic look.
 */
private val LightColorScheme = lightColorScheme(
    primary = AppColors.golden,
    onPrimary = AppColors.black,
    primaryContainer = AppColors.goldenLight,
    onPrimaryContainer = AppColors.black87,
    secondary = AppColors.teal,
    onSecondary = AppColors.white,
    tertiary = AppColors.ayahHadith,
    onTertiary = AppColors.white,
    background = AppColors.background,
    onBackground = AppColors.black,
    surface = AppColors.white,
    onSurface = AppColors.black,
    onSurfaceVariant = AppColors.grey,
)

/** Dark counterparts. A few colours are written inline here because they exist only for this scheme. */
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.golden,
    onPrimary = AppColors.black,
    primaryContainer = Color(0xFF4A3A10),
    onPrimaryContainer = AppColors.golden,
    secondary = AppColors.teal,
    onSecondary = AppColors.white,
    tertiary = Color(0xFFB388FF),
    onTertiary = AppColors.white,
    background = Color(0xFF1A1A2E),
    onBackground = AppColors.white,
    surface = Color(0xFF16213E),
    onSurface = AppColors.white,
    onSurfaceVariant = Color(0xFFBDBDBD),
)

/**
 * The root theme, applied ONCE in MainActivity and inherited by every screen
 * below it. Screens never call this themselves — they just read
 * `MaterialTheme.colorScheme` and get the right values for free.
 *
 * @param darkTheme defaults to the system setting, so the app follows the
 * phone's light/dark preference automatically.
 * @param dynamicColor when true, Android 12+ takes the colours from the user's
 * wallpaper. It is OFF by default on purpose: this app's gold-on-black identity
 * is part of its design, and a wallpaper-derived palette would replace it.
 * @param content the UI to wrap — a lambda, so the theme applies to whatever
 * is composed inside it.
 */
@Composable
fun KhatirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Ordered by specificity: the most specific option that applies wins.
    val colorScheme = when {
        // Build.VERSION.SDK_INT is a runtime version check — the wallpaper colours
        // only exist from Android 12 (S) onward, and older devices must not touch
        // these APIs at all.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // The app is Arabic-first, so the layout direction is forced to RTL here,
    // once, instead of per screen. Everything laid out below then mirrors
    // automatically: Row lays out right-to-left, and mirrored icons such as
    // Icons.AutoMirrored.Filled.ArrowBack point the correct way.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
