package com.day.mate.data.local.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // 🚨 التعديل 1: قراءة نوع الإشعار (إذا تم إرساله)
        val notificationType = intent.getIntExtra(ReminderConstants.EXTRA_NOTIFICATION_TYPE, -1)

        // قراءة البيانات المشتركة (العنوان والوصف)
        val title = intent.getStringExtra(ReminderConstants.EXTRA_TODO_TITLE) ?: "Task Reminder"
        val description = intent.getStringExtra(ReminderConstants.EXTRA_TODO_DESCRIPTION) ?: ""


        // 🚨 التعديل 2: فحص نوع الإشعار الجديد (البومودورو)
        if (notificationType == ReminderConstants.TYPE_POMODORO_BREAK) {
            // 🚨 1. قراءة البيانات باستخدام المفاتيح المخصصة
            val title = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_TITLE)
            val content = intent.getStringExtra(ReminderConstants.EXTRA_NOTIFICATION_CONTENT)

            if (title != null && content != null) {
                // 🚨 2. استدعاء دالة العرض المخصصة (showPomodoroNotification)
                NotificationHelper.showPomodoroNotification(
                    context = context,
                    title = title,
                    content = content,
                    notificationId = ReminderConstants.NOTIFICATION_ID_POMODORO
                )
            }
            return
        }

        // 🚨 التعديل 3: التعامل مع إشعارات المهام (الكود الأصلي)
        // إذا لم يكن إشعار بومودورو، نعتبره إشعار مهمة (Todo)
        val todoId = intent.getIntExtra(ReminderConstants.EXTRA_TODO_ID, -1)

        // إذا لم يتم العثور على ID للمهمة، نتوقف
        if (todoId == -1) return


        // عرض إشعار المهمة (Todo) باستخدام الدالة الأصلية
        NotificationHelper.showTodoReminderNotification(
            context = context,
            todoId = todoId,
            title = title,
            description = description
        )
    }
}