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
 * - Cloud sync settings (local state only)
 * - Notification settings (local state only; directs to system settings for changes)
 * - Authentication operations (logout, password reset)
 *
 * @property context Application context needed for DataStore and Toast messages
 */
class SettingsViewModel(private val context: Context) : ViewModel() {

    // DataStore key for dark mode preference
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    // Internal mutable state that is updated by logic functions
    private val _uiState = MutableStateFlow(SettingsState())

    /**
     * Public immutable state flow for UI observation.
     * Emits the current settings state to all collectors.
     */
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    /**
     * Dark mode state flow.
     * Reads the persistent dark mode setting from DataStore.
     * The initial value is set to true (assuming dark mode by default unless preference says otherwise).
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
                    // Attempt to map the document data to the User model
                    val userData = document.toObject(User::class.java)
                    if (userData != null) {
                        _uiState.value = _uiState.value.copy(
                            user = userData,
                            loading = false
                        )
                    } else {
                        // Document exists but mapping failed
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            error = context.getString(R.string.error_loading_user)
                        )
                    }
                } else {
                    // Document does not exist (e.g., first login, or incomplete setup)
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = context.getString(R.string.error_user_not_found)
                    )
                }
            }
            .addOnFailureListener { e ->
                // Firestore fetch failed
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: context.getString(R.string.error_loading_user)
                )
            }
    }

    /**
     * Toggles dark mode setting and persists the preference to DataStore.
     * This preference is read by the main activity or theme provider to update the UI.
     *
     * @param enabled True to enable dark mode, false to disable
     */
    fun toggleDarkMode(enabled: Boolean) {
        // Update local state immediately for fast UI response
        _uiState.update { it.copy(darkModeEnabled = enabled) }

        viewModelScope.launch {
            try {
                // Persist the new value to DataStore
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
     * Note: This currently only updates local state. Actual sync logic needs implementation.
     *
     * @param enabled True to enable cloud sync, false to disable
     */
    fun toggleCloudSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cloudSyncEnabled = enabled)
    }

    /**
     * Toggles notification setting's local state.
     * Note: Actual notification permissions are managed by the system settings (via the Composable).
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
     * Handles password change/reset request based on the user's login method.
     *
     * Behavior:
     * - Google sign-in: Informs user that they must change the password via Google.
     * - Email/password: Sends a password reset email to the user's registered email.
     *
     * @param context Context needed for SharedPreferences (to check login method) and Toast messages.
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

        // Get the stored login method (assuming it was saved during login)
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