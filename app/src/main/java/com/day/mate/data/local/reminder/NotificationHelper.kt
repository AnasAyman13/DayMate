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
        notificationId: Int, // رقم تعريف فريد
    ) {
        // 1. إنشاء القناة إذا لم تكن موجودة (استدعاء نفس الدالة)
        createNotificationChannelIfNeeded(context)

        // 2. يمكنك تغيير soundUri ليكون مختلفاً عن تنبيهات المهام إذا أردتِ
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // تغيير من TYPE_ALARM

        // 3. PendingIntent بدون أي بيانات إضافية (Intent بسيط يفتح MainActivity)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // استخدام رقم تعريف الإشعار العام
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. بناء الإشعار
        val notification = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_todo_filled) // ⚠️ يجب التأكد من استخدام أيقونة مناسبة
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // تصنيف مختلف
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // نمط اهتزاز أبسط
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 5. عرض الإشعار
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
}
