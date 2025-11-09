package com.day.mate.ui.screens

import android.widget.Toast
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
import com.facebook.login.LoginManager
import com.google.android.gms.common.api.ApiException
// تم حذف Imports Google Sign-In المكررة هنا

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    // *** المعامل المفقود تم إضافته لحل جميع الأخطاء ***
    onGoogleSignInClicked: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val backgroundDark = Color(0xFF102022)
    val primaryColor = Color(0xFF13DAEC)

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                // ملاحظة: يُفضل ترك onLoggedIn في AuthActivity ليتم بعد التأكد من التحقق من البريد
                // Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                // onLoggedIn()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    // *** تم حذف LaunchedEffect(Unit) الخاص بتهيئة Google Client ***
    // *** تم حذف Google Sign-In Launcher بالكامل ***

    val facebookLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract()
    ) { result ->
        try {
            val token = com.facebook.AccessToken.getCurrentAccessToken()
            if (token != null) {
                viewModel.handleFacebookAccessToken(token)
            } else {
                println("Facebook Token is null")
            }
        } catch (e: Exception) {
            println("Facebook Error: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ---------- Header (كما هو) ----------
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.forgrnd),
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = stringResource(R.string.login_welcome_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    stringResource(R.string.login_welcome_subtitle),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // ---------- Inputs (كما هو) ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = "email",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password_label)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "password",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        val icon = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = description,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        viewModel.resetPassword(context, email)
                    }) {
                        Text(
                            stringResource(R.string.forgot_password),
                            color = primaryColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ---------- Login button (كما هو) ----------
            Button(
                onClick = { viewModel.signIn(context, email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    stringResource(R.string.login_button),
                    color = backgroundDark,
                    fontSize = 18.sp
                )
            }

            // ---------- Divider (كما هو) ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
                Text(
                    "  " + stringResource(R.string.or_continue_with) + "  ",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
            }

            // ---------- Social ----------
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    // *** استدعاء المعامل الممرر لبدء العملية في Activity ***
                    onClick = onGoogleSignInClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = Color.White
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.googlelogo),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.google_button))
                }

                OutlinedButton(
                    onClick = {facebookLauncher.launch(listOf("email", "public_profile")) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = Color.White
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.facebooklogo),
                        contentDescription = "Facebook",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.facebook_button))
                }
            }

            Spacer(Modifier.height(1.dp))
            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    stringResource(R.string.dont_have_account),
                    color = primaryColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    val fakeViewModel = remember { AuthViewModel() }
    MaterialTheme {
        LoginScreen(
            viewModel = fakeViewModel,
            onLoggedIn = {},
            onNavigateToSignUp = {},
            onForgotPassword = {},
            onGoogleSignInClicked = {} // يجب تمرير دالة حتى في الـ Preview
        )
    }
}