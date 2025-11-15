package com.day.mate.data.model

import androidx.compose.ui.graphics.Color
import com.day.mate.ui.theme.PrimaryColor
import com.day.mate.ui.theme.EventColorSky
import com.day.mate.ui.theme.EventColorGreen



enum class EventType {
    PRAYER,
    TODO_TASK
}

data class TimelineEvent(
    val id: String, //  "todo-123" أو "prayer-Fajr")
    val timestamp: Long,
    val title: String,
    val description: String,
    val timeRange: String,
    val type: EventType,
    val icon: String,
    val iconColor: Color,
    val isDone: Boolean = false,
    val isProgress: Float? = null,
    val timeLabel: String = ""
)