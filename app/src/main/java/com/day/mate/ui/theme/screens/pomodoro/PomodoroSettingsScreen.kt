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

@Composable
fun PomodoroSettingsScreen(
    onCancel: () -> Unit,
    onSave: (focusSeconds: Int, shortSeconds: Int, longSeconds: Int) -> Unit,
    initialFocus: Int,
    initialShort: Int,
    initialLong: Int
) {
    fun parseTime(secs: Int): Triple<Int, Int, Int> =
        Triple(secs / 3600, (secs % 3600) / 60, secs % 60)

    var (focusH, focusM, focusS) = parseTime(initialFocus)
    var (shortH, shortM, shortS) = parseTime(initialShort)
    var (longH, longM, longS) = parseTime(initialLong)

    var focusHours by remember { mutableStateOf(focusH) }
    var focusMinutes by remember { mutableStateOf(focusM) }
    var focusSeconds by remember { mutableStateOf(focusS) }
    var shortHours by remember { mutableStateOf(shortH) }
    var shortMinutes by remember { mutableStateOf(shortM) }
    var shortSeconds by remember { mutableStateOf(shortS) }
    var longHours by remember { mutableStateOf(longH) }
    var longMinutes by remember { mutableStateOf(longM) }
    var longSeconds by remember { mutableStateOf(longS) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE


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
                item {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

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

                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = CircleShape,
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text(stringResource(R.string.cancel_button), fontSize = 18.sp)
                        }
                        Button(
                            onClick = {
                                val focusSecs = focusHours * 3600 + focusMinutes * 60 + focusSeconds
                                val shortSecs = shortHours * 3600 + shortMinutes * 60 + shortSeconds
                                val longSecs = longHours * 3600 + longMinutes * 60 + longSeconds
                                onSave(focusSecs, shortSecs, longSecs)
                            },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text(stringResource(R.string.save_button), fontSize = 18.sp)
                        }
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

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(iconTint.copy(alpha = 0.15f), shape = CircleShape)
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.align(Alignment.Center).size(30.dp))
        }
        Spacer(Modifier.width(16.dp))


        Text(label, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.width(100.dp))
        Spacer(Modifier.width(12.dp))


        TimeComponentPicker(
            items = (0..12).map { it.toString() },
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
                            color =
                                if (isSel) MaterialTheme.colorScheme.primary
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
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}