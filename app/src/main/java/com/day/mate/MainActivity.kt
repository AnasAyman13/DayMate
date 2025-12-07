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
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.day.mate.data.local.prayer.AppDatabase
import com.day.mate.data.local.reminder.ReminderConstants
import com.day.mate.data.local.reminder.ReminderScheduler
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.screens.settings.SettingsViewModel
import com.day.mate.ui.screens.settings.SettingsViewModelFactory
import com.day.mate.ui.theme.DayMateTheme
import com.day.mate.ui.theme.screens.media.MainNavGraph
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory
import com.day.mate.util.LocaleUtils

/**
 * MainActivity
 *
 * Main entry point of the application.
 * Handles:
 * - Locale management (Arabic/English)
 * - Permission requests (Location, Notifications, Exact Alarms)
 * - Database and repository initialization
 * - Theme management (Dark/Light mode)
 * - Daily reminder scheduling
 */
class MainActivity : AppCompatActivity() {

    private lateinit var todoRepository: TodoRepository

    /**
     * TodoViewModel instance created using custom factory.
     * Manages todo items and categories.
     */
    private val todoViewModel: TodoViewModel by viewModels {
        TodoViewModelFactory(todoRepository)
    }

    /**
     * SettingsViewModel instance for managing app settings.
     * Requires application context for DataStore operations.
     */
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(applicationContext)
    }

    /**
     * Applies saved locale before creating the activity.
     * This ensures the correct language is set before any UI is created.
     *
     * @param newBase The new base context
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
    }
    override fun attachBaseContext(newBase: Context?) {
        val localeUpdated = newBase?.let { LocaleUtils.applySavedLocale(it) }
        super.attachBaseContext(localeUpdated)
    }

    /**
     * Activity creation callback.
     * Initializes all components and sets up the Compose UI.
     *
     * @param savedInstanceState Saved state from previous instance
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = intent.getStringExtra(ReminderConstants.EXTRA_NAVIGATION_DESTINATION)
        val scheduler = ReminderScheduler(applicationContext)
        scheduler.scheduleDailyReminder(hour = 10, minute = 0)
        val db = AppDatabase.getInstance(this)
        todoRepository = TodoRepository(
            todoDao = db.todoDao(),
            categoryDao = db.categoryDao()
        )
        todoViewModel.initReminderScheduler(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val hasAskedBefore = prefs.getBoolean("exact_alarm_asked", false)

                if (!hasAskedBefore) {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_exact_alarms_request),
                        Toast.LENGTH_LONG
                    ).show()
                    openExactAlarmSettings()
                    prefs.edit().putBoolean("exact_alarm_asked", true).apply()
                }
            }
        }
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            DayMateTheme(darkTheme = isDarkMode) {
                MainNavGraph(startRouteFromIntent = startDestination)
            }
        }
    }

    /**
     * Opens system settings for exact alarm permission.
     * Only available on Android 12 (S) and above.
     */
    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

}
