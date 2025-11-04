package com.day.mate.ui.theme.navigation

import com.day.mate.R

sealed class BottomNavItem(
    val titleRes: Int,
    val route: String,
    val iconRes: Int,
    val selectedIconRes: Int
) {
    object TimeLine : BottomNavItem(
        R.string.nav_timeline,
        "timeline",
        R.drawable.ic_timeline_outline,
        R.drawable.ic_timeline_filled
    )

    object Todo : BottomNavItem(
        R.string.nav_todo,
        "todo",
        R.drawable.ic_todo_outline,
        R.drawable.ic_todo_filled
    )

    object Pomodoro : BottomNavItem(
        R.string.nav_pomodoro,
        "pomodoro",
        R.drawable.ic_pomodoro_outline,
        R.drawable.ic_pomodoro_filled
    )

    object Prayer : BottomNavItem(
        R.string.nav_prayer,
        "prayer",
        R.drawable.ic_mosque_outline,
        R.drawable.ic_mosque_filled
    )

    object Media : BottomNavItem(
        R.string.nav_media,
        "media",
        R.drawable.ic_media_outline,
        R.drawable.ic_media_filled
    )

    object Settings : BottomNavItem(
        R.string.nav_settings,
        "settings",
        R.drawable.ic_profile_outline,
        R.drawable.ic_profile_filled
    )
}
