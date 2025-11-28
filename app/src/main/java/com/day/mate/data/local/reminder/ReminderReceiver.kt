package com.day.mate.data.local.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = ReminderScheduler(context)

            scheduler.scheduleDailyReminder(hour = 10, minute = 0)
        }

        val notificationType = intent.getIntExtra(ReminderConstants.EXTRA_NOTIFICATION_TYPE, -1)
        val title = intent.getStringExtra(ReminderConstants.EXTRA_TODO_TITLE) ?: "Task Reminder"
        val description = intent.getStringExtra(ReminderConstants.EXTRA_TODO_DESCRIPTION) ?: ""

        if (notificationType == ReminderConstants.TYPE_POMODORO_BREAK) {

            val title = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_TITLE)
            val content = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_CONTENT)

            if (title != null && content != null) {
                NotificationHelper.showPomodoroNotification(
                    context = context,
                    title = title,
                    content = content,
                    notificationId = ReminderConstants.NOTIFICATION_ID_POMODORO
                )
            }
            return
        }
        if (notificationType == ReminderConstants.TYPE_DAILY_REMINDER) {
            val title = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_TITLE)
            val content = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_CONTENT)

            if (title != null && content != null) {
                NotificationHelper.showGeneralNotification(
                    context = context,
                    title = title,
                    content = content,
                    notificationId = ReminderConstants.NOTIFICATION_ID_DAILY
                )
            }
            return
        }
        val todoId = intent.getIntExtra(ReminderConstants.EXTRA_TODO_ID, -1)
        if (todoId == -1) return
        NotificationHelper.showTodoReminderNotification(
            context = context,
            todoId = todoId,
            title = title,
            description = description
        )
    }
}