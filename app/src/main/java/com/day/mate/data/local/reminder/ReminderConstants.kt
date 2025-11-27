package com.day.mate.data.local.reminder

object ReminderConstants {
    const val CHANNEL_ID = "todo_reminder_channel"
    const val CHANNEL_NAME = "Todo Reminders"
    const val CHANNEL_DESCRIPTION = "Reminders for your tasks"

    const val EXTRA_TODO_ID = "extra_todo_id"
    const val EXTRA_TODO_TITLE = "extra_todo_title"
    const val EXTRA_TODO_DESCRIPTION = "extra_todo_description"


    const val NOTIFICATION_ID_BASE = 1000

    // 🚨 ثوابت البومودورو: هذه تضمن الفصل الكامل
    const val TYPE_POMODORO_BREAK = 101
    const val NOTIFICATION_ID_POMODORO = 2000

    // 🚨 مفاتيح مخصصة لبيانات إشعار البومودورو
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
    const val EXTRA_NOTIFICATION_CONTENT = "extra_notification_content"
}
