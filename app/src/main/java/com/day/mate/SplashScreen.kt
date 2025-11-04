package com.day.mate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.ui.onboardingActivity1.DayMateDarkTheme
import com.day.mate.ui.onboardingActivity1.OnboardingActivity1
import com.day.mate.ui.onboardingActivity3.OnboardingActivity3
import com.day.mate.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // نجيب SharedPreferences
        val sharedPref = getSharedPreferences("DayMatePrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        setContent {
            DayMateDarkTheme {
                SplashScreen {
                    if (isFirstTime) {
                        // أول مرة → نفتح Onboarding
                        startActivity(Intent(this, OnboardingActivity1::class.java))
                        // بعد كده نخليها false
                        sharedPref.edit().putBoolean("isFirstTime", false).apply()
                    } else {
                        // مش أول مرة → نفتح signup
                        startActivity(Intent(this,  AuthActivity::class.java))
                    }
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500))
        scale.animateTo(1.2f, tween(500))
        scale.animateTo(1f, tween(300))
        rotation.animateTo(360f, tween(700))
        delay(1200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102022)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.forgrnd),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(260.dp)
                    .scale(scale.value)
                    .graphicsLayer(rotationZ = rotation.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Day")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF00AABB))) {
                        append("Mate")
                    }
                },
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
        }
    }
}
