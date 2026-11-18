package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary = BcaElectricCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004B87),
    onPrimaryContainer = Color(0xFFD6EEFF),
    secondary = BcaSoftSky,
    onSecondary = Color(0xFF041E3A),
    secondaryContainer = Color(0xFF0E3963),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = CoralExpense,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C0D1D),
    onTertiaryContainer = Color(0xFFFECDD3),
    background = DarkBackground,
    onBackground = Color(0xFFF0F7FF),
    surface = DarkSurface,
    onSurface = Color(0xFFF0F7FF),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9CC2E6),
    outline = DarkOutline,
    error = CoralExpense
)

private val LightColorScheme = lightColorScheme(
    primary = BcaRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF0FF),
    onPrimaryContainer = Color(0xFF00356B),
    secondary = BcaCobaltBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0A2E52),
    tertiary = CoralExpense,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF881337),
    background = LightBackground,
    onBackground = BcaTextNavy,
    surface = LightSurface,
    onSurface = BcaTextNavy,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = BcaTextSlate,
    outline = LightOutline,
    error = CoralExpense
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to clean minimalist myBCA Blue & White Glassmorphism
    fontScaleFactor: Float = 1.0f,
    highContrast: Boolean = false,
    accentTheme: String = "ROYAL_BLUE",
    content: @Composable () -> Unit
) {
    val primaryColor = when (accentTheme) {
        "EMERALD" -> EmeraldPrimary
        "COBALT" -> BcaCobaltBlue
        "VIOLET" -> Color(0xFF7C3AED)
        else -> if (darkTheme) BcaElectricCyan else BcaRoyalBlue
    }

    val baseLight = LightColorScheme.copy(
        primary = primaryColor,
        outline = if (highContrast) Color(0xFF0F172A) else LightOutline
    )
    val baseDark = DarkColorScheme.copy(
        primary = if (accentTheme == "EMERALD") Color(0xFF34D399) else primaryColor,
        outline = if (highContrast) Color(0xFFFFFFFF) else DarkOutline
    )
    val colorScheme = if (darkTheme) baseDark else baseLight

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * fontScaleFactor.coerceIn(0.85f, 1.5f)
    )

    CompositionLocalProvider(LocalDensity provides customDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
