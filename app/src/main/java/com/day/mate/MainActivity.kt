package com.day.mate

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.day.mate.data.local.prayer.AppDatabase
import com.day.mate.data.local.reminder.ReminderConstants
import com.day.mate.data.local.reminder.ReminderScheduler
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.components.GlobalLoadingOverlay
import com.day.mate.ui.screens.AuthNavGraph
import com.day.mate.ui.screens.settings.SettingsViewModel
import com.day.mate.ui.screens.settings.SettingsViewModelFactory
import com.day.mate.ui.theme.DayMateTheme
import com.day.mate.ui.theme.screens.media.MainNavGraph
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory
import com.day.mate.util.LocaleUtils
import com.day.mate.utils.LoadingManager
import com.day.mate.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var todoRepository: TodoRepository

    private val todoViewModel: TodoViewModel by viewModels {
        TodoViewModelFactory(todoRepository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(applicationContext)
    }

    private val authViewModel: AuthViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeUpdated = newBase?.let { LocaleUtils.applySavedLocale(it) }
        super.attachBaseContext(localeUpdated)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ شفافية شريط الحالة والتنقل
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }
        }

        // ✅ Immersive Mode
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            val isDark =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        // --- Database & Repository ---
        val db = AppDatabase.getInstance(this)
        todoRepository = TodoRepository(
            todoDao = db.todoDao(),
            categoryDao = db.categoryDao()
        )
        todoViewModel.initReminderScheduler(this)

        // --- Permissions ---
        setupPermissions()

        val scheduler = ReminderScheduler(applicationContext)
        scheduler.scheduleDailyReminder(hour = 10, minute = 0)

        val startDestination =
            intent.getStringExtra(ReminderConstants.EXTRA_NAVIGATION_DESTINATION)

        // --- UI ---
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            var isLoggedIn by remember {
                mutableStateOf(authViewModel.getCurrentUser() != null)
            }

            // ✅ Global Loading State
            val isLoading by LoadingManager.isLoading.collectAsState()

            DayMateTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {

                    if (isLoggedIn) {
                        MainNavGraph(startRouteFromIntent = startDestination)
                    } else {
                        AuthNavGraph(
                            viewModel = authViewModel,
                            onAuthDone = { isLoggedIn = true }
                        )
                    }

                    // ✅ Global Loading Overlay (اللوجو بيلف)
                    if (isLoading) {
                        GlobalLoadingOverlay()
                    }
                }
            }
        }
    }

    // ---------------- Permissions ----------------

    private fun setupPermissions() {
        // إشعارات Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    101
                )
            }
        }

        // Exact Alarm Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                if (!prefs.getBoolean("exact_alarm_asked", false)) {
                    openExactAlarmSettings()
                    prefs.edit().putBoolean("exact_alarm_asked", true).apply()
                }
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
