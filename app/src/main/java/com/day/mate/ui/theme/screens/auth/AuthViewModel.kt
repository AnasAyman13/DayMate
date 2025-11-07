package com.day.mate.ui.theme.screens.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.day.mate.MainActivity
import com.day.mate.data.authUiState.AuthUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val state: StateFlow<AuthUiState> = _state

    fun signIn(context: Context, email: String, password: String) {
        _state.value = AuthUiState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(
                            context,
                            "Welcome back!",
                            Toast.LENGTH_SHORT
                        ).show()
                        _state.value = AuthUiState.Success

                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Please verify your email before signing in.",
                            Toast.LENGTH_LONG
                        ).show()
                        auth.signOut()
                        _state.value = AuthUiState.Error("Email not verified")
                    }
                } else {
                    Toast.makeText(
                        context,
                        task.exception?.message ?: "Sign-in failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    _state.value = AuthUiState.Error(task.exception?.message ?: "Sign-in failed")
                }
            }
    }


    fun signUp(context: Context, email: String, password: String) {
        _state.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    //  Send email verification
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Verification email sent! Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                _state.value = AuthUiState.Success
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                _state.value = AuthUiState.Error(
                                    verifyTask.exception?.message ?: "Failed to send verification email"
                                )
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        task.exception?.message ?: "Sign-up failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    _state.value = AuthUiState.Error(task.exception?.message ?: "Sign-up failed")
                }
            }
    }
    fun getCurrentUser() = auth.currentUser

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _state.value = AuthUiState.Success
        } else {
            _state.value = AuthUiState.Idle
        }
    }

}
