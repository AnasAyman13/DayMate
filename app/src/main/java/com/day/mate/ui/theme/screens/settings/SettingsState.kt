package com.day.mate.ui.screens.settings

import com.day.mate.data.model.User

/**
 * SettingsState
 *
 * Data class representing the current UI state of the Settings Screen.
 * This state is consumed by the stateless [SettingsScreen] Composable.
 *
 * @property user The currently logged-in user's data.
 * @property darkModeEnabled Boolean state of the dark mode setting.
 * @property cloudSyncEnabled Boolean state of the cloud sync setting (currently unused in the provided UI).
 * @property notificationsEnabled Boolean state of the notifications setting.
 * @property isLoggedOut Boolean flag to trigger navigation away after successful logout.
 * @property loading Boolean flag indicating an ongoing background operation (e.g., fetching user data).
 * @property error Optional string to display if an error occurred.
 */
data class SettingsState(
    val user: User = User(),
    val darkModeEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isLoggedOut: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)