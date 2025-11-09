package com.day.mate.ui.screens

import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.authUiState.AuthUiState
import com.day.mate.viewmodel.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignedUp: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val backgroundDark = Color(0xFF102022)
    val primaryColor = Color(0xFF13DAEC)
    val context = LocalContext.current

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.firebaseAuthWithGoogle(it) } ?: run {
                Toast.makeText(context, "Google Sign-up failed: Token missing", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-up failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Facebook Launcher
    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract()
    ) { _ ->
        try {
            val token = com.facebook.AccessToken.getCurrentAccessToken()
            token?.let { viewModel.handleFacebookAccessToken(it) }
        } catch (_: Exception) {}
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onSignedUp()
            is AuthUiState.Error -> Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // ---------- Header ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.forgrnd),
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.signup_header_title),
                        color = Color.White,
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.signup_create_account),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
            }

            // ---------- Input Fields ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextFieldComposable(name, { name = it }, stringResource(R.string.fullname_label), R.drawable.ic_person)
                OutlinedTextFieldComposable(email, { email = it }, stringResource(R.string.email_label), R.drawable.ic_email)
                OutlinedTextFieldComposable(password, { password = it }, stringResource(R.string.password_label), R.drawable.ic_lock, true, passwordVisible) { passwordVisible = !passwordVisible }
                OutlinedTextFieldComposable(confirmPassword, { confirmPassword = it }, stringResource(R.string.confirm_password_label), R.drawable.ic_lock, true, confirmPasswordVisible) { confirmPasswordVisible = !confirmPasswordVisible }
            }

            // ---------- Sign up Button ----------
            Button(
                onClick = { viewModel.signUp(context, name, email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    stringResource(R.string.signup_button),
                    color = backgroundDark,
                    fontSize = 18.sp
                )
            }

            // ---------- Divider + Social ----------
            SocialSignUpButtons(
                googleSignInLauncher = googleSignInLauncher,
                facebookLauncher = facebookLauncher,
                viewModel = viewModel,
            )

            // ---------- Footer ----------
            TextButton(onClick = onNavigateToSignIn) {
                Text(
                    stringResource(R.string.already_have_account),
                    color = primaryColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun OutlinedTextFieldComposable(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIconRes: Int,
    isPassword: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIconRes),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f)
            )
        },
        trailingIcon = if (isPassword && onToggleVisibility != null) {
            {
                val icon = if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF13DAEC),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = Color(0xFF13DAEC),
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color(0xFF13DAEC),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )

}

@Composable
fun SocialSignUpButtons(
    googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    facebookLauncher: ManagedActivityResultLauncher<Collection<String>, CallbackManager.ActivityResultParameters>,
    viewModel: AuthViewModel
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Google Button
        OutlinedButton(
            onClick = {
                viewModel.googleSignOut {
                    googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                }
            },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.05f),
                contentColor = Color.White
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.googlelogo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.google_button))
        }

        // Facebook Button
        OutlinedButton(
            onClick = { facebookLauncher.launch(listOf("email", "public_profile")) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.05f),
                contentColor = Color.White
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.facebooklogo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.facebook_button))
        }
    }
}

