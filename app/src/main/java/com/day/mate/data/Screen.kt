package com.day.mate.data

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Stats : Screen("stats")
    data object Goals : Screen("goals")
    data object Profile : Screen("profile")
}