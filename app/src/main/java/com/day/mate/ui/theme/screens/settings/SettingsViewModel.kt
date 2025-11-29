package com.day.mate.ui.screens.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.R
import com.day.mate.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * DataStore extension for storing user preferences.
 * This creates a DataStore instance named "settings" for the application context.
 */
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * SettingsViewModel
 *
 * Manages the settings screen state and user preferences including:
 * - User profile data from Firestore
 * - Dark mode toggle with persistent storage
 * - Cloud sync settings
 * - Notification settings
 * - Authentication (logout, password reset)
 *
 * @property context Application context needed for DataStore and Toast messages
 */
class SettingsViewModel(private val context: Context) : ViewModel() {

    // DataStore key for dark mode preference
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    // Internal mutable state
    private val _uiState = MutableStateFlow(SettingsState())

    /**
     * Public immutable state flow for UI observation.
     * Emits the current settings state to all collectors.
     */
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    /**
     * Dark mode state flow.
     * Reads from DataStore and provides real-time updates when dark mode changes.
     * Default value is true (dark mode enabled).
     */
    val isDarkMode: StateFlow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        // Load user data and dark mode preference on initialization
        loadUser()
        loadDarkModePreference()
    }

    /**
     * Loads the saved dark mode preference from DataStore
     * and updates the UI state accordingly.
     */
    private fun loadDarkModePreference() {
        viewModelScope.launch {
            isDarkMode.collect { darkMode ->
                _uiState.update { it.copy(darkModeEnabled = darkMode) }
            }
        }
    }

    /**
     * Loads the current user's profile data from Firestore.
     * Retrieves user document using Firebase Auth UID.
     *
     * Updates UI state with:
     * - Loading indicator while fetching
     * - User data on success
     * - Error message on failure
     */
    fun loadUser() {
        val currentUser = auth.currentUser ?: return

        _uiState.value = _uiState.value.copy(loading = true)

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    if (userData != null) {
                        _uiState.value = _uiState.value.copy(
                            user = userData,
                            loading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = context.getString(R.string.error_user_not_found)
                    )
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: context.getString(R.string.error_loading_user)
                )
            }
    }

    /**
     * Toggles dark mode setting and persists the preference to DataStore.
     * This triggers a recomposition of the entire app theme.
     *
     * @param enabled True to enable dark mode, false to disable
     */
    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkModeEnabled = enabled) }

        viewModelScope.launch {
            try {
                context.dataStore.edit { preferences ->
                    preferences[DARK_MODE_KEY] = enabled
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error saving dark mode preference", e)
            }
        }
    }

    /**
     * Toggles cloud sync setting.
     * Note: This currently only updates local state.
     * TODO: Implement actual cloud sync logic
     *
     * @param enabled True to enable cloud sync, false to disable
     */
    fun toggleCloudSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cloudSyncEnabled = enabled)
    }

    /**
     * Toggles notification setting.
     * Note: This currently only updates local state.
     * Actual notification permissions are handled by the system settings.
     *
     * @param enabled True to enable notifications, false to disable
     */
    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    /**
     * Signs out the current user from Firebase Auth.
     * Updates UI state to trigger navigation to login screen.
     */
    fun onLogoutClicked() {
        viewModelScope.launch {
            auth.signOut()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }

    /**
     * Handles password change/reset request based on login method.
     *
     * Behavior:
     * - For Google sign-in users: Shows a toast message indicating they use Google
     * - For email/password users: Sends a password reset email via Firebase Auth
     * - For unknown methods: Shows an error toast
     *
     * @param context Context needed for SharedPreferences and Toast messages
     */
    fun onChangePasswordClicked(context: Context) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(
                context,
                context.getString(R.string.error_no_user_logged_in),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val method = prefs.getString("login_method", "unknown")

        when (method) {
            "google" -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.info_signed_in_with_google),
                    Toast.LENGTH_LONG
                ).show()
            }
            "password" -> {
                val email = user.email
                if (email != null) {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                context.getString(R.string.success_password_reset_email_sent, email),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_prefix, e.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            else -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_unable_detect_login_method),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}