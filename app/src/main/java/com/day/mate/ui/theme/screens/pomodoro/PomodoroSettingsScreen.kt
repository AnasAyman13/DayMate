package com.day.mate.ui.theme.screens.pomodoro

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/**
 * PomodoroSettingsScreen
 *
 * Settings screen for configuring Pomodoro timer durations (Focus, Short Break, Long Break).
 * Uses MaterialTheme colors for proper dark/light mode support and adapts layout for
 * landscape orientation by reducing the card width.
 *
 * @param onCancel Callback when user cancels the settings change.
 * @param onSave Callback when user saves with new durations (focusSeconds, shortSeconds, longSeconds).
 * @param initialFocus Initial focus duration in seconds.
 * @param initialShort Initial short break duration in seconds.
 * @param initialLong Initial long break duration in seconds.
 */
@Composable
fun PomodoroSettingsScreen(
    onCancel: () -> Unit,
    onSave: (focusSeconds: Int, shortSeconds: Int, longSeconds: Int) -> Unit,
    initialFocus: Int,
    initialShort: Int,
    initialLong: Int
) {
    // Helper function to parse total seconds into hours, minutes, seconds components
    fun parseTime(secs: Int): Triple<Int, Int, Int> =
        Triple(secs / 3600, (secs % 3600) / 60, secs % 60)

    // Initial parsing of the durations
    val (fH, fM, fS) = parseTime(initialFocus)
    val (sH, sM, sS) = parseTime(initialShort)
    val (lH, lM, lS) = parseTime(initialLong)

    // Mutable state for each time component
    var focusHours by remember { mutableStateOf(fH) }
    var focusMinutes by remember { mutableStateOf(fM) }
    var focusSeconds by remember { mutableStateOf(fS) }
    var shortHours by remember { mutableStateOf(sH) }
    var shortMinutes by remember { mutableStateOf(sM) }
    var shortSeconds by remember { mutableStateOf(sS) }
    var longHours by remember { mutableStateOf(lH) }
    var longMinutes by remember { mutableStateOf(lM) }
    var longSeconds by remember { mutableStateOf(lS) }

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
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Title
                item {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Focus time row
                item {
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
                }

                // Short break row
                item {
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
                }

                // Long break row
                item {
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
                }

                // Action buttons
                item {
                    Spacer(Modifier.height(16.dp))
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
                                // Calculate total seconds from components
                                val focusSecs = focusHours * 3600 + focusMinutes * 60 + focusSeconds
                                val shortSecs = shortHours * 3600 + shortMinutes * 60 + shortSeconds
                                val longSecs = longHours * 3600 + longMinutes * 60 + longSeconds
                                onSave(focusSecs, shortSecs, longSecs)
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
}

/**
 * TimeRow
 *
 * Row for configuring a time duration (hours, minutes, seconds).
 *
 * @param label Label for the time configuration.
 * @param icon Icon representing the time type.
 * @param iconTint Color for the icon.
 * @param hours Current hours value.
 * @param minutes Current minutes value.
 * @param seconds Current seconds value.
 * @param onHoursChange Callback when hours change.
 * @param onMinutesChange Callback when minutes change.
 * @param onSecondsChange Callback when seconds change.
 */
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

        // Time pickers for Hours, Minutes, Seconds
        TimeComponentPicker(
            items = (0..3).map { it.toString() }, // تم تعديل النطاق من (0..12) إلى (0..3)
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

/**
 * TimeComponentPicker
 *
 * Scrollable picker using LazyColumn for selecting a time component (hours/minutes/seconds).
 *
 * @param items List of string items to display (e.g., "00", "01", ..., "59").
 * @param selected The index (integer value) of the currently selected item.
 * @param onSelected Callback when the selection changes, passing the new index (value).
 * @param label Label text displayed beneath the picker.
 * @param modifier Modifier for the component.
 * @param boxHeight Height of the picker box.
 */
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

    // Scroll to the selected item when the 'selected' value changes externally
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
                                // The click listener updates the selected state via index
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