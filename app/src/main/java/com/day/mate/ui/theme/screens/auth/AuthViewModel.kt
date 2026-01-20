package com.day.mate.viewmodel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.day.mate.MainActivity
import com.day.mate.data.authUiState.AuthUiState
import com.day.mate.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var googleSignInClient: GoogleSignInClient

    private fun saveLoginMethod(context: Context, method: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("login_method", method).apply()
    }

    // --- Google Sign-In Functions ---
    fun initGoogleClient(context: Context, webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        if (!::googleSignInClient.isInitialized) {
            throw IllegalStateException("GoogleSignInClient must be initialized via initGoogleClient first.")
        }
        return googleSignInClient.signInIntent
    }

    fun firebaseAuthWithGoogle(idToken: String, context: Context) {
        _state.value = AuthUiState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveLoginMethod(context, "google")
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    _state.value = AuthUiState.Error("Google login failed: no user")
                    return@addOnCompleteListener
                }

                val userId = firebaseUser.uid
                val name = firebaseUser.displayName ?: "Unknown"
                val email = firebaseUser.email ?: ""

                val userData = User(
                    id = userId,
                    name = name,
                    email = email
                )

                db.collection("users")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        _state.value = AuthUiState.Success
                    }
                    .addOnFailureListener { e ->
                        _state.value = AuthUiState.Error("Firestore error: ${e.message}")
                    }

            } else {
                val msg = task.exception?.message ?: "Google Sign-in failed"
                _state.value = AuthUiState.Error(msg)
            }
        }
    }


    fun googleSignOut(onComplete: () -> Unit = {}) {
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut().addOnCompleteListener { onComplete() }
        } else {
            onComplete()
        }
    }

    //helper function to check email is exist
    fun checkEmailExistsInFirestore(
        context: Context,
        email: String,
        onResult: (Boolean) -> Unit
    ) {
        if (email.isBlank()) {
            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onResult(!querySnapshot.isEmpty)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    exception.localizedMessage ?: "Error checking email",
                    Toast.LENGTH_SHORT
                ).show()
                onResult(false)
            }
    }



    // --- Email & Password Sign-In ---
    fun signIn(context: Context, email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        _state.value = AuthUiState.Loading

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    saveLoginMethod(context, "password")
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    _state.value = AuthUiState.Success

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

    // --- Email & Password Sign-Up ---
    fun signUp(context: Context, name: String, email: String, password: String,confirmPass:String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()||confirmPass.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if(password!=confirmPass){
            Toast.makeText(context, "Please match your password with confirm password", Toast.LENGTH_SHORT).show()
            return
        }
        _state.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val firebaseUser = auth.currentUser
                    if (firebaseUser == null) {
                        _state.value = AuthUiState.Error("User not found after sign up")
                        return@addOnCompleteListener
                    }

                    val userId = firebaseUser.uid

                    val userData = User(
                        id = userId,
                        name = name,
                        email = email
                    )

                    db.collection("users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener {

                            firebaseUser.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Verification email sent! Please check your inbox, then sign in manually.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    auth.signOut()

                                    _state.value = AuthUiState.Idle
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    auth.signOut()

                                    _state.value = AuthUiState.Error(
                                        e.message ?: "Failed to send verification email"
                                    )
                                }

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Failed to save user data.",
                                Toast.LENGTH_SHORT
                            ).show()

                            auth.signOut()

                            _state.value = AuthUiState.Error(
                                e.message ?: "Failed to save user data"
                            )
                        }

                } else {
                    val msg = task.exception?.message ?: "Sign-up failed"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    _state.value = AuthUiState.Error(msg)
                }
            }
    }




    // --- Reset Password ---
    fun resetPassword(context: Context, email: String) {

        if (email.isBlank()) {
            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }
        checkEmailExistsInFirestore(context, email) { exists ->
            if (!exists) {
                Toast.makeText(context, "Email not found", Toast.LENGTH_SHORT).show()
            } else {
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
        }


    }
    fun resetState() {
        _state.value = AuthUiState.Idle
    }

//    // --- Sign Out ---
//    fun signOut() {
//        auth.signOut()
//        _state.value = AuthUiState.Idle
//    }

    // --- Current User ---
    fun getCurrentUser() = auth.currentUser
}
