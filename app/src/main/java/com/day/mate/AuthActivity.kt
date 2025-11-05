package com.day.mate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                                onLoggedIn = { /* Navigate to MainActivity */ },
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
        val viewModel: AuthViewModel = AuthViewModel()
        viewModel.checkCurrentUser()
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null) {
            // TODO: Navigate to MainActivity
        }
    }
}