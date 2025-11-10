package com.day.mate.data.model

import androidx.compose.ui.graphics.Color
import com.day.mate.ui.theme.PrimaryColor
import com.day.mate.ui.theme.EventColorSky
import com.day.mate.ui.theme.EventColorGreen

// تم تعريف الألوان في ملف Colors.kt سابقًا

enum class EventType {
    PRAYER,
    TODO_TASK
}

data class TimelineEvent(
    val id: String, // معرف فريد (مثلاً: "todo-123" أو "prayer-Fajr")
    val timestamp: Long, // أهم حقل للفرز
    val title: String,
    val description: String, // يمكن استخدامها للوصف أو اسم الصلاة الفرعي
    val timeRange: String, // النص المعروض للوقت (مثل 10:00 - 11:30)
    val type: EventType,
    val icon: String, // اسم الرمز (مثل "task_alt" أو "self_improvement")
    val iconColor: Color,
    val isDone: Boolean = false, // خاص بالمهام
    val isProgress: Float? = null, // نسبة التقدم
    val timeLabel: String = "" // تسمية الساعة (مثل 10 AM)
)