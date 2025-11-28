package com.day.mate

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.day.mate.data.local.AppDatabase
import com.day.mate.data.local.reminder.ReminderScheduler
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.theme.DayMateTheme
import com.day.mate.ui.theme.screens.media.MainNavGraph
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory
import com.day.mate.util.LocaleUtils

class MainActivity : AppCompatActivity() {

    private lateinit var todoRepository: TodoRepository

    private val todoViewModel: TodoViewModel by viewModels {
        TodoViewModelFactory(todoRepository)
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeUpdated = newBase?.let { LocaleUtils.applySavedLocale(it) }
        super.attachBaseContext(localeUpdated)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduler = ReminderScheduler(applicationContext)
        scheduler.scheduleDailyReminder(hour = 10, minute = 0)
        val db = AppDatabase.getInstance(this)
        todoRepository = TodoRepository(
            todoDao = db.todoDao(),
            categoryDao = db.categoryDao()
        )
        todoViewModel.initReminderScheduler(this)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // 🔔 طلب صلاحية الإشعارات (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            // التحقق من الإذن أولاً
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // التحقق إذا كنا طلبنا الإذن قبل كده ولا لأ
                if (!shouldShowRequestPermissionRationale(permission)) {
                    // أول مرة يطلب الإذن
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
                }
            }
        }

        // ⏰ التحقق من صلاحية المنبه الدقيق (Android 12+) - مرة واحدة فقط
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // التحقق إذا طلبنا الإذن قبل كده من SharedPreferences
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val hasAskedBefore = prefs.getBoolean("exact_alarm_asked", false)

                if (!hasAskedBefore) {
                    Toast.makeText(this, "Please Allow Exact Alarms for reminders.", Toast.LENGTH_LONG).show()
                    openExactAlarmSettings()
                    // حفظ إننا طلبنا الإذن
                    prefs.edit().putBoolean("exact_alarm_asked", true).apply()
                }
            }
        }

        // 🎨 واجهة التطبيق
        setContent {
            DayMateTheme {
                MainNavGraph()
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
}