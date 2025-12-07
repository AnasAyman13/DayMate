package com.day.mate.ui.theme.screens.pomodoro

import android.widget.Toast
import com.day.mate.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun PomodoroSettingsScreen(
    onCancel: () -> Unit,
    onSave: (focusSeconds: Int, shortSeconds: Int, longSeconds: Int) -> Unit,
    initialFocus: Int,
    initialShort: Int,
    initialLong: Int
) {
    val context = LocalContext.current

    fun parseTime(secs: Int): Triple<Int, Int, Int> =
        Triple(secs / 3600, (secs % 3600) / 60, secs % 60)

    val (fH, fM, fS) = parseTime(initialFocus)
    val (sH, sM, sS) = parseTime(initialShort)
    val (lH, lM, lS) = parseTime(initialLong)

    var focusHours by remember { mutableStateOf(fH.coerceAtLeast(0)) }
    var focusMinutes by remember { mutableStateOf(fM.coerceAtLeast(0)) }
    var focusSeconds by remember { mutableStateOf(fS.coerceAtLeast(0)) }
    var shortHours by remember { mutableStateOf(sH.coerceAtLeast(0)) }
    var shortMinutes by remember { mutableStateOf(sM.coerceAtLeast(0)) }
    var shortSeconds by remember { mutableStateOf(sS.coerceAtLeast(0)) }
    var longHours by remember { mutableStateOf(lH.coerceAtLeast(0)) }
    var longMinutes by remember { mutableStateOf(lM.coerceAtLeast(0)) }
    var longSeconds by remember { mutableStateOf(lS.coerceAtLeast(0)) }

    fun isValidTime(hours: Int, minutes: Int, seconds: Int): Boolean {
        val totalSeconds = hours * 3600 + minutes * 60 + seconds
        return totalSeconds > 0
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

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
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(28.dp))

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

        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(12.dp))

        TimeComponentPicker(
            items = (0..3).map { it.toString() },
            selectedIndex = hours,
            onSelectedChange = onHoursChange,
            label = stringResource(R.string.unit_hours),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TimeComponentPicker(
            items = (0..59).map { it.toString().padStart(2, '0') },
            selectedIndex = minutes,
            onSelectedChange = onMinutesChange,
            label = stringResource(R.string.unit_minutes),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        TimeComponentPicker(
            items = (0..59).map { it.toString().padStart(2, '0') },
            selectedIndex = seconds,
            onSelectedChange = onSecondsChange,
            label = stringResource(R.string.unit_seconds),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TimeComponentPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 38.dp,
    visibleItems: Int = 3
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.lastIndex)
    )
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val boxHeightDp = itemHeight * visibleItems

    // المتغير ده للعرض فقط - بيحسب أقرب عنصر للمنتصف
    val visualCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2

            layoutInfo.visibleItemsInfo
                .minByOrNull { itemInfo ->
                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                    abs(itemCenter - viewportCenter)
                }?.index ?: selectedIndex
        }
    }

    // بس لما السحب يخلص تماماً - نعمل snap
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // دلوقتي السحب خلص - نحسب أقرب عنصر
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2

            val targetIndex = layoutInfo.visibleItemsInfo
                .minByOrNull { itemInfo ->
                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                    abs(itemCenter - viewportCenter)
                }?.index ?: selectedIndex

            // نعمل snap للعنصر ده
            listState.animateScrollToItem(
                index = targetIndex.coerceIn(0, items.lastIndex),
                scrollOffset = 0
            )

            // نبلغ الـ parent بالتغيير
            if (targetIndex != selectedIndex) {
                onSelectedChange(targetIndex)
            }
        }
    }

    // لو الـ selectedIndex اتغير من بره - نسنك بس لو مش بنسحب
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(
                index = selectedIndex.coerceIn(0, items.lastIndex),
                scrollOffset = 0
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(boxHeightDp)
                .fillMaxWidth()
                .border(
                    width = 1.4.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            // المربع اللي في النص
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2)),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // بنمنع الـ tap gestures
                        detectTapGestures { }
                    }
            ) {
                itemsIndexed(items) { index, value ->
                    val isCentered = index == visualCenterIndex
                    val distanceFromCenter = abs(index - visualCenterIndex)
                    val alpha = when (distanceFromCenter) {
                        0 -> 1f
                        1 -> 0.6f
                        else -> 0.3f
                    }
                    val scale = if (isCentered) 1.0f else 0.85f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value,
                            fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isCentered) 20.sp else 16.sp,
                            color = if (isCentered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .alpha(alpha)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
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