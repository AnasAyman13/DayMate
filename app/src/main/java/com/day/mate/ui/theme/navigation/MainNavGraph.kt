package com.day.mate.ui.theme.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.day.mate.ui.screens.PrayerScreen
import com.day.mate.ui.theme.Components.BottomNavigationBar
import com.day.mate.ui.theme.screens.MediaScreen
import com.day.mate.ui.theme.screens.PomodoroScreen
import com.day.mate.ui.theme.screens.SettingsScreen
import com.day.mate.ui.theme.screens.TimeLineScreen
import com.day.mate.ui.theme.screens.TodoScreen
import com.day.mate.ui.screens.settings.SettingsScreenContainer

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TimeLine.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 🟢 Home / Timeline
            composable(BottomNavItem.TimeLine.route) {
                TimeLineScreen()
            }

            // 🟢 To-Do
            composable(BottomNavItem.Todo.route) {
                TodoScreen()
            }

            // 🟢 Pomodoro
            composable(BottomNavItem.Pomodoro.route) {
                PomodoroScreen()
            }

            // 🟢 Media
            composable(BottomNavItem.Media.route) {
                MediaScreen()
            }

            // 🟢 Prayer
            composable(BottomNavItem.Prayer.route) {
                PrayerScreen()
            }

            // 🟢 Settings
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContainer(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
