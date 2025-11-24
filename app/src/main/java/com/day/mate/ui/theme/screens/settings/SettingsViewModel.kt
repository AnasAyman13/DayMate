package com.day.mate.ui.screens.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
                        error = "User not found"
                    )
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error loading user"
                )
            }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
    }

    fun toggleCloudSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cloudSyncEnabled = enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            auth.signOut()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }
    fun onChangePasswordClicked(context: Context) {

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "No user is currently logged in.", Toast.LENGTH_LONG).show()
            return
        }
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val method = prefs.getString("login_method", "unknown")

        when (method) {
            "google" -> { Toast.makeText(context, "You are signed in with Google!", Toast.LENGTH_LONG).show() }
            "password" -> {
                val email = user.email
                if (email != null) {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener { Toast.makeText(context, "A password reset email has been sent to $email.", Toast.LENGTH_LONG).show() }
                }
            }

            else -> { Toast.makeText(context, "Unable to detect your login method.", Toast.LENGTH_LONG).show() }
        }
    }
}
