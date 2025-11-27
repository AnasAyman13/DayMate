package com.day.mate.data.local.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.day.mate.R
import com.day.mate.data.model.Todo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar


class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(todo: Todo) {
        if (!todo.remindMe) return


        val date = LocalDate.parse(todo.date)
        val time = LocalTime.parse(todo.time)
        val dateTime = LocalDateTime.of(date, time)

        val triggerAtMillis = dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_TODO_ID, todo.id)
            putExtra(ReminderConstants.EXTRA_TODO_TITLE, todo.title)
            putExtra(ReminderConstants.EXTRA_TODO_DESCRIPTION, todo.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todo.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancel(todoId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDailyReminder(hour: Int = 18, minute: Int = 30) {

        // 1. تحديد الوقت المستهدف (مثل 8:00 صباحاً)
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }
        val title = context.getString(R.string.daily_reminder_title)
        val content = context.getString(R.string.daily_reminder_content)


        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_TYPE, ReminderConstants.TYPE_DAILY_REMINDER)
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_TITLE, title)
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_CONTENT, content)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderConstants.NOTIFICATION_ID_DAILY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    @SuppressLint("ScheduleExactAlarm")
    fun schedulePomodoroBreak(triggerDateTime: LocalDateTime, breakType: String) {

        val triggerAtMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localizedBreakType = when (breakType) {
            "Long Break" -> context.getString(R.string.long_break)
            "Short Break" -> context.getString(R.string.short_break)
            else -> context.getString(R.string.short_break)
        }
        val title = context.getString(R.string.notification_break_finished_title, localizedBreakType)
        val content = context.getString(R.string.notification_break_finished_content)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_TYPE, ReminderConstants.TYPE_POMODORO_BREAK)
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_TITLE, title)
            putExtra(ReminderConstants.EXTRA_NOTIFICATION_CONTENT, content)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderConstants.NOTIFICATION_ID_POMODORO,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
    fun cancelPomodoroBreak() {
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderConstants.NOTIFICATION_ID_POMODORO,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

}
