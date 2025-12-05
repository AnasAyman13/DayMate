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


/**
 * Composable function for the Splash Screen UI, handling animations and sound playback.
 * It features logo scaling, rotation, and fading, accompanied by a random sound effect.
 *
 * @param onTimeout Callback executed when the animation and sound finish, triggering navigation.
 */
@Composable
fun SplashScreenComposable(onTimeout: () -> Unit) {

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Animation state variables: scale, alpha (opacity), and rotation
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Sound list (R.raw.* resources must exist in the raw folder)
        val sounds = listOf(
            R.raw.spl1,
            R.raw.spl2,
            R.raw.spl3
        )

        // Random sound selection
        val selectedSound = sounds.random()

        mediaPlayer = MediaPlayer.create(context, selectedSound)
        mediaPlayer?.start()

        // --- Entry Animation Sequence ---
        alpha.animateTo(1f, tween(500)) // Fade in content
        scale.animateTo(1.2f, tween(500)) // Initial bounce out
        scale.animateTo(1f, tween(300)) // Settle scale
        rotation.animateTo(360f, tween(700)) // Full 360 degree rotation

        delay(200) // Small pause before exit animation starts

        // --- Exit Animation Sequence ---
        alpha.animateTo(0f, tween(500)) // Fade out content
        scale.animateTo(0.8f, tween(500)) // Shrink slightly on exit

        // Ensure media player is stopped and navigation occurs
        mediaPlayer?.stop()
        onTimeout() // Execute navigation callback
    }

    DisposableEffect(Unit) {
        onDispose {
            // Release MediaPlayer resources when the Composable leaves the screen
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102022)), // Deep gray/blue background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value) // Apply current opacity state
                .scale(scale.value)  // Apply current scale state
        ) {
            Image(
                // R.drawable.forgrnd must contain the app logo
                painter = painterResource(id = R.drawable.forgrnd),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer(rotationZ = rotation.value) // Apply rotation
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name Text with stylized color change and shadow effect
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
