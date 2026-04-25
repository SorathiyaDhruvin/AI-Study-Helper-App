package com.aistudy.solver.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = GradientStart,
    tertiary = AccentGold,
    background = BaseBackgroundDark,
    surface = BaseSurfaceDark,
    surfaceVariant = BaseCardDark,
    onPrimary = BaseTextPrimaryDark,
    onSecondary = BaseTextPrimaryDark,
    onBackground = BaseTextPrimaryDark,
    onSurface = BaseTextPrimaryDark,
    onSurfaceVariant = BaseTextSecondaryDark,
    outline = BaseOutlinedDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = GradientStart,
    tertiary = AccentGold,
    background = BaseBackgroundLight,
    surface = BaseSurfaceLight,
    surfaceVariant = Color(0xFFE5E7EB),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp)
)

val LocalThemeMode = staticCompositionLocalOf<MutableState<Boolean>> { error("No ThemeMode provided") }

@Composable
fun AIStudySolverTheme(
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb() // Make status bar transparent for premium headers
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}


