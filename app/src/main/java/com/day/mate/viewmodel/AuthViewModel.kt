package com.day.mate.viewmodel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.day.mate.MainActivity
import com.day.mate.data.authUiState.AuthUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- Google Sign-In
    fun handleGoogleSignIn(account: GoogleSignInAccount, onResult: (AuthUiState) -> Unit) {
        _state.value = AuthUiState.Loading
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _state.value = AuthUiState.Success
                onResult(AuthUiState.Success)
            } else {
                val msg = task.exception?.message ?: "Google Sign-in failed"
                _state.value = AuthUiState.Error(msg)
                onResult(AuthUiState.Error(msg))
            }
        }
    }
    private lateinit var googleSignInClient: GoogleSignInClient
    fun initGoogleClient(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1052155530713-ejd10bvrcjlr5o4b452dtu454dnrte68.apps.googleusercontent.com") // Web client ID
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun googleSignOut(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1052155530713-ejd10bvrcjlr5o4b452dtu454dnrte68.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut()
    }
    fun signIn(context: Context, email: String, password: String) {
        _state.value = AuthUiState.Loading

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    _state.value = AuthUiState.Success

                    // انتقل مباشرة لـ MainActivity
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Please verify your email before signing in.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    _state.value = AuthUiState.Error("Email not verified")
                }
            } else {
                Toast.makeText(context, task.exception?.message ?: "Sign-in failed", Toast.LENGTH_SHORT).show()
                _state.value = AuthUiState.Error(task.exception?.message ?: "Sign-in failed")
            }
        }
    }

    // --- Email & Password Sign-Up
    fun signUp(context: Context, email: String, password: String) {
        _state.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Toast.makeText(context, "Verification email sent!", Toast.LENGTH_LONG).show()
                            _state.value = AuthUiState.Success
                        } else {
                            Toast.makeText(context, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                            _state.value = AuthUiState.Error(verifyTask.exception?.message ?: "Failed to send verification email")
                        }
                    }
                } else {
                    Toast.makeText(context, task.exception?.message ?: "Sign-up failed", Toast.LENGTH_SHORT).show()
                    _state.value = AuthUiState.Error(task.exception?.message ?: "Sign-up failed")
                }
            }
    }


    // --- Reset Password
    fun resetPassword(context: Context, email: String) {
        if (email.isBlank()) {
            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = task.exception?.message ?: "Invalid email"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- Sign Out
    fun signOut() {
        auth.signOut()
        _state.value = AuthUiState.Idle
    }

    // --- Current User
    fun getCurrentUser() = auth.currentUser
}
