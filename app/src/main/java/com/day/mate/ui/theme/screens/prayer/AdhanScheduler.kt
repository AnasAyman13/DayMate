package com.day.mate.ui.theme.screens.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * جدولة الأذان باستخدام AlarmManager
 * @param context السياق (Context)
 * @param prayer اسم الصلاة (Fajr, Dhuhr, Asr, Maghrib, Isha)
 * @param hour الساعة (24-hour format)
 * @param minute الدقيقة
 */
fun scheduleAdhan(context: Context, prayer: String, hour: Int, minute: Int) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 1. التحقق من إذن الجدولة الدقيقة (Android 12+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!am.canScheduleExactAlarms()) {
            Log.w("AdhanScheduler", "⚠️ Cannot schedule $prayer. Exact Alarm permission missing.")
            return
        }
    }

    // 2. إعداد الوقت المطلوب
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // إذا كان الوقت قد مر اليوم، جدوله للغد
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    // 3. إنشاء Intent للـ BroadcastReceiver
    val intent = Intent(context, AdhanReceiver::class.java).apply {
        putExtra("PRAYER_NAME", prayer)
    }

    // 4. إنشاء PendingIntent
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        prayer.hashCode(), // Request code فريد لكل صلاة
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 5. جدولة المنبه
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // استخدام setExactAndAllowWhileIdle للعمل حتى في وضع Doze
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }

        val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
        Log.d("AdhanScheduler", "✅ Scheduled $prayer at ${sdf.format(Date(cal.timeInMillis))}")

    } catch (e: SecurityException) {
        Log.e("AdhanScheduler", "❌ SecurityException during scheduling $prayer", e)
    }
}

/**
 * إلغاء جدولة الأذان
 * @param context السياق (Context)
 * @param prayer اسم الصلاة
 */
fun cancelAdhanSchedule(context: Context, prayer: String) {
    val intent = Intent(context, AdhanReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        prayer.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pendingIntent)
    pendingIntent.cancel()

    Log.d("AdhanScheduler", "❌ Canceled schedule for $prayer")
}

/**
 * التحقق من إذن الجدولة الدقيقة
 * @param context السياق (Context)
 * @return true إذا كان الإذن ممنوحاً أو غير مطلوب
 */
fun checkExactAlarmPermission(context: Context): Boolean {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        am.canScheduleExactAlarms()
    } else {
        true // الإصدارات القديمة لا تحتاج إلى هذا الإذن
    }
}

/**
 * حفظ حالة تفعيل الأذان في SharedPreferences
 */
fun saveAdhanPref(context: Context, prayer: String, enabled: Boolean) {
    val prefs = context.getSharedPreferences("adhan_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean(prayer, enabled).apply()
    Log.d("AdhanScheduler", "💾 Saved pref: $prayer = $enabled")
}

/**
 * قراءة حالة تفعيل الأذان من SharedPreferences
 */
fun getAdhanPref(context: Context, prayer: String): Boolean {
    val prefs = context.getSharedPreferences("adhan_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean(prayer, false)
}