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

@Composable
fun KhatirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
