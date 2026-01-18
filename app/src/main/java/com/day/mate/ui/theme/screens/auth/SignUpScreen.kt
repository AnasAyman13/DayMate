package com.day.mate.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.authUiState.AuthUiState
import com.day.mate.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignedUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,

    // shared from AuthActivity/AuthNavGraph
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

    // ✅ IMPORTANT: init Google client once (required for idToken in Release APK)
    LaunchedEffect(Unit) {
        viewModel.initGoogleClient(context, context.getString(R.string.default_web_client_id))
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.firebaseAuthWithGoogle(it, context) } ?: run {
                Toast.makeText(
                    context,
                    if (isArabic) "فشل التسجيل بجوجل: التوكن غير موجود" else "Google Sign-up failed: Token missing",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(
                context,
                if (isArabic) "فشل التسجيل بجوجل: ${e.statusCode}" else "Google Sign-up failed: ${e.statusCode}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handle auth state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onSignedUp()
            is AuthUiState.Error -> Toast.makeText(
                context,
                (uiState as AuthUiState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
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

            // ---------- Header (moves naturally with RTL/LTR, text alignment fixed) ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.forgrnd),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    // ✅ key fix: weight + Alignment.Start (Start becomes Right in RTL automatically)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
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

                // ✅ End becomes left in RTL automatically (like SignIn behavior)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = (-10).dp)
                ) {
                    LanguageToggleButton(
                        isArabic = isArabic,
                        primaryColor = primaryColor,
                        onClick = onToggleLang
                    )
                }
            }

            // ---------- Input Fields ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextFieldComposable(
                    value = name,
                    onValueChange = { name = it },
                    label = t(R.string.fullname_label),
                    leadingIconRes = R.drawable.ic_person
                )

                OutlinedTextFieldComposable(
                    value = email,
                    onValueChange = { email = it },
                    label = t(R.string.email_label),
                    leadingIconRes = R.drawable.ic_email
                )

                OutlinedTextFieldComposable(
                    value = password,
                    onValueChange = { password = it },
                    label = t(R.string.password_label),
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    visible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                OutlinedTextFieldComposable(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = t(R.string.confirm_password_label),
                    leadingIconRes = R.drawable.ic_lock,
                    isPassword = true,
                    visible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
                )
            }

            // ---------- Sign up Button ----------
            Button(
                onClick = { viewModel.signUp(context, name, email, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    text = t(R.string.signup_button),
                    color = backgroundDark,
                    fontSize = 18.sp
                )
            }

            // ---------- Google Button ----------
            OutlinedButton(
                onClick = {
                    viewModel.googleSignOut {
                        googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                Text(t(R.string.google_button))
            }

            // ---------- Footer ----------
            TextButton(onClick = onNavigateToSignIn) {
                Text(
                    text = t(R.string.already_have_account),
                    color = primaryColor,
                    fontSize = 14.sp
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = if (isArabic)
                        "تنبيه: قد تصل رسالة التفعيل إلى البريد غير المرغوب فيه (Spam). لو مش لاقيها في الوارد، راجع Spam وعلّمها كـ “ليست مزعجة”."
                    else
                        "Heads up: Your verification email may land in your Spam folder. If you don’t see it in your inbox, please check there and mark it as ‘Not spam’.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
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
        visualTransformation =
            if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSignUpScreen() {
    val fakeViewModel = remember { AuthViewModel() }
    MaterialTheme {
        SignUpScreen(
            viewModel = fakeViewModel,
            onSignedUp = {},
            onNavigateToSignIn = {},
            t = { "Preview" },
            isArabic = false,
            onToggleLang = {}
        )
    }
}
