package com.day.mate.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.authUiState.AuthUiState
import com.day.mate.utils.getLocalizedErrorMessage
import com.day.mate.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    t: (Int) -> String,
    isArabic: Boolean,
    onToggleLang: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val backgroundDark = Color(0xFF102022)
    val primaryColor = Color(0xFF13DAEC)
    val context = LocalContext.current

    // ✅ مراقبة الحالة مع منع التكرار
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, if (isArabic) "تم تسجيل الدخول بنجاح!" else "Login successful!", Toast.LENGTH_SHORT).show()
                onLoggedIn()
                viewModel.resetState() // ✅ تصفير
            }
            is AuthUiState.Error -> {
                val rawError = (uiState as AuthUiState.Error).message
                val translatedError = getLocalizedErrorMessage(rawError, isArabic)
                Toast.makeText(context, translatedError, Toast.LENGTH_SHORT).show()
                viewModel.resetState() // ✅ تصفير لمنع التكرار
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundDark).padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(id = R.drawable.forgrnd), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(64.dp))
                    Text(text = t(R.string.login_welcome_title), color = Color.White, fontSize = 24.sp, style = MaterialTheme.typography.headlineSmall)
                    Text(text = t(R.string.login_welcome_subtitle), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(y = (-10).dp)) {
                    LanguageToggleButton(isArabic = isArabic, primaryColor = primaryColor, onClick = onToggleLang)
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(t(R.string.email_label)) },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_email), contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = primaryColor, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = primaryColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text(t(R.string.password_label)) },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_lock), contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        val icon = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(painter = painterResource(id = icon), contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = primaryColor, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = primaryColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        if (email.isBlank()) {
                            val msg = if (isArabic) "أدخل البريد أولاً لإعادة التعيين" else "Enter email first to reset"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.resetPassword(context, email)
                        }
                    }) {
                        Text(text = t(R.string.forgot_password), color = primaryColor, fontSize = 14.sp)
                    }
                }
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        val msg = if (isArabic) "يرجى إدخال البيانات" else "Please fill all fields"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.signIn(context, email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(text = t(R.string.login_button), color = backgroundDark, fontSize = 18.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
                Text(text = "  ${t(R.string.or_continue_with)}  ", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
            }

            OutlinedButton(
                onClick = onGoogleSignInClicked,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White)
            ) {
                Image(painter = painterResource(id = R.drawable.googlelogo), contentDescription = "Google", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(t(R.string.google_button))
            }

            Spacer(Modifier.height(1.dp))
            TextButton(onClick = onNavigateToSignUp) {
                Text(text = t(R.string.dont_have_account), color = primaryColor, fontSize = 14.sp)
            }

            // ✅ رسالة السبام المترجمة
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = if (isArabic)
                        "⚠️ تنبيه: قد تصل رسالة التفعيل إلى صندوق (Spam). يرجى التحقق منه."
                    else
                        "⚠️ Heads up: Verification email might land in your 'Spam' folder. Please check there.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
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
            onGoogleSignInClicked = {},
            t = { "Preview" },
            isArabic = false,
            onToggleLang = {}
        )
    }
}