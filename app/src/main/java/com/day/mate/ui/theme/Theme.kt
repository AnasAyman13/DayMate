package com.day.mate.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// --- ✅ ده الـ Dark Theme الحقيقي بتاعك ---
private val AppDarkColorScheme = darkColorScheme(
    primary = AppCyan,       // اللون الأساسي (سماوي)
    onPrimary = DarkBg,      // لون الكلام اللي فوق السماوي (أسود/غامق)
    background = DarkBg,     // خلفية التطبيق
    onBackground = DarkText, // لون الكلام اللي فوق الخلفية
    surface = DarkField,     // لون الكروت وحقول الإدخال
    onSurface = DarkText     // لون الكلام اللي فوق الكروت
    // (ممكن نضيف بقية الألوان زي secondary لو احتجنا)
)

@Composable
fun DayMateTheme(
    // إحنا أجبرنا التطبيق يبقى Dark Mode
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // هنقفل الألوان الديناميكية عشان نحافظ على تصميمنا
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicDarkColorScheme(context)
        }
        else -> AppDarkColorScheme // <-- بنستخدم الألوان بتاعتنا
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // (ملف Type.kt زي ما هو)
        content = content
    )
}