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
import com.day.mate.ui.theme.screens.PomodoroScreen
import com.day.mate.ui.theme.screens.SettingsScreen
import com.day.mate.ui.theme.screens.TimeLineScreen
import com.day.mate.ui.theme.screens.TodoScreen
import com.day.mate.ui.screens.settings.SettingsScreenContainer
import com.day.mate.ui.theme.screens.VaultScreen
import com.day.mate.ui.theme.screens.VaultViewerScreen

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
            composable(BottomNavItem.TimeLine.route) { TimeLineScreen() }
            composable(BottomNavItem.Todo.route) { TodoScreen() }
            composable(BottomNavItem.Pomodoro.route) { PomodoroScreen() }
            composable(BottomNavItem.Media.route) { VaultScreen(navController = navController) }
            composable(BottomNavItem.Prayer.route) { PrayerScreen() }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContainer(onBackClick = { navController.popBackStack() })
            }

            // 🟢 شاشة عرض الملفات
            composable("viewer/{uri}/{type}") { backStackEntry ->
                val uri = backStackEntry.arguments?.getString("uri") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: "PHOTO"
                VaultViewerScreen(navController = navController, uri = uri, type = type)
            }
        }
    }
}