@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.authUiState.AuthUiState
import com.day.mate.ui.screens.LanguageToggleButton
import com.day.mate.utils.getLocalizedErrorMessage
import com.day.mate.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignedUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    t: (Int) -> String,
    isArabic: Boolean,
    onToggleLang: () -> Unit
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                viewModel.firebaseAuthWithGoogle(token, context)
            } ?: run {
                val msg = if (isArabic) "فشل التسجيل: التوكن مفقود" else "Google Sign-up failed: Token missing"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            val msg = if (isArabic) "فشل التسجيل بجوجل: ${e.statusCode}" else "Google Sign-up failed: ${e.statusCode}"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // ✅ مراقبة الحالة
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onSignedUp()
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                val rawError = (uiState as AuthUiState.Error).message
                val translatedError = getLocalizedErrorMessage(rawError, isArabic)
                Toast.makeText(context, translatedError, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
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
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.forgrnd),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(
                            text = t(R.string.signup_header_title),
                            color = Color.White,
                            fontSize = 28.sp,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = t(R.string.signup_create_account),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(y = (-10).dp)) {
                    // لو عندك LanguageToggleButton في package تانية اعملي import لها
                    // أو استبدليها بزر عادي مؤقتاً
                    LanguageToggleButton(isArabic = isArabic, primaryColor = primaryColor, onClick = onToggleLang)
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextFieldComposable(
                    value = name,
                    onValueChange = { newValue -> name = newValue },
                    label = t(R.string.fullname_label),
                    leadingIconRes = R.drawable.ic_person
                )

                OutlinedTextFieldComposable(
                    value = email,
                    onValueChange = { newValue -> email = newValue },
                    label = t(R.string.email_label),
                    leadingIconRes = R.drawable.ic_email
                )

                OutlinedTextFieldComposable(
                    value = password,
                    onValueChange = { newValue -> password = newValue },
                    label = t(R.string.password_label),
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    visible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                OutlinedTextFieldComposable(
                    value = confirmPassword,
                    onValueChange = { newValue -> confirmPassword = newValue },
                    label = t(R.string.confirm_password_label),
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    visible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
                )
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        val msg = if (isArabic) "يرجى ملء جميع الحقول" else "Please fill all fields"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        val msg = if (isArabic) "كلمات المرور غير متطابقة" else "Passwords do not match"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.signUp(context, name, email, password, confirmPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(text = t(R.string.signup_button), color = backgroundDark, fontSize = 18.sp)
            }

            OutlinedButton(
                onClick = { viewModel.googleSignOut { googleSignInLauncher.launch(viewModel.getGoogleSignInIntent()) } },
                modifier = Modifier.fillMaxWidth().height(48.dp),
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
                Text(t(R.string.google_button))
            }

            TextButton(onClick = onNavigateToSignIn) {
                Text(text = t(R.string.already_have_account), color = primaryColor, fontSize = 14.sp)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = if (isArabic)
                        "⚠️ تنبيه هام: قد تصل رسالة التفعيل إلى صندوق (Spam/Junk). يرجى التحقق منه وتفعيل الحساب."
                    else
                        "⚠️ Heads up: Verification email usually lands in 'Spam' or 'Junk' folder due to security policies. Please check there.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * ✅ نفس الـ Composable اللي كان عندك لكن داخل نفس الملف
 * علشان نمنع Unresolved reference
 */
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
    val primaryColor = Color(0xFF13DAEC)

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
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = primaryColor,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = primaryColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}
