package com.day.mate.ui.theme.screens.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.day.mate.MainActivity
import com.day.mate.ui.screens.LoginScreen
import com.day.mate.ui.screens.SignUpScreen
import com.day.mate.viewmodel.AuthViewModel

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: AuthViewModel = viewModel()

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = "signin"
                    ) {
                        composable("signin") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoggedIn =  {
                                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                },
                                onNavigateToSignUp = { navController.navigate("signup") },
                                onForgotPassword = { /* Handle forgot password */ }
                            )
                        }


                        composable("signup") {
                            SignUpScreen(
                                viewModel = viewModel,
                                onSignedUp = { /* Navigate to MainActivity */ },
                                onNavigateToSignIn = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val viewModel: AuthViewModel by viewModels()
        val currentUser = viewModel.getCurrentUser()

        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {

        }
    }
}