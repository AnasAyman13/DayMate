package com.day.mate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.day.mate.ui.screens.LoginScreen
import com.day.mate.ui.screens.SignUpScreen
import com.day.mate.viewmodel.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
// **Imports Google Sign-In المصححة**
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
// **Import R لحل مشكلة web_client_id**
import com.day.mate.R
// *************************************

class AuthActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val callbackManager = CallbackManager.Factory.create()

    companion object {
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)


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
                                onForgotPassword = { /* Handle forgot password */ }
                            )
                        }

                        composable("signup") {
                            SignUpScreen(
                                viewModel = authViewModel,
                                onSignedUp = { /* Navigate to MainActivity or show success */ },
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
            startActivityForResult(signInIntent, RC_SIGN_IN)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // حل مشكلة Unresolved reference 'GoogleSignIn'
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                account.idToken?.let {
                    authViewModel.firebaseAuthWithGoogle(it)
                } ?: run {
                    println("Google Sign-In failed: ID Token is null.")
                }

            } catch (e: ApiException) {
                val statusCode = e.statusCode
                println("Google Sign-In failed: Status Code $statusCode, Error: ${e.message}")
            } catch (e: Exception) {
                println("Google Sign-In failed: Unknown Error: ${e.message}")
            }
        }
    }
}