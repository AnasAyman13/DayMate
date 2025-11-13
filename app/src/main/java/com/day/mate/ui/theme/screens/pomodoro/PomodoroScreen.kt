package com.day.mate.ui.theme.screens.pomodoro
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {


            Text(
                text = when (timerState.mode) {
                    TimerMode.FOCUS -> stringResource(id = R.string.focus)
                    TimerMode.SHORT_BREAK -> stringResource(id = R.string.short_break)
                    TimerMode.LONG_BREAK -> stringResource(id = R.string.long_break)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                fontSize = if (isLandscape) 50.sp else 42.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            )

            {
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }
        if (showSettings) {
            var focusTimeStr by remember { mutableStateOf((viewModel.focusTime / 60).toString()) }
            var shortBreakStr by remember { mutableStateOf((viewModel.shortBreakTime / 60).toString()) }
            var longBreakStr by remember { mutableStateOf((viewModel.longBreakTime / 60).toString()) }

            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text(stringResource(id = R.string.settings_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = focusTimeStr,
                            onValueChange = { focusTimeStr = it },
                            label = { Text(stringResource(id = R.string.focus_time_label)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        OutlinedTextField(
                            value = shortBreakStr,
                            onValueChange = { shortBreakStr = it },
                            label = { Text(stringResource(id = R.string.short_break_label)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        OutlinedTextField(
                            value = longBreakStr,
                            onValueChange = { longBreakStr = it },
                            label = { Text(stringResource(id = R.string.long_break_label)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val focusTime = focusTimeStr.toIntOrNull() ?: viewModel.focusTime / 60
                        val shortBreak = shortBreakStr.toIntOrNull() ?: viewModel.shortBreakTime / 60
                        val longBreak = longBreakStr.toIntOrNull() ?: viewModel.longBreakTime / 60

                        viewModel.updateTimes(focusTime, shortBreak, longBreak)
                        showSettings = false
                    }) {
                        Text(stringResource(id = R.string.save_button))
                    }
                },
                dismissButton = {
                    Button(onClick = { showSettings = false }) {
                        Text(stringResource(id = R.string.cancel_button))
                    }
                }
            )
        }


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
                fontWeight = FontWeight.Bold
            )
        }

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
                        fontSize = if (isLandscape) 12.sp else 14.sp
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
                        fontSize = if (isLandscape) 12.sp else 14.sp
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
                        fontSize = if (isLandscape) 12.sp else 14.sp
                    )
                }
            }


            Text(
                text = stringResource(id = R.string.completed_sessions, timerState.completedSessions),
                fontSize = if (isLandscape) 14.sp else 16.sp
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
