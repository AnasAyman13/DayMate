package com.day.mate.data.model
import androidx.room.Query
import com.day.mate.combineDateTimeToTimestamp
import com.day.mate.data.local.TodoEntity
import com.day.mate.data.model.Todo
import com.day.mate.formatTimestampToHourLabel

import com.day.mate.ui.theme.PrimaryColor // لاستخدام اللون الرئيسي
data class Todo(

    val id: Int = 0,
    val remoteId: String = "",  // For FireStore
    val title: String,
    val description: String,
    val category: String,
    val date: String, // (e.g., "2025-11-07")
    val time: String, // (e.g., "13:30")
    val remindMe: Boolean,

    val isDone: Boolean
)
fun Todo.toTimelineEvent(): TimelineEvent {
    val timestamp = combineDateTimeToTimestamp(this.date, this.time)

    return TimelineEvent(
        id = "todo-${this.id}",
        timestamp = timestamp,
        title = this.title,
        description = this.description,
        timeRange = this.time,
        type = EventType.TODO_TASK,
        icon = "task_alt",
        iconColor = PrimaryColor,
        isDone = this.isDone,
        isProgress = null,
        timeLabel = formatTimestampToHourLabel(timestamp)
    )
}