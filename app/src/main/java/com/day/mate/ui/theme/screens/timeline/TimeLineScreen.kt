@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.timeline

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.day.mate.R
import com.day.mate.data.model.EventType
import com.day.mate.data.model.TimelineEvent
import com.day.mate.ui.theme.AppGold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

// --- Models ---
data class TimeBlock(
    val hour: Int,
    val timeLabel: String,
    val events: List<TimelineEvent>,
    val isCurrentHour: Boolean
)

// --- Utility Functions ---

@Composable
fun getTranslatedCategory(category: String): String {
    return when (category.lowercase(Locale.ROOT)) {
        "study" -> stringResource(R.string.category_study)
        "work" -> stringResource(R.string.category_work)
        "personal" -> stringResource(R.string.category_personal)
        "shopping" -> stringResource(R.string.category_shopping)
        "general" -> stringResource(R.string.category_general)
        else -> category
    }
}

@Composable
fun getTranslatedTitle(event: TimelineEvent): String {
    return when {
        event.type == EventType.PRAYER && event.title.contains("Fajr", ignoreCase = true) -> stringResource(R.string.fajr)
        event.type == EventType.PRAYER && event.title.contains("Dhuhr", ignoreCase = true) -> stringResource(R.string.dhuhr)
        event.type == EventType.PRAYER && event.title.contains("Asr", ignoreCase = true) -> stringResource(R.string.asr)
        event.type == EventType.PRAYER && event.title.contains("Maghrib", ignoreCase = true) -> stringResource(R.string.maghrib)
        event.type == EventType.PRAYER && event.title.contains("Isha", ignoreCase = true) -> stringResource(R.string.isha)
        else -> event.title
    }
}

@Composable
fun formatTimeForDisplayFixed(time24h: String): String {
    val currentLanguage = LocalConfiguration.current.locales[0].language
    return try {
        val time = LocalTime.parse(
            time24h.trim().uppercase().replace(" ", ""),
            DateTimeFormatter.ofPattern("H:mm", Locale.US)
        )
        val formatted = time.format(DateTimeFormatter.ofPattern("h:mm a", Locale(currentLanguage)))
        if (currentLanguage == "ar") translateNumerals(formatted) else formatted
    } catch (e: Exception) { time24h }
}

fun translateNumerals(text: String): String {
    val numerals = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )
    return text.map { numerals[it] ?: it }.joinToString("")
}

// --- UI Components ---

@Composable
fun DayMateTopBar(
    viewModel: TimelineViewModel,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val primary = MaterialTheme.colorScheme.primary
    val quoteBrush = remember { Brush.linearGradient(colors = listOf(AppGold, primary)) }

    // الاقتباسات
    val quotes = listOf(
        stringResource(R.string.topbar_quote_1),
        stringResource(R.string.topbar_quote_2),
        stringResource(R.string.topbar_quote_3),
        stringResource(R.string.timeline_topbar_title)
    )
    val randomQuote = remember { quotes[Random.nextInt(quotes.size)] }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp), // زيادة الـ Padding الرأسي
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. اللوجو (تم تصغيره قليلاً ليعطي مساحة للنص)
            Icon(
                painterResource(id = R.drawable.forgrnd),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.width(8.dp))

            // 2. نص الاقتباس (تم إزالة maxLines = 1 وتعديل الوزن)
            Text(
                text = randomQuote,
                fontSize = 13.sp, // تقليل الخط قليلاً لضمان الاحتواء
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(brush = quoteBrush),
                lineHeight = 16.sp, // ضبط المسافة بين السطور لو نزل لسطرين
                softWrap = true    // السماح للنص بالالتفاف بدلاً من الاختفاء
            )

            Spacer(Modifier.width(4.dp))

            // 3. المنيو
            TimelineMenu(viewModel, context, coroutineScope, snackbarHostState)
        }
    }
}

@Composable
fun TimelineMenu(
    viewModel: TimelineViewModel,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val hideCompleted by viewModel.hideCompleted.collectAsState()
    val events by viewModel.timelineEvents.collectAsState()

    var isMenuExpanded by remember { mutableStateOf(false) }
    val isRtl = LocalConfiguration.current.locales[0].language == "ar"

    Box {
        IconButton(onClick = { isMenuExpanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            offset = DpOffset(if (isRtl) 20.dp else (-180).dp, 8.dp),
            modifier = Modifier
                .width(220.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .border(1.dp, AppGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            val isTomorrow = selectedDate.isAfter(LocalDate.now())
            ModernDropdownItem(
                text = stringResource(if (isTomorrow) R.string.menu_view_today else R.string.menu_view_tomorrow),
                icon = Icons.Filled.EventNote,
                onClick = {
                    isMenuExpanded = false // ✅ يقفل فوراً
                    if (isTomorrow) viewModel.viewToday() else viewModel.viewTomorrow()
                }
            )

            ModernDropdownItem(
                text = stringResource(if (hideCompleted) R.string.menu_show_completed else R.string.menu_hide_completed),
                icon = Icons.Filled.VisibilityOff,
                onClick = {
                    isMenuExpanded = false // ✅ يقفل فوراً
                    viewModel.toggleHideCompleted()
                }
            )

            Divider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))

            ModernDropdownItem(
                text = stringResource(R.string.menu_mark_all_done),
                icon = Icons.Filled.DoneAll,
                onClick = {
                    isMenuExpanded = false // ✅ يقفل فوراً قبل التنفيذ

                    coroutineScope.launch {
                        val tasks = events.filter { it.type == EventType.TODO_TASK }
                        val totalTasks = tasks.size
                        val completedTasks = tasks.count { it.isDone }

                        if (totalTasks == 0) {
                            snackbarHostState.showSnackbar(
                                message = if(isRtl) "لا توجد مهام لهذا اليوم" else "No tasks for this day"
                            )
                        } else if (completedTasks == totalTasks) {
                            snackbarHostState.showSnackbar(
                                message = if(isRtl) "جميع المهام مكتملة بالفعل!" else "All tasks are already completed!"
                            )
                        } else {
                            viewModel.markAllTasksAsDone(selectedDate)
                            snackbarHostState.showSnackbar(
                                message = if(isRtl) "تم تحديد جميع المهام كمكتملة" else "All tasks marked as done"
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ModernDropdownItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text, fontWeight = FontWeight.Medium, fontSize = 14.sp) },
        leadingIcon = {
            Box(
                Modifier
                    .size(30.dp)
                    .background(AppGold.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = AppGold)
            }
        },
        onClick = onClick
    )
}

@Composable
fun CategoryTag(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = AppGold.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = AppGold,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TimelineItem(event: TimelineEvent) {
    val iconRes = if (event.type == EventType.PRAYER) R.drawable.ic_mosque_filled else R.drawable.ic_todo_filled
    val isRtl = LocalConfiguration.current.locales[0].language == "ar"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = event.iconColor.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, event.iconColor.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // 1. Icon Box
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(event.iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(iconRes),
                    null,
                    tint = event.iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))

            // 2. Content
            Column(Modifier.weight(1f)) {
                Text(
                    text = getTranslatedTitle(event),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textDecoration = if (event.isDone && event.type == EventType.TODO_TASK) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimeForDisplayFixed(event.timeRange),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 3. Checkbox & Category
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (event.type == EventType.TODO_TASK) {
                    Icon(
                        if (event.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        null,
                        tint = if (event.isDone) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    val categoryText = event.category ?: ""
                    if (categoryText.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        CategoryTag(text = getTranslatedCategory(categoryText))
                    }
                } else if (event.type == EventType.PRAYER) {
                    val prayerText = if (isRtl) "صلاة" else "Prayer"
                    CategoryTag(text = prayerText)
                }
            }
        }
    }
}


@Composable
fun TimelineRow(block: TimeBlock, isViewingToday: Boolean) {
    val currentMinute = LocalTime.now().minute
    val fractionOfHour = currentMinute / 60f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTimeForDisplayFixed("${block.hour}:00"),
                fontSize = 11.sp,
                color = if (block.isCurrentHour && isViewingToday) AppGold else Color.Gray,
                fontWeight = if (block.isCurrentHour && isViewingToday) FontWeight.Bold else FontWeight.Normal
            )

            // الحاوية الخاصة بالخط والمؤشر المتحرك
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // الخط الرمادي الخلفي (الثابت)
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                )

                if (block.isCurrentHour && isViewingToday) {
                    // الخط الذهبي الأمامي (المتغير حسب الدقائق)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight(fractionOfHour)
                            .background(AppGold)
                    )

                    // التنفيذ المظبوط للمؤشر العائم
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fractionOfHour),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // هالة التوهج (Glow)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(AppGold.copy(alpha = glowAlpha), CircleShape)
                        )
                        // النقطة الذهبية المركزية
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AppGold, CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                        )
                    }
                }
            }
        }

        // عمود الأحداث (التصميم العائم Floating)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = 28.dp)
        ) {
            block.events.forEach { event ->
                TimelineItem(event)
            }
        }
    }
}
@Composable
fun TimelineScreen(viewModel: TimelineViewModel = viewModel()) {
    val events by viewModel.timelineEvents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isViewingToday by viewModel.isViewingToday.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val listState = rememberLazyListState()

    // ترتيب الساعات
    val timeBlocks = remember(events) {
        val currentH = LocalTime.now().hour
        events.groupBy {
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault())
            dt.hour
        }.map { (h, evs) ->
            TimeBlock(h, evs.first().timeLabel, evs.sortedBy { it.timestamp }, h == currentH)
        }.sortedBy { it.hour }
    }

    LaunchedEffect(timeBlocks) {
        if (isViewingToday && timeBlocks.isNotEmpty()) {
            val nowHour = LocalTime.now().hour
            val index = timeBlocks.indexOfFirst { it.hour >= nowHour }.coerceAtLeast(0)
            listState.animateScrollToItem(index)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { DayMateTopBar(viewModel, LocalContext.current, rememberCoroutineScope(), snackbarHostState) },
        snackbarHost = {
            // 🔥 تعديل لون السناك بار ليكون AppGold
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 110.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppGold, // الخلفية ذهبي
                    contentColor = Color.Black // النص أسود عشان التباين
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val lang = LocalConfiguration.current.locales[0].language
            val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale(lang)))

            Text(
                text = if (lang == "ar") translateNumerals(dateStr) else dateStr,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.ExtraBold
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = AppGold) }
            } else if (timeBlocks.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text(stringResource(R.string.no_events_message)) }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(timeBlocks.size) { index ->
                        TimelineRow(timeBlocks[index], isViewingToday)
                    }
                }
            }
        }
    }
}