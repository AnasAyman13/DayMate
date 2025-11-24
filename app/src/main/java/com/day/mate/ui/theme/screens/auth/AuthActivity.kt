package com.day.mate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
                authViewModel.firebaseAuthWithGoogle(idToken,this)
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

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = "signin"
                    ) {
                        composable("signin") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoggedIn = {
                                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                },
                                onGoogleSignInClicked = { startGoogleSignIn() },
                                onNavigateToSignUp = { navController.navigate("signup") },
                                onForgotPassword = { }
                            )
                        }

                        composable("signup") {
                            SignUpScreen(
                                viewModel = authViewModel,
                                onSignedUp = { },
                                onNavigateToSignIn = { navController.popBackStack() }
                            )
                        }
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

    override fun onStart() {
        super.onStart()
        val currentUser = authViewModel.getCurrentUser()

        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
