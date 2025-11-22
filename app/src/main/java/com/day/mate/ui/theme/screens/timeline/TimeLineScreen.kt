package com.day.mate.ui.theme.screens.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.viewmodel.compose.viewModel
import com.day.mate.R
import com.day.mate.data.model.EventType
import com.day.mate.data.model.TimelineEvent
import com.day.mate.formatTimestampToHourLabel
import com.day.mate.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime


data class TimeBlock(
    val timeLabel: String,
    val events: List<TimelineEvent>,
    val isCurrentHour: Boolean
)



fun formatTimeForDisplay(time24h: String): String {

    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.parse(time24h, inputFormatter)

        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale("ar"))

        time.format(outputFormatter)
    } catch (e: Exception) {
        try {
            val inputFormatter = SimpleDateFormat("HH:mm", Locale.US)
            val date: Date? = inputFormatter.parse(time24h)
            val outputFormatter = SimpleDateFormat("hh:mm", Locale("ar"))

            if (date != null) outputFormatter.format(date) else time24h
        } catch (e2: Exception) {
            time24h
        }
    }
}
fun getHourFromTimestamp(timestamp: Long): Int {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        localDateTime.hour
    } catch (e: Exception) {
        -1
    }
}


fun groupEventsIntoTimeBlocks(events: List<TimelineEvent>): List<TimeBlock> {
    if (events.isEmpty()) return emptyList()

    val groupedByHour = events.groupBy { getHourFromTimestamp(it.timestamp) }

    val currentHour = getHourFromTimestamp(System.currentTimeMillis())


    return groupedByHour.keys
        .sorted()
        .mapNotNull { hour ->
            val hourEvents = groupedByHour[hour]?.sortedBy { it.timestamp }
            if (hourEvents.isNullOrEmpty()) return@mapNotNull null
            val primaryTimeLabel = hourEvents.first().timeLabel

            TimeBlock(
                timeLabel = primaryTimeLabel,
                events = hourEvents,
                isCurrentHour = (hour == currentHour)
            )
        }
}


@Composable
fun DayMateTopBar() {
    val isDark = isSystemInDarkTheme()
    val barColor = if (isDark) BackgroundDark else BackgroundLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(barColor.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = null,
            tint = Color(0xFF03A9F4),
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.timeline_topbar_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { /* More actions */ }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun TimelineItem(event: TimelineEvent) {
    val isDark = isSystemInDarkTheme()

    val iconImageVector: Int = when (event.type) {
        EventType.PRAYER -> R.drawable.ic_mosque_filled
        EventType.TODO_TASK -> R.drawable.ic_todo_filled
    }

    val borderColor = event.iconColor.copy(alpha = 0.3f)
    val containerColor = event.iconColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textDecoration = if (event.isDone && event.type == EventType.TODO_TASK)
                        TextDecoration.LineThrough else null,
                    color = if (event.isDone) Color.Gray else MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WatchLater,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatTimeForDisplay(event.timeRange),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                event.isProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(top = 4.dp)
                            .clip(CircleShape),
                        color = event.iconColor,
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            if (event.type == EventType.TODO_TASK) {
                IconButton(onClick = { /* TODO: Toggle done status in ViewModel */ }) {
                    Icon(
                        imageVector = if (event.isDone)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (event.isDone) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineRow(
    timeLabel: String,
    content: @Composable () -> Unit,
    isCurrentHour: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.width(30.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = timeLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier
                    .padding(top = if (timeLabel.isNotEmpty()) 50.dp else 2.dp)
                    .width(2.dp)
                    .fillMaxHeight() // يمتد الخط لكامل ارتفاع محتوى الصف
                    .background(Color.LightGray)
            )

            if (isCurrentHour) {
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
                        color = PrimaryColor
                    )
                    Text(
                        text = stringResource(R.string.timeline_now),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 10.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = if (timeLabel.isNotEmpty()) 0.dp else 10.dp)
        ) {
            content()
        }
    }
}
@Composable
fun TimelineGroupedRow(
    block: TimeBlock
) {

    TimelineRow(
        timeLabel = block.timeLabel,
        isCurrentHour = block.isCurrentHour,
        content = {
            // نستخدم Column لتكديس بطاقات المهام (TimelineItem)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                block.events.forEach { event ->
                    TimelineItem(event = event)
                }
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
}

fun isCurrentHour(timestamp: Long): Boolean {
    val currentHourLabel = formatTimestampToHourLabel(System.currentTimeMillis())
    val eventHourLabel = formatTimestampToHourLabel(timestamp)
    return currentHourLabel == eventHourLabel
}

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val events by viewModel.timelineEvents.collectAsState()

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight


    val timeBlocks = remember(events) {
        groupEventsIntoTimeBlocks(events)
    }

    Scaffold(
        topBar = { DayMateTopBar() },
        containerColor = backgroundColor
    ) { paddingValues ->
        if (timeBlocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.timeline_empty_or_loading),
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(timeBlocks.size) { index ->
                    val block = timeBlocks[index]
                    TimelineGroupedRow(block = block)
                }
            }
        }
    }
}