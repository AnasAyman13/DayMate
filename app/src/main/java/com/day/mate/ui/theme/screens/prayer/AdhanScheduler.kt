package com.day.mate.ui.theme.screens.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.day.mate.services.AdhanService // استيراد الخدمة
import java.util.*
import java.text.SimpleDateFormat

fun scheduleAdhan(context: Context, prayer: String, hour: Int, minute: Int) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // إذا كان الوقت قد مر اليوم، حدد موعده للغد
        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
    }

    val intent = Intent(context, AdhanService::class.java).apply {
        putExtra("PRAYER_NAME", prayer) // تمرير اسم الصلاة للخدمة
    }

    val pendingIntent = PendingIntent.getService(
        context,
        prayer.hashCode(), // Request code فريد
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        // ✅ استخدام setExactAndAllowWhileIdle لضمان عملها في وضع توفير الطاقة
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
        Log.d("AdhanScheduler", "Scheduled $prayer at ${SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(cal.timeInMillis))}")
    } catch (e: SecurityException) {
        // 🚨 التعامل مع خطأ عدم وجود إذن SCHEDULE_EXACT_ALARM
        Log.e("AdhanScheduler", "SecurityException: Cannot schedule exact alarm for $prayer.", e)
        // تنبيه المستخدم إذا كان الإذن غير ممنوح (مطلوب في Android 12+)
        Toast.makeText(context, "الرجاء منح إذن الجدولة الدقيقة لتشغيل الأذان في الخلفية.", Toast.LENGTH_LONG).show()
    }
}

fun cancelAdhanSchedule(context: Context, prayer: String) {
    val intent = Intent(context, AdhanService::class.java)
    val pendingIntent = PendingIntent.getService(
        context,
        prayer.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pendingIntent)
    Log.d("AdhanScheduler", "Canceled schedule for $prayer")
}