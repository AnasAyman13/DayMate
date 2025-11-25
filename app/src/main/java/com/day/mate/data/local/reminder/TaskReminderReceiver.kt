package com.day.mate.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("title") ?: "Task Reminder"
        val description = intent.getStringExtra("description") ?: ""

        // 🔔 Alarm Sound (Strong)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // 🔔 Notification
        val builder = NotificationCompat.Builder(context, "task_reminder_channel")
            .setSmallIcon(com.day.mate.R.drawable.ic_todo_filled) // غيّرها لو عندك أيقونة تانية
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setSound(alarmSound) // 🔥 صوت المنبه
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // --- Extract Task ID ---
        val taskId = intent.getIntExtra("task_id", -1)

// --- PendingIntent to open the app and edit the task ---
        val openIntent = Intent(context, com.day.mate.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

// Attach the pending intent to notification:
        builder.setContentIntent(pendingIntent)


        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // 🔥 Vibration قوية
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                1500,  // مدة الاهتزاز
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}
