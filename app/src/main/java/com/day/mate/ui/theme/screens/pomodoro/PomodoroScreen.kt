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
import java.util.Locale
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

        PomodoroTitleBar(timerState.mode, isLandscape) { showSettings = true }

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


        if (isLandscape) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .offset(y = (-20).dp),

                    contentAlignment = Alignment.Center
                ) {
                    TimerCircleContent(viewModel, animatedProgress, isLandscape)
                }


                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomButtonsContent(viewModel, timerState.isRunning, isLandscape)
                    CompletedSessionsText(timerState.completedSessions, isLandscape)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                TimerCircleContent(viewModel, animatedProgress, isLandscape)

                Spacer(modifier = Modifier.height(40.dp))

                BottomButtonsContent(viewModel, timerState.isRunning, isLandscape)

                Spacer(modifier = Modifier.height(16.dp))

                CompletedSessionsText(timerState.completedSessions, isLandscape)
            }
        }
    }
}

@Composable
fun PomodoroTitleBar(mode: TimerMode, isLandscape: Boolean, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = when (mode) {
                TimerMode.FOCUS -> stringResource(id = R.string.focus)
                TimerMode.SHORT_BREAK -> stringResource(id = R.string.short_break)
                TimerMode.LONG_BREAK -> stringResource(id = R.string.long_break)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            fontSize = if (isLandscape) 36.sp else 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Box(modifier = Modifier.size(42.dp)) {}
    }
}

@Composable
fun TimerCircleContent(viewModel: TimerViewModel, animatedProgress: Float, isLandscape: Boolean) {
    val timerState by viewModel.timerState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth(if (isLandscape) 0.6f else 0.7f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
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
            text = String.format(Locale.ENGLISH, "%02d:%02d", timerState.secondsLeft / 60, timerState.secondsLeft % 60),

            fontSize = if (isLandscape) 60.sp else 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BottomButtonsContent(viewModel: TimerViewModel, isRunning: Boolean, isLandscape: Boolean) {
    val buttonSize = if (isLandscape) 65.dp else 60.dp
    val mainButtonSize = if (isLandscape) 85.dp else 70.dp
    val buttonFontSize = if (isLandscape) 14.sp else 14.sp

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.8f else 1f)
    ) {
        // --- Reset Button ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { viewModel.resetTimer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.reset),
                    tint = Color.White
                )
            }
            Text(text = stringResource(id = R.string.reset), fontSize = buttonFontSize, color = Color.White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(mainButtonSize)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.pause_start),
                    tint = Color.White
                )
            }
            Text(text = stringResource(id = R.string.pause_start), fontSize = buttonFontSize, color = Color.White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { viewModel.skipTimer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(id = R.string.skip),
                    tint = Color.White
                )
            }
            Text(text = stringResource(id = R.string.skip), fontSize = buttonFontSize, color = Color.White)
        }
    }
}

@Composable
fun CompletedSessionsText(completedSessions: Int, isLandscape: Boolean) {
    Text(
        text = stringResource(id = R.string.completed_sessions, completedSessions),
        fontSize = if (isLandscape) 16.sp else 16.sp,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}