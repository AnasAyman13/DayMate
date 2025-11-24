package com.day.mate

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreenComposable(onTimeout: () -> Unit) {

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        // قائمة الأصوات
        val sounds = listOf(
            R.raw.spl1,
            R.raw.spl2,
            R.raw.spl3
        )

        // اختيار عشوائي
        val selectedSound = sounds.random()

        mediaPlayer = MediaPlayer.create(context, selectedSound)
        mediaPlayer?.start()

        // أنيميشن الدخول
        alpha.animateTo(1f, tween(500))
        scale.animateTo(1.2f, tween(500))
        scale.animateTo(1f, tween(300))
        rotation.animateTo(360f, tween(700))

        delay(200)

        // أنيميشن الخروج
        alpha.animateTo(0f, tween(500))
        scale.animateTo(0.8f, tween(500))

        onTimeout()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102022)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.forgrnd),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer(rotationZ = rotation.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            androidx.compose.material3.Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Day") }
                    withStyle(SpanStyle(color = Color(0xFF00AABB))) { append("Mate") }
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
