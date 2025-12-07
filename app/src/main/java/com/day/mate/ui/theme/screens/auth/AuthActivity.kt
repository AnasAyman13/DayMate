package com.day.mate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.day.mate.ui.screens.LoginScreen
import com.day.mate.ui.screens.SignUpScreen
import com.day.mate.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class AuthActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.firebaseAuthWithGoogle(idToken, this)
            } else {
                println("Google Sign-In failed: ID Token is null")
            }
        } catch (e: Exception) {
            println("Google Sign-In error: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webClientId = getString(R.string.web_client_id)
        authViewModel.initGoogleClient(this, webClientId)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var showForgotPasswordDialog by remember { mutableStateOf(false) }

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = "signin"
                    ) {
                        composable("signin") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoggedIn = {
                                    navigateToMainActivity()
                                },
                                onNavigateToSignUp = { navController.navigate("signup") },
                                onForgotPassword = { showForgotPasswordDialog = true },
                                onGoogleSignInClicked = { startGoogleSignIn() }
                            )
                        }

                        composable("signup") {
                            SignUpScreen(
                                viewModel = authViewModel,
                                onSignedUp = {
                                    // عند التسجيل بـ Google ينتقل للـ MainActivity
                                    navigateToMainActivity()
                                },
                                onNavigateToSignIn = { navController.popBackStack() }
                            )
                        }
                    }

                    // Forgot Password Dialog
                    if (showForgotPasswordDialog) {
                        ForgotPasswordDialog(
                            onDismiss = { showForgotPasswordDialog = false },
                            onConfirm = { email ->
                                authViewModel.resetPassword(this@AuthActivity, email)
                                showForgotPasswordDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        authViewModel.googleSignOut {
            val signInIntent = authViewModel.getGoogleSignInIntent()
            googleLauncher.launch(signInIntent)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@AuthActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = authViewModel.getCurrentUser()

        if (currentUser != null && currentUser.isEmailVerified) {
            navigateToMainActivity()
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (email.isNotBlank()) {
                                onConfirm(email)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = email.isNotBlank()
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}