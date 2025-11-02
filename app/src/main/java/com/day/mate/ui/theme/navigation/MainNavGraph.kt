package com.day.mate.ui.theme.navigation



import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.day.mate.ui.theme.Components.BottomNavigationBar
import com.day.mate.ui.theme.screens.PomodoroScreen
import com.day.mate.ui.theme.screens.PrayerScreen
import com.day.mate.ui.theme.screens.SettingsScreen
import com.day.mate.ui.theme.screens.TimeLineScreen
import com.day.mate.ui.theme.screens.TodoScreen

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    androidx.compose.material3.Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TimeLine.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.TimeLine.route) { TimeLineScreen() }
            composable(BottomNavItem.Todo.route) { TodoScreen() }
            composable(BottomNavItem.Pomodoro.route) { PomodoroScreen() }
            composable(BottomNavItem.Settings.route) { SettingsScreen() }
            composable(BottomNavItem.Prayer.route) { PrayerScreen() }
        }
    }
}
