package com.aistudy.solver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Gradient
val GradientStart = Color(0xFF7E57C2) // Purple
val GradientEnd = Color(0xFF42A5F5)   // Blue
val PrimaryColor = Color(0xFF6C63FF)

// Static Dark Base Palette
val BaseBackgroundDark = Color(0xFF0D0B1A)
val BaseSurfaceDark = Color(0xFF14112A)
val BaseCardDark = Color(0xFF1E1A3D)
val BaseOutlinedDark = Color(0xFF2C2755)

// Static Light Base Palette
val BaseBackgroundLight = Color(0xFFF3F4F6)
val BaseSurfaceLight = Color(0xFFFFFFFF)

val BaseTextPrimaryDark = Color(0xFFFFFFFF)
val BaseTextSecondaryDark = Color(0xFFB0AEDB)

// Accent & Status
val AccentGold = Color(0xFFFFD700)
val ErrorColor = Color(0xFFEF4444)
val SuccessColor = Color(0xFF10B981)

val GlassLight = Color(0xCCFFFFFF)
val GlassDark = Color(0x1AFFFFFF)

// Legacy Aliases
val SecondaryColor = GradientStart
val AccentColor = AccentGold

// Dynamic Theming Getters used across all App Screens
val BackgroundDark: Color
    @Composable get() = MaterialTheme.colorScheme.background

val SurfaceDark: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val CardDark: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val OutlinedDark: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant


