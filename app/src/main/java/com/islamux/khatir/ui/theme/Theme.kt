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
 * Material's role-based colour slots. `primary` is the brand colour and `onPrimary`
 * must be readable on it, so the two are always chosen together.
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
 * The root theme, applied ONCE in MainActivity and inherited by every screen below.
 *
 * @param darkTheme defaults to the system setting.
 * @param dynamicColor off by purpose: a wallpaper palette would replace this app's
 *   gold-on-black identity.
 * @param content the UI to wrap.
 */
@Composable
fun KhatirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Ordered by specificity: the most specific option that applies wins.
    val colorScheme = when {
        // Wallpaper colours exist only from Android 12, so older devices must not use these APIs.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Arabic-first, so layout direction is forced to RTL once here; everything below mirrors.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
