package com.day.mate.data.local.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra(ReminderConstants.EXTRA_TODO_ID, -1)
        val title = intent.getStringExtra(ReminderConstants.EXTRA_TODO_TITLE) ?: "Task Reminder"
        val description = intent.getStringExtra(ReminderConstants.EXTRA_TODO_DESCRIPTION) ?: ""

        if (todoId == -1) return


        NotificationHelper.showTodoReminderNotification(
            context = context,
            todoId = todoId,
            title = title,
            description = description
        )
    }
}
