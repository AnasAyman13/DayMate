package com.day.mate.ui.theme.screens.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.viewmodel.compose.viewModel
import com.day.mate.R
import com.day.mate.data.model.EventType
import com.day.mate.data.model.TimelineEvent
import com.day.mate.ui.theme.AppGold
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.LocalDate
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.DpOffset


data class TimeBlock(
    val timeLabel: String,
    val events: List<TimelineEvent>,
    val isCurrentHour: Boolean
)


@Composable
fun getTranslatedTitle(event: TimelineEvent): String {
    return when (event.title) {
        "Fajr Prayer" -> stringResource(R.string.fajr)
        "Dhuhr Prayer" -> stringResource(R.string.dhuhr)
        "Asr Prayer" -> stringResource(R.string.asr)
        "Maghrib Prayer" -> stringResource(R.string.maghrib)
        "Isha Prayer" -> stringResource(R.string.isha)
        else -> event.title
    }
}

@Composable
fun getTranslatedCategory(category: String): String {
    return when (category) {
        "Work" -> stringResource(R.string.category_work)
        "Study" -> stringResource(R.string.category_study)
        "Personal" -> stringResource(R.string.category_personal)
        "Health" -> stringResource(R.string.category_health)
        "General" -> stringResource(R.string.category_General)
        else -> category
    }
}


@Composable
fun TimelineMenu(viewModel: TimelineViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val hideCompleted by viewModel.hideCompleted.collectAsState()
    var isMenuExpanded by remember { mutableStateOf(false) }
    val isViewingTomorrow = selectedDate.isAfter(LocalDate.now())

    val isRtl = LocalConfiguration.current.locale.language == "ar"
    val horizontalOffset = if (isRtl) 180.dp else (-300).dp

    IconButton(onClick = { isMenuExpanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
        offset = DpOffset(x = horizontalOffset, y = 0.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(12.dp))
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
    ) {
        // View Today/Tomorrow
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(
                        if (isViewingTomorrow) R.string.menu_view_today
                        else R.string.menu_view_tomorrow
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                if (isViewingTomorrow) {
                    viewModel.viewToday()
                } else {
                    viewModel.viewTomorrow()
                }
                isMenuExpanded = false
            },
            colors = MenuDefaults.itemColors(
                leadingIconColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Show/Hide Completed
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(
                        if (hideCompleted) R.string.menu_show_completed
                        else R.string.menu_hide_completed
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                viewModel.toggleHideCompleted()
                isMenuExpanded = false
            },
            colors = MenuDefaults.itemColors(
                leadingIconColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Divider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )

        // Mark All as Done
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.menu_mark_all_done),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.DoneAll,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                viewModel.markAllTasksAsDone(selectedDate)
                isMenuExpanded = false
            },
            colors = MenuDefaults.itemColors(
                leadingIconColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Format date for display based on current locale
 */
@Composable
fun formatDateForDisplay(dateToFormat: LocalDate): String {
    val currentLocale = LocalConfiguration.current.locale
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", currentLocale)
    return dateToFormat.format(formatter)
}

/**
 * Translate numerals to Arabic if needed
 */
fun translateNumerals(text: String): String {
    return text
        .replace('0', '٠')
        .replace('1', '١')
        .replace('2', '٢')
        .replace('3', '٣')
        .replace('4', '٤')
        .replace('5', '٥')
        .replace('6', '٦')
        .replace('7', '٧')
        .replace('8', '٨')
        .replace('9', '٩')
}

/**
 * Format time for display based on current locale
 */

@Composable
fun formatTimeForDisplay(time24h: String): String {
    val currentLanguage = LocalConfiguration.current.locale.language
    val outputLocale = if (currentLanguage == "ar") Locale("ar") else Locale.US

    return try {
        val cleaned = time24h.trim()

        // جرّب فورمات HH:mm الأول
        val parsed = when {
            cleaned.matches(Regex("\\d{1,2}:\\d{2}")) -> {
                SimpleDateFormat("HH:mm", Locale.US).parse(cleaned)
            }
            cleaned.matches(Regex("\\d{1,2}\\s?(AM|PM)", RegexOption.IGNORE_CASE)) -> {
                SimpleDateFormat("hh a", Locale.US).parse(cleaned)
            }
            else -> null
        }

        if (parsed != null) {
            SimpleDateFormat("h:mm a", outputLocale).format(parsed)
        } else {
            time24h // سيبه زي ما هو لو مش عارف يفسّره
        }
    } catch (e: Exception) {
        time24h
    }
}


/**
 * Extract hour from timestamp
 */
fun getHourFromTimestamp(timestamp: Long): Int {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        localDateTime.hour
    } catch (e: Exception) {
        -1
    }
}
/**
 * Group events into time blocks by hour
 * This function is now pure Kotlin and does NOT call any @Composable functions
 */
fun groupEventsIntoTimeBlocks(events: List<TimelineEvent>): List<TimeBlock> {
    if (events.isEmpty()) return emptyList()

    // Sort events by timestamp
    val sortedEvents = events.sortedBy { it.timestamp }

    // Group events by the hour they occur
    val groupedByHour = sortedEvents.groupBy { getHourFromTimestamp(it.timestamp) }

    // Get current hour for highlighting
    val currentHour = getHourFromTimestamp(System.currentTimeMillis())

    return groupedByHour.keys
        .sortedBy { hour -> groupedByHour[hour]?.first()?.timestamp ?: 0L }
        .mapNotNull { hour ->
            val hourEvents = groupedByHour[hour]?.sortedBy { it.timestamp }
            if (hourEvents.isNullOrEmpty()) return@mapNotNull null

            // Use the first event's timeLabel as the primary label
            val primaryTimeLabel = hourEvents.first().timeLabel

            TimeBlock(
                timeLabel = primaryTimeLabel,
                events = hourEvents,
                isCurrentHour = (hour == currentHour)
            )
        }
}

@Composable
fun DayMateTopBar(viewModel: TimelineViewModel) {
    val background = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground

    val primary = MaterialTheme.colorScheme.primary
    val quoteBrush = remember(primary) {
        Brush.linearGradient(
            colors = listOf(
                AppGold,
                primary
            )
        )
    }

    val quotes = listOf(
        stringResource(R.string.topbar_quote_1),
        stringResource(R.string.topbar_quote_2),
        stringResource(R.string.topbar_quote_3),
        stringResource(R.string.topbar_quote_4),
        stringResource(R.string.topbar_quote_5),
        stringResource(R.string.topbar_quote_6),
        stringResource(R.string.topbar_quote_7),
        stringResource(R.string.topbar_quote_8),
        stringResource(R.string.topbar_quote_9),
        stringResource(R.string.topbar_quote_10),
        stringResource(R.string.timeline_topbar_title)
    )

    val randomQuote = quotes[Random.nextInt(quotes.size)]

    Surface(
        tonalElevation = 0.dp,
        color = background
    ) {
        CompositionLocalProvider(LocalContentColor provides onBackground) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.forgrnd),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = randomQuote,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(brush = quoteBrush),
                    maxLines = 1
                )

                Spacer(Modifier.width(8.dp))

                TimelineMenu(viewModel = viewModel)
            }

        }
        }
    }





/**
 * TimelineItem
 *
 * Individual event card in the timeline
 */
@Composable
fun TimelineItem(event: TimelineEvent) {
    val iconImageVector: Int = when (event.type) {
        EventType.PRAYER -> R.drawable.ic_mosque_filled
        EventType.TODO_TASK -> R.drawable.ic_todo_filled
    }

    val borderColor = event.iconColor.copy(alpha = 0.3f)
    val containerColor = event.iconColor.copy(alpha = 0.1f)

    val currentLanguage = LocalConfiguration.current.locale.language
    val rawTimeText = formatTimeForDisplay(event.timeRange)
    val displayedTime = if (currentLanguage == "ar") {
        translateNumerals(rawTimeText)
    } else {
        rawTimeText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(event.iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconImageVector),
                    contentDescription = event.title,
                    tint = event.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Event details
            Column(modifier = Modifier.weight(1f)) {
                val translatedTitle = getTranslatedTitle(event)
                Text(
                    text = translatedTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textDecoration = if (event.isDone && event.type == EventType.TODO_TASK)
                        TextDecoration.LineThrough else null,
                    color = if (event.isDone)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WatchLater,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = displayedTime,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Progress indicator
                event.isProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(top = 4.dp)
                            .clip(CircleShape),
                        color = event.iconColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Todo status and category
            if (event.type == EventType.TODO_TASK) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        imageVector = if (event.isDone)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (event.isDone)
                            Color(0xFF4CAF50)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!event.category.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        val translatedCategory = getTranslatedCategory(event.category!!)
                        Text(
                            text = translatedCategory,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 60.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * TimelineRow
 *
 * Row showing time label with vertical line and events
 */
@Composable
fun TimelineRow(
    timeLabel: String,
    content: @Composable () -> Unit,
    isCurrentHour: Boolean = false,
    isViewingToday: Boolean = true
) {
    val currentLanguage = LocalConfiguration.current.locale.language
    val rawTimeText = formatTimeForDisplay(timeLabel.take(5))
    val displayedTime = if (currentLanguage == "ar") {
        translateNumerals(rawTimeText)
    } else {
        rawTimeText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time label column
        Box(
            modifier = Modifier.width(50.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = displayedTime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Vertical line
            Spacer(
                modifier = Modifier
                    .padding(top = if (timeLabel.isNotEmpty()) 50.dp else 2.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Current hour indicator
            if (isCurrentHour&& isViewingToday) {
                Box(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth()
                        .align(Alignment.Center)
                ) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.timeline_now),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 10.dp)
                    )
                }
            }
        }

        // Events content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = if (timeLabel.isNotEmpty()) 0.dp else 10.dp)
        ) {
            content()
        }
    }
}

/**
 * TimelineGroupedRow
 *
 * Row showing all events in a time block
 */
@Composable
fun TimelineGroupedRow(block: TimeBlock,
                       isViewingToday: Boolean) {
    TimelineRow(
        timeLabel = block.timeLabel,
        isCurrentHour = block.isCurrentHour,
        isViewingToday = isViewingToday,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                block.events.forEach { event ->
                    TimelineItem(event = event)
                }
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
}

/**
 * TimelineScreen
 *
 * Main timeline screen showing events for the selected day
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val events by viewModel.timelineEvents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val isViewingToday by viewModel.isViewingToday.collectAsState()

    val timeBlocks = remember(events) {
        groupEventsIntoTimeBlocks(events)
    }

    val listState = rememberLazyListState()
    val currentHour = getHourFromTimestamp(System.currentTimeMillis())

    val firstCurrentHourIndex = remember(timeBlocks) {
        timeBlocks.indexOfFirst { block ->
            getHourFromTimestamp(block.events.first().timestamp) >= currentHour
        }.coerceAtLeast(0)
    }

    // Auto-scroll to current hour
    LaunchedEffect(timeBlocks) {
        if (timeBlocks.isNotEmpty() && firstCurrentHourIndex > 0) {
            listState.animateScrollToItem(firstCurrentHourIndex)
        }
    }

    val currentLanguage = LocalConfiguration.current.locale.language
    val rawDateText = formatDateForDisplay(selectedDate)
    val displayedDate = if (currentLanguage == "ar") {
        translateNumerals(rawDateText)
    } else {
        rawDateText
    }

    Scaffold(
        topBar = { DayMateTopBar(viewModel = viewModel) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Date header
            Text(
                text = displayedDate,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp)
            )

            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp
            )

            // Content
            when {
                isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                timeBlocks.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_timeline_outline),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_events_message),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // Timeline list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(timeBlocks.size) { index ->
                            val block = timeBlocks[index]
                            TimelineGroupedRow(block = block,
                                isViewingToday = isViewingToday)
                        }
                    }
                }
            }
        }
    }
}