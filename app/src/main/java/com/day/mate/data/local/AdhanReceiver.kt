package com.day.mate.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.day.mate.services.AdhanService

// هذا الكلاس يستقبل إشارة المنبه (Alarm) من AlarmManager
class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Log للتأكد من وصول الإشارة
        Log.d("AdhanReceiver", "Broadcast received. Starting AdhanService.")

        // 1. استخلاص اسم الصلاة الذي تم تمريره من دالة scheduleAdhan
        val prayerName = intent.getStringExtra("prayer") // نستخدم مفتاح "prayer" للاستقبال من Scheduler

        // 2. إنشاء نية (Intent) لاستهداف الخدمة الأمامية (AdhanService)
        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            // ✅ التصحيح: نمرر الاسم باستخدام مفتاح "PRAYER_NAME" ليتطابق مع AdhanService
            putExtra("PRAYER_NAME", prayerName)
        }

        // إطلاق الخدمة الأمامية (Foreground Service) لتشغيل الأذان
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}