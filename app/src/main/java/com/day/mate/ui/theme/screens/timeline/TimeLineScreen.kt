package com.day.mate.ui.theme.screens.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.day.mate.data.model.EventType
import com.day.mate.data.model.TimelineEvent
import com.day.mate.formatTimestampToHourLabel
import com.day.mate.ui.theme.* // **هذا هو الاستيراد الذي يحل مشكلة الألوان**

// =========================================================================
// نماذج وبيانات وهمية (مؤقتة)
// =========================================================================

// نموذج بيانات وهمي مؤقت
data class FakeTimelineEvent(
    val id: Int,
    val timeLabel: String,
    val title: String,
    val timeRange: String,
    val icon: String, // اسم الرمز
    val eventColor: Color,
    val isDone: Boolean = false,
    val isProgress: Float? = null // نسبة التقدم 0.0f to 1.0f
)

// بيانات وهمية للعرض فقط (لتشغيل التصميم)
val fakeEvents = listOf(
    FakeTimelineEvent(1, "09 AM", "Morning Meditation", "09:00 - 09:30 AM", "self_improvement", PrimaryColor, false),
    FakeTimelineEvent(2, "10 AM", "Team Sync Meeting", "10:00 - 11:30 AM", "event", Color(0xFF03A9F4), false, isProgress = 0.25f),
    FakeTimelineEvent(3, "12 PM", "Gym Session", "12:00 PM", "fitness_center", Color(0xFFFFCC00), false),
    FakeTimelineEvent(4, "01 PM", "Lunch with Sarah", "01:00 - 02:00 PM", "restaurant", Color(0xFF4CAF50), true),
    FakeTimelineEvent(5, "02 PM", "", "", "", Color.Transparent), // فاصل زمني وهمي
)

// =========================================================================
// المكونات المساعدة (تم نقلها هنا مؤقتاً لتجنب مشاكل الـ Import)
// =========================================================================

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
            text = "Make today amazing.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { /* More actions */ }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}



@Composable
fun TimelineItem(event: TimelineEvent) {
    val isDark = isSystemInDarkTheme()
    // CardBackgroundDark و CardBackgroundLight و PrimaryColor يجب أن تكون متاحة عبر الـ import: com.day.mate.ui.theme.*

    // 1. تحديد الأيقونة بناءً على نوع الحدث
    val iconImageVector: ImageVector = when (event.type) {
        EventType.PRAYER -> Icons.Filled.SelfImprovement // أيقونة الصلاة
        EventType.TODO_TASK -> Icons.Filled.TaskAlt // أيقونة المهمة
    }

    // 2. تحديد لون الحدود والخلفية الشفافة
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
            // 3. عرض أيقونة الحدث
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(event.iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconImageVector,
                    contentDescription = event.title,
                    tint = event.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // عنوان الحدث (مع خط مشطوب للمهام المنجزة)
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textDecoration = if (event.isDone && event.type == EventType.TODO_TASK) TextDecoration.LineThrough else null,
                    color = if (event.isDone) Color.Gray else MaterialTheme.colorScheme.onBackground
                )

                // الوقت أو الوصف
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WatchLater,
                        contentDescription = "Time",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = event.timeRange, // (مثل 09:00 - 09:30 AM)
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // عرض شريط التقدم إذا كان موجوداً
                event.isProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(top = 4.dp)
                            .clip(CircleShape),
                        color = event.iconColor, // لون التقدم هو لون الحدث
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            // زر حالة الإنجاز (يظهر فقط للمهام)
            if (event.type == EventType.TODO_TASK) {
                IconButton(onClick = { /* TODO: Toggle done status in ViewModel */ }) {
                    Icon(
                        imageVector = if (event.isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Status",
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
        modifier = Modifier.fillMaxWidth(),
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
                    .padding(top = 24.dp)
                    .width(2.dp)
                    .height(80.dp)
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
                        text = "Now",
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

// =========================================================================
// المكون الرئيسي (TimelineScreen)
// =========================================================================
fun isCurrentHour(timestamp: Long): Boolean {
    val currentHourLabel = formatTimestampToHourLabel(System.currentTimeMillis())
    val eventHourLabel = formatTimestampToHourLabel(timestamp)
    return currentHourLabel == eventHourLabel
}
@Composable
fun TimelineScreen(
    // 🆕 استلام الـ ViewModel
    viewModel: TimelineViewModel = viewModel()
) {
    // 🚀 مراقبة قائمة الأحداث الحقيقية
    val events by viewModel.timelineEvents.collectAsState()

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight

    Scaffold(
        topBar = { DayMateTopBar() },
        containerColor = backgroundColor
    ) { paddingValues ->
        // ✅ عرض رسالة تحميل أو حالة فارغة
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading timeline or no events for today...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(events.size) { index ->
                    val event = events[index]

                    TimelineRow(
                        timeLabel = event.timeLabel,
                        content = {
                            // نستخدم TimelineItem لكل حدث حقيقي
                            TimelineItem(event = event)
                        },
                        // استخدام الدالة الجديدة لتحديد مؤشر "Now"
                        isCurrentHour = isCurrentHour(event.timestamp)
                    )
                }
            }
        }
    }
    // ** ملاحظة: تم إزالة FAB من هنا **
}
