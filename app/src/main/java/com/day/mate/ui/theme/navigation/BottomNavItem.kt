package com.day.mate.ui.theme.navigation

import androidx.annotation.DrawableRes
import com.day.mate.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int
) {
    object TimeLine : BottomNavItem("timeline", "TimeLine", R.drawable.timeline)
    object Todo : BottomNavItem("todo", "To-Do", R.drawable.todolist)
    object Pomodoro : BottomNavItem("pomodoro", "Pomodoro", R.drawable.promodoro)
    object Prayer : BottomNavItem("prayer", "Prayer", R.drawable.prayer)
    object Settings : BottomNavItem("settings", "Settings", R.drawable.settings)
}
