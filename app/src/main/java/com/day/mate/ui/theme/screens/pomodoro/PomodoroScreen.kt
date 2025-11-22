package com.day.mate.ui.theme.screens.pomodoro

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.day.mate.R
import com.day.mate.data.local.TimerMode
import kotlinx.coroutines.delay

@Composable
fun PomodoroScreen(viewModel: TimerViewModel = viewModel()) {
    val context = LocalContext.current
    val timerState by viewModel.timerState.collectAsState()
    val progress = remember(timerState) { viewModel.progress() }
    var showSettings by remember { mutableStateOf(false) }

    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.timer_end) }
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101F22))
    ) {
        // ======= TOP BAR =======
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = when (timerState.mode) {
                    TimerMode.FOCUS -> stringResource(id = R.string.focus)
                    TimerMode.SHORT_BREAK -> stringResource(id = R.string.short_break)
                    TimerMode.LONG_BREAK -> stringResource(id = R.string.long_break)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                fontSize = if (isLandscape) 50.sp else 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Box(modifier = Modifier.size(42.dp)) {}
        }

        // ======= SETTINGS DIALOG =======
        if (showSettings) {
            Dialog(
                onDismissRequest = { showSettings = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                PomodoroSettingsScreen(
                    onCancel = { showSettings = false },
                    onSave = { focusSeconds, shortSeconds, longSeconds ->
                        viewModel.updateTimesRaw(focusSeconds, shortSeconds, longSeconds)
                        showSettings = false
                    },
                    initialFocus = viewModel.focusTime,
                    initialShort = viewModel.shortBreakTime,
                    initialLong = viewModel.longBreakTime
                )
            }
        }

        // ======= TIMER CIRCLE =======
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.5f else 0.7f)
                .aspectRatio(1f)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface

            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweep = 360 * animatedProgress
                val strokeWidth = size.minDimension * 0.06f

                drawArc(
                    color = onSurfaceColor.copy(alpha = 0.1f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = String.format("%02d:%02d", timerState.secondsLeft / 60, timerState.secondsLeft % 60),
                fontSize = if (isLandscape) 72.sp else 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // ======= BOTTOM BUTTONS =======
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isLandscape) 16.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 50.dp else 60.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { viewModel.resetTimer() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.reset),
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.reset),
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 60.dp else 70.dp)
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
                                contentDescription = stringResource(id = R.string.pause_start),
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.pause_start),
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 50.dp else 60.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { viewModel.skipTimer() }) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = stringResource(id = R.string.skip),
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.skip),
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = Color.White
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.completed_sessions, timerState.completedSessions),
                fontSize = if (isLandscape) 14.sp else 16.sp,
                color = Color.White
            )
        }
    }
}
