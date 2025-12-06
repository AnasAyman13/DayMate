package com.day.mate.data.local.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.day.mate.MainActivity
import com.day.mate.R
import com.day.mate.data.local.TimerMode

object NotificationHelper {

    fun showTodoReminderNotification(
        context: Context,
        todoId: Int,
        title: String,
        description: String
    ) {

        createNotificationChannelIfNeeded(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(ReminderConstants.EXTRA_TODO_ID, todoId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)

        val notification = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_todo_filled)
            .setContentTitle(title)
            .setContentText(description.ifEmpty { "You have a task to do" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description.ifEmpty { "You have a task to do" })
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = ReminderConstants.NOTIFICATION_ID_BASE + todoId
        notificationManager.notify(notificationId, notification)
    }

    fun createNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                ReminderConstants.CHANNEL_ID,
                ReminderConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ReminderConstants.CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
                setSound(soundUri, attributes)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    fun showGeneralNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int,
    ) {
        createGeneralNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderConstants.GENERAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.forgrnd)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    fun showPomodoroNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int
    ) {
        createNotificationChannelIfNeeded(context)



        val notification = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pomodoro_filled)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notificationId, notification)
    }
    private fun createGeneralNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(
                ReminderConstants.GENERAL_CHANNEL_ID,
                ReminderConstants.GENERAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ReminderConstants.GENERAL_CHANNEL_DESCRIPTION
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createPersistentPomodoroChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderConstants.POMODORO_PERSISTENT_CHANNEL_ID,
                ReminderConstants.POMODORO_PERSISTENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the current Pomodoro timer status."
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showPersistentPomodoroNotification(
        context: Context,
        timerMode: TimerMode,
        secondsLeft: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createPersistentPomodoroChannel(context)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val modeString = when (timerMode) {

            TimerMode.FOCUS -> context.getString(R.string.focus)
            TimerMode.SHORT_BREAK -> context.getString(R.string.short_break)
            TimerMode.LONG_BREAK -> context.getString(R.string.long_break)
        }

        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val title = context.getString(R.string.pomodoro_timer_running)
        val content = "$modeString: $timeString"

        val notification = NotificationCompat.Builder(context, ReminderConstants.POMODORO_PERSISTENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pomodoro_filled)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ReminderConstants.NOTIFICATION_ID_PERSISTENT_POMODORO, notification)
    }

    fun cancelPersistentPomodoroNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ReminderConstants.NOTIFICATION_ID_PERSISTENT_POMODORO)
    }
}
