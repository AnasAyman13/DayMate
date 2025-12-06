package com.day.mate.ui.theme.screens.pomodoro

import android.widget.Toast
import com.day.mate.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Composable
fun PomodoroSettingsScreen(
    onCancel: () -> Unit,
    onSave: (focusSeconds: Int, shortSeconds: Int, longSeconds: Int) -> Unit,
    initialFocus: Int,
    initialShort: Int,
    initialLong: Int
) {
    val context = LocalContext.current

    // Helper function to parse total seconds into hours, minutes, seconds components
    fun parseTime(secs: Int): Triple<Int, Int, Int> =
        Triple(secs / 3600, (secs % 3600) / 60, secs % 60)

    // Initial parsing of the durations
    val (fH, fM, fS) = parseTime(initialFocus)
    val (sH, sM, sS) = parseTime(initialShort)
    val (lH, lM, lS) = parseTime(initialLong)

    // Mutable state for each time component
    var focusHours by remember { mutableStateOf(fH.coerceAtLeast(0)) }
    var focusMinutes by remember { mutableStateOf(fM.coerceAtLeast(0)) }
    var focusSeconds by remember { mutableStateOf(fS.coerceAtLeast(0)) }
    var shortHours by remember { mutableStateOf(sH.coerceAtLeast(0)) }
    var shortMinutes by remember { mutableStateOf(sM.coerceAtLeast(0)) }
    var shortSeconds by remember { mutableStateOf(sS.coerceAtLeast(0)) }
    var longHours by remember { mutableStateOf(lH.coerceAtLeast(0)) }
    var longMinutes by remember { mutableStateOf(lM.coerceAtLeast(0)) }
    var longSeconds by remember { mutableStateOf(lS.coerceAtLeast(0)) }

    // Validation function
    fun isValidTime(hours: Int, minutes: Int, seconds: Int): Boolean {
        val totalSeconds = hours * 3600 + minutes * 60 + seconds
        return totalSeconds > 0
    }

    // Show toast function
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Adjust layout for landscape
    val cardWidthFraction = if (isLandscape) 0.9f else 0.95f
    val horizontalPadding = if (isLandscape) 32.dp else 16.dp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(cardWidthFraction)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                // Title
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Focus time row
                TimeRow(
                    label = stringResource(R.string.focus_label),
                    icon = Icons.Outlined.PsychologyAlt,
                    iconTint = MaterialTheme.colorScheme.primary,
                    hours = focusHours,
                    minutes = focusMinutes,
                    seconds = focusSeconds,
                    onHoursChange = { focusHours = it },
                    onMinutesChange = { focusMinutes = it },
                    onSecondsChange = { focusSeconds = it }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Short break row
                TimeRow(
                    label = stringResource(R.string.short_break_label),
                    icon = Icons.Outlined.LocalCafe,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    hours = shortHours,
                    minutes = shortMinutes,
                    seconds = shortSeconds,
                    onHoursChange = { shortHours = it },
                    onMinutesChange = { shortMinutes = it },
                    onSecondsChange = { shortSeconds = it }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Long break row
                TimeRow(
                    label = stringResource(R.string.long_break_label),
                    icon = Icons.Outlined.Bedtime,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    hours = longHours,
                    minutes = longMinutes,
                    seconds = longSeconds,
                    onHoursChange = { longHours = it },
                    onMinutesChange = { longMinutes = it },
                    onSecondsChange = { longSeconds = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel_button),
                            fontSize = 18.sp
                        )
                    }
                    Button(
                        onClick = {
                            val focusSecs = focusHours * 3600 + focusMinutes * 60 + focusSeconds
                            val shortSecs = shortHours * 3600 + shortMinutes * 60 + shortSeconds
                            val longSecs = longHours * 3600 + longMinutes * 60 + longSeconds

                            when {
                                !isValidTime(focusHours, focusMinutes, focusSeconds) -> {
                                    showToast("Focus time must be more than 0 seconds")
                                }
                                !isValidTime(shortHours, shortMinutes, shortSeconds) -> {
                                    showToast("Short break must be more than 0 seconds")
                                }
                                !isValidTime(longHours, longMinutes, longSeconds) -> {
                                    showToast("Long break must be more than 0 seconds")
                                }
                                else -> {
                                    onSave(focusSecs, shortSecs, longSecs)
                                }
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            stringResource(R.string.save_button),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRow(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(iconTint.copy(alpha = 0.15f), shape = CircleShape)
        ) {
            Icon(
                icon,
                null,
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
            )
        }
        Spacer(Modifier.width(16.dp))

        // Label
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(12.dp))

        // Time pickers
        TimeComponentPicker(
            items = (0..3).map { it.toString() },
            selected = hours,
            onSelected = onHoursChange,
            label = stringResource(R.string.unit_hours),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TimeComponentPicker(
            items = (0..59).map { it.toString().padStart(2, '0') },
            selected = minutes,
            onSelected = onMinutesChange,
            label = stringResource(R.string.unit_minutes),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TimeComponentPicker(
            items = (0..59).map { it.toString().padStart(2, '0') },
            selected = seconds,
            onSelected = onSecondsChange,
            label = stringResource(R.string.unit_seconds),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TimeComponentPicker(
    items: List<String>,
    selected: Int,
    onSelected: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    boxHeight: Dp = 64.dp
) {
    val state = rememberLazyListState(selected)

    LaunchedEffect(selected) {
        state.scrollToItem(selected)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(boxHeight)
                .fillMaxWidth()
                .border(
                    width = 1.4.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            LazyColumn(
                state = state,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(items) { i, v ->
                    val isSel = i == selected

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = v,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isSel) 20.sp else 15.sp,
                            color = if (isSel) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelected(i) }
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 3.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
