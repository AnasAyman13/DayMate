package com.day.mate.ui.theme.screens.media

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.plcoding.biometricauth.BiometricPromptManager

@Composable
fun BiometricLockScreen(navController: NavController) {
    val context = LocalContext.current

    // Use your existing BiometricPromptManager
    val promptManager = remember { BiometricPromptManager(context as AppCompatActivity) }
    val biometricResult by promptManager.promptResults.collectAsState(initial = null)

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            println("Activity result: $it")
            // After returning from settings, try biometric again
            promptManager.showBiometricPrompt(
                title = "Unlock Media Vault",
                description = "Authenticate to access your private media files"
            )
        }
    )

    // Handle biometric results and auto-redirect to settings
    LaunchedEffect(biometricResult) {
        when (biometricResult) {
            is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    enrollLauncher.launch(enrollIntent)
                }
            }
            is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                errorMessage = (biometricResult as BiometricPromptManager.BiometricResult.AuthenticationError).error
            }
            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                errorMessage = "Authentication failed. Please try again."
            }
            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                // Navigate to media vault on success
                navController.navigate("media_vault") {
                    popUpTo("media_biometric") { inclusive = true }
                }
            }
            BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                errorMessage = "Biometric feature unavailable"
            }
            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                errorMessage = "Biometric hardware unavailable"
            }
            else -> {
                // Initial state or other states
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101F22))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color(0xFF4DB6AC),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Media Vault Locked",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Authenticate to access your private media",
                color = Color.White.copy(alpha = 0.7f)
            )

            // Show error message if any
            errorMessage?.let { message ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color.Red,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Show current biometric status
            biometricResult?.let { result ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when(result) {
                        is BiometricPromptManager.BiometricResult.AuthenticationError -> "Error occurred"
                        BiometricPromptManager.BiometricResult.AuthenticationFailed -> "Authentication failed"
                        BiometricPromptManager.BiometricResult.AuthenticationNotSet -> "Setting up biometric..."
                        BiometricPromptManager.BiometricResult.AuthenticationSuccess -> "Success! Redirecting..."
                        BiometricPromptManager.BiometricResult.FeatureUnavailable -> "Feature unavailable"
                        BiometricPromptManager.BiometricResult.HardwareUnavailable -> "Hardware unavailable"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    errorMessage = null
                    promptManager.showBiometricPrompt(
                        title = "Unlock Media Vault",
                        description = "Authenticate to access your private media files"
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4DB6AC)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Unlock with Biometrics",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Go Back",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}