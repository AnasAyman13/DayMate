package com.day.mate.ui.theme.screens.media

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.day.mate.R
import com.day.mate.ui.theme.AppGold

@Composable
fun BiometricLockScreen(
    navController: NavController,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val promptManager = remember { BiometricPromptManager(context as AppCompatActivity) }
    val biometricResult by promptManager.promptResults.collectAsState(initial = null)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            promptManager.showBiometricPrompt(
                title = context.getString(R.string.biometric_unlock_title),
                description = context.getString(R.string.biometric_unlock_description),
                allowWithoutLock = true
            )
        }
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lock_pulse"
    )

    LaunchedEffect(biometricResult) {
        when (biometricResult) {
            BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
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
                // التعديل هنا:
                // بدلاً من عرض رسالة النظام (التي قد تكون بالعربية)
                // errorMessage = (biometricResult as BiometricPromptManager.BiometricResult.AuthenticationError).error

                // نستخدم رسالة عامة من التطبيق لتلتزم بلغة التطبيق
                errorMessage = context.getString(R.string.biometric_auth_failed)
            }
            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                errorMessage = context.getString(R.string.biometric_auth_failed)
            }
            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                onUnlockSuccess()
            }
            BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                errorMessage = context.getString(R.string.biometric_feature_unavailable)
            }
            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                errorMessage = context.getString(R.string.biometric_hardware_unavailable)
            }
            else -> Unit
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding() // مهم جداً للتعامل مع الـ Landscape والـ Notch
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // إضافة السكرول لضمان ظهور كل العناصر في الوضع الأفقي
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // أيقونة القفل المتحركة
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        color = AppGold.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.desc_lock_icon),
                    tint = AppGold,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.media_vault_locked),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.media_vault_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // رسالة الخطأ
            errorMessage?.let { message ->
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // زر فتح الخزن
            Button(
                onClick = {
                    errorMessage = null
                    promptManager.showBiometricPrompt(
                        title = context.getString(R.string.biometric_unlock_title),
                        description = context.getString(R.string.biometric_unlock_description),
                        allowWithoutLock = true
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppGold,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.unlock_with_biometrics),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // زر العودة
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = stringResource(R.string.go_back),
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.biometric_helper_text),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}