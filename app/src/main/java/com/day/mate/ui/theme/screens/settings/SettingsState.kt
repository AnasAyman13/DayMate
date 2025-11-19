package com.day.mate.ui.screens.settings

import com.day.mate.data.model.User


data class SettingsState(
    val user: User = User(),
    val darkModeEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean  = false
)
