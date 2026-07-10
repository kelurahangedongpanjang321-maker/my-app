package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.data.SettingsEntity

// Parses hex colors safely with default fallbacks
fun parseHexColor(hexString: String, defaultColor: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexString))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun SIMPELTheme(
    settings: SettingsEntity,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Determine light/dark mode based on setting
    val isDark = when (settings.temaMode) {
        "Dark Mode" -> true
        "Light Mode" -> false
        else -> darkTheme
    }

    // Determine primary color from settings
    val primaryColor = parseHexColor(settings.warnaUtama, Color(0xFF005AC1))

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = if (settings.warnaUtama == "#FFFFFF") Color.Black else Color.White,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = Color(0xFFE0A800), // Gold accent
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onBackground = Color(0xFFE3E3E3),
            onSurface = Color(0xFFF5F5F5),
            surfaceVariant = Color(0xFF2D2D2D),
            onSurfaceVariant = Color(0xFFCCCCCC)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = if (settings.warnaUtama == "#FFFFFF") Color.Black else Color.White,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = Color(0xFFFFB300), // Gold accent
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFF1F3F4),
            onSurfaceVariant = Color(0xFF444746)
        )
    }

    // Determine font family
    val selectedFontFamily = when (settings.fontType) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    // Apply font family to Material Typography presets
    val customTypography = androidx.compose.material3.Typography(
        displayLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.25.sp
        ),
        titleLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
