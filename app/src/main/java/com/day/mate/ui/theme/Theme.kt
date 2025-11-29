package com.day.mate.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Dark color scheme for the application.
 * Uses custom colors defined in Color.kt
 */
private val AppDarkColorScheme = darkColorScheme(
    primary = AppCyan,           // Cyan accent color
    onPrimary = DarkBg,          // Text color on primary (dark background)
    background = DarkBg,         // Main dark background
    onBackground = DarkText,     // Text color on background (white)
    surface = DarkField,         // Card and input field background
    onSurface = DarkText,        // Text color on surface (white)
    secondary = AppGold,         // Gold accent color
    onSecondary = DarkBg         // Text on secondary (dark)
)

/**
 * Light color scheme for the application.
 * Provides an alternative theme for users who prefer light mode.
 */
private val AppLightColorScheme = lightColorScheme(
    primary = AppCyan,           // Cyan accent color
    onPrimary = Color.White,     // White text on primary
    background = Color(0xFFF6F8F8), // Light gray background
    onBackground = Color(0xFF102022), // Dark text on background
    surface = Color.White,       // White cards and surfaces
    onSurface = Color(0xFF102022), // Dark text on surface
    secondary = AppGold,         // Gold accent color
    onSecondary = Color.White    // White text on secondary
)

/**
 * DayMateTheme
 *
 * Main theme composable for the application.
 * Supports both dark and light modes with optional dynamic colors (Android 12+).
 *
 * @param darkTheme Whether to use dark theme. Defaults to true but can be controlled
 *                  by user preference via SettingsViewModel.
 * @param dynamicColor Whether to use Material You dynamic colors (Android 12+).
 *                     Defaults to false to maintain consistent branding.
 * @param content The composable content to be themed.
 *
 * Usage:
 * ```
 * DayMateTheme(darkTheme = isDarkMode) {
 *     // Your app content here
 * }
 * ```
 */
@Composable
fun DayMateTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Select color scheme based on theme settings
    val colorScheme = when {
        // Use dynamic colors on Android 12+ if enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Use custom dark colors
        darkTheme -> AppDarkColorScheme
        // Use custom light colors
        else -> AppLightColorScheme
    }

    // Apply the selected color scheme to MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}