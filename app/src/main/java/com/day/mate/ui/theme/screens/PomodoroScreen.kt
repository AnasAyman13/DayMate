package com.day.mate.ui.theme.screens


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.material3.SnackbarDefaults.color

import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.day.mate.viewmodel.TimerViewModel
import com.day.mate.R
import kotlinx.coroutines.delay

@Composable
fun PomodoroScreen(viewModel: TimerViewModel = viewModel()) {
    val context = LocalContext.current
    val timerState by viewModel.timerState.collectAsState()
    val progress = remember(timerState) { viewModel.progress() }

    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.timer_end) }
    val vibrator = remember {
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(timerState.isFinished) {
        if (timerState.isFinished) {
            if (!mediaPlayer.isPlaying) mediaPlayer.start()
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(800)
                }
            }
            delay(1500)
            viewModel.handleSessionEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101F22))
    ) {
        // 🔹 الشريط العلوي (فوق الشاشة)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding( top = 8.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /* TODO: Back action */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = when (timerState.mode) {
                    com.day.mate.data.TimerMode.FOCUS -> "Focus"
                    com.day.mate.data.TimerMode.SHORT_BREAK -> "Short Break"
                    com.day.mate.data.TimerMode.LONG_BREAK -> "Long Break"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /* TODO: Settings action */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 🔵 الدايرة فقط في منتصف الشاشة
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweep = 360 * animatedProgress

                    drawArc(
                        color = onSurfaceColor.copy(alpha = 0.1f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, secondaryColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = String.format(
                        "%02d:%02d",
                        timerState.secondsLeft / 60,
                        timerState.secondsLeft % 60
                    ),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 🟢 الأزرار تحت الدايرة
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { viewModel.resetTimer() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (timerState.isRunning) viewModel.pauseTimer()
                        else viewModel.startTimer()
                    }) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Start/Pause",
                            tint = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { viewModel.skipTimer() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = Color.White
                        )
                    }

                }
            }

            Text(
                text = "Completed Focus Sessions: ${timerState.completedSessions}",
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PomodoroScreenPreview() {
    MaterialTheme {
        PomodoroScreen()
    }
}
