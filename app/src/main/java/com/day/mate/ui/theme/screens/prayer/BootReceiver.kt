package com.day.mate.ui.theme.screens.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d("BootReceiver", "Device booted. Rescheduling all prayers.")

            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val prefs = context.getSharedPreferences("adhan_times", Context.MODE_PRIVATE)
            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

            for (prayer in prayers) {
                val timeStr = prefs.getString(prayer, null)
                if (timeStr.isNullOrEmpty()) {
                    Log.w("BootReceiver", "No time found for $prayer, skipping.")
                    continue
                }

                // تحويل النص إلى Date
                val date = try {
                    sdf.parse(timeStr)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to parse time for $prayer: $timeStr", e)
                    null
                } ?: continue

                // إنشاء تقويم بناءً على الوقت المحفوظ
                val cal = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    // ✅ إذا كان الوقت قد مر اليوم، حدده للغد
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                        Log.d("BootReceiver", "$prayer time has passed today, scheduling for tomorrow.")
                    }
                }

                // لو المستخدم مفعل الأذان للصلاة دي
                if (getAdhanPref(context, prayer)) {
                    scheduleAdhan(
                        context = context,
                        prayer = prayer,
                        hour = cal.get(Calendar.HOUR_OF_DAY),
                        minute = cal.get(Calendar.MINUTE)
                    )
                    Log.d("BootReceiver", "Rescheduled $prayer at ${sdf.format(cal.time)}")
                } else {
                    Log.d("BootReceiver", "Adhan disabled for $prayer, skipping.")
                }
            }
        }
    }
}