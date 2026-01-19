package com.day.mate.ui.theme.navigation

import com.day.mate.R

/**
 * هذا الملف يحتوي فقط على تعريف العناصر (البيانات)
 * ولا يجب أن يحتوي على أي دالة Composable
 */
sealed class BottomNavItem(
    val titleRes: Int,
    val route: String,
    val iconRes: Int,
    val selectedIconRes: Int
) {
    data object TimeLine : BottomNavItem(
        R.string.nav_timeline,
        "timeline",
        R.drawable.ic_timeline_outline,
        R.drawable.ic_timeline_filled
    )

    data object Todo : BottomNavItem(
        R.string.nav_todo,
        "todo",
        R.drawable.ic_todo_outline,
        R.drawable.ic_todo_filled
    )

    data object Pomodoro : BottomNavItem(
        R.string.nav_pomodoro,
        "pomodoro",
        R.drawable.ic_pomodoro_outline,
        R.drawable.ic_pomodoro_filled
    )

    data object Prayer : BottomNavItem(
        R.string.nav_prayer,
        "prayer",
        R.drawable.ic_mosque_outline,
        R.drawable.ic_mosque_filled
    )

    data object Media : BottomNavItem(
        R.string.nav_media,
        "media",
        R.drawable.ic_media_outline,
        R.drawable.ic_media_filled
    )

    data object Settings : BottomNavItem(
        R.string.nav_settings,
        "settings",
        R.drawable.ic_profile_outline,
        R.drawable.ic_profile_filled
    )
}