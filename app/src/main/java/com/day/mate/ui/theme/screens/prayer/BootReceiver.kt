package com.day.mate.ui.theme.screens.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.day.mate.ui.screens.getAdhanPref
import java.text.SimpleDateFormat
import java.util.*


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            // نقرأ الأوقات من SharedPreferences (يجب أن يتم تحديثها عند جلب الأوقات من النت)
            val prefs = context.getSharedPreferences("adhan_times", Context.MODE_PRIVATE)
            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

            for (prayer in prayers) {
                val timeStr = prefs.getString(prayer, null) ?: continue

                // تحويل النص إلى Date
                val date = try { sdf.parse(timeStr) } catch (_: Exception) { null } ?: continue

                // إنشاء تقويم بناءً على الوقت المحفوظ
                val cal = Calendar.getInstance().apply {
                    time = date
                }

                // لو المستخدم مفعل الأذان للصلاة دي
                if (getAdhanPref(context, prayer)) {
                    scheduleAdhan( // ✅ استخدام الدالة الجديدة
                        context = context,
                        prayer = prayer,
                        hour = cal.get(Calendar.HOUR_OF_DAY),
                        minute = cal.get(Calendar.MINUTE)
                    )
                }
            }
        }
    }
}