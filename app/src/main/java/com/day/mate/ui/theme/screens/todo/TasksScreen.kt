@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.todo

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.model.Todo
import com.day.mate.ui.theme.AppCyan
import com.day.mate.ui.theme.AppGold
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Category style configuration
 */
@Composable
private fun getCategoryStyle(category: String): CategoryStyle {
    return when (category.lowercase(Locale.ROOT)) {
        "study" -> CategoryStyle(AppCyan.copy(alpha = 0.7f), Icons.Default.School)
        "work" -> CategoryStyle(Color(0xFF9C27B0), Icons.Default.Work)
        "personal" -> CategoryStyle(Color(0xFF4CAF50), Icons.Default.Person)
        "shopping" -> CategoryStyle(Color(0xFFE91E63), Icons.Default.ShoppingCart)
        "general" -> CategoryStyle(Color(0xFF03A9F4), Icons.Default.Label)
        else -> CategoryStyle(MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Label)
    }
}

private data class CategoryStyle(val color: Color, val icon: ImageVector)

@Composable
fun CategoryLabel(category: String): String {
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
fun TasksScreen(
    viewModel: TodoViewModel,
    onEditTask: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // 🔥 اللون الرمادي المحايد بدلاً من البينك
    val neutralCardColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    val today = LocalDate.now()
    val weekDays = (-3L..3L).map { today.plusDays(it) }
    var selectedDate by remember { mutableStateOf(today) }

    var showDatePicker by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val allCategoriesText = stringResource(id = R.string.form_category_all)
    val filters = listOf(allCategoriesText) + categories
    var selectedFilter by remember { mutableStateOf(filters.first()) }

    val allTodos by viewModel.todos.collectAsState()

    val tasksPerDay = remember(allTodos) {
        allTodos.groupBy { it.date }.mapValues { it.value.size }
    }

    val filteredTodos = allTodos.filter { todo ->
        val matchesDate = todo.date == selectedDate.toString()
        val matchesCategory = if (selectedFilter == allCategoriesText) true else todo.category == selectedFilter
        matchesDate && matchesCategory
    }

    val inProgress = filteredTodos.filter { !it.isDone }
    val completed = filteredTodos.filter { it.isDone }

    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showCategoryErrorDialog by remember { mutableStateOf(false) }
    var categoryErrorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (isLandscape) {
        Row(
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(surfaceColor)
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.todo_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { day ->
                            DateButton(
                                date = day,
                                isSelected = selectedDate == day,
                                onClick = { selectedDate = day },
                                isVertical = true,
                                taskCount = tasksPerDay[day.toString()] ?: 0,
                                neutralColor = neutralCardColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(neutralCardColor) // 🔥 استخدام اللون المحايد
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = stringResource(R.string.go_to_date),
                                    tint = onBackgroundColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.go_to_date),
                                    color = onBackgroundColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val isAll = filter == allCategoriesText
                            val displayText = if (isAll) allCategoriesText else CategoryLabel(filter)

                            ModernCategoryChip(
                                text = displayText,
                                isSelected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                isVertical = true,
                                neutralColor = neutralCardColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(neutralCardColor) // 🔥 استخدام اللون المحايد
                                .clickable { showManageCategoriesDialog = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.manage_categories),
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.manage_categories),
                                    color = onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
            ) {
                item(key = "header_in_progress") {
                    ListHeader(stringResource(R.string.header_in_progress))
                }
                items(inProgress, key = { "task_${it.id}" }) { todo ->
                    SwipeToDeleteTaskItem(
                        todo = todo,
                        onToggle = { viewModel.toggleTodoDone(todo) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onEdit = { onEditTask(todo.id) },
                        neutralColor = neutralCardColor
                    )
                }

                item(key = "header_completed") {
                    Spacer(Modifier.height(20.dp))
                    ListHeader(stringResource(R.string.header_completed))
                }
                items(completed, key = { "task_${it.id}" }) { todo ->
                    SwipeToDeleteTaskItem(
                        todo = todo,
                        onToggle = { viewModel.toggleTodoDone(todo) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onEdit = { onEditTask(todo.id) },
                        neutralColor = neutralCardColor
                    )
                }

                if (inProgress.isEmpty() && completed.isEmpty()) {
                    item(key = "empty_state") {
                        Text(
                            stringResource(
                                R.string.empty_tasks,
                                selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM"))
                            ),
                            color = onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(top = 20.dp)
        ) {
            Text(
                stringResource(id = R.string.todo_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = onBackgroundColor
            )
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { day ->
                    DateButton(
                        date = day,
                        isSelected = selectedDate == day,
                        onClick = { selectedDate = day },
                        taskCount = tasksPerDay[day.toString()] ?: 0,
                        neutralColor = neutralCardColor
                    )
                }
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(neutralCardColor) // 🔥 استخدام اللون المحايد
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.go_to_date),
                        tint = onBackgroundColor
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isAll = filter == allCategoriesText
                    val displayText = if (isAll) allCategoriesText else CategoryLabel(filter)

                    ModernCategoryChip(
                        text = displayText,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        neutralColor = neutralCardColor
                    )
                }

                IconButton(
                    onClick = { showManageCategoriesDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(neutralCardColor) // 🔥 استخدام اللون المحايد
                ) {
                    Icon(
                        Icons.Default.Settings,
                        stringResource(R.string.manage_categories),
                        tint = onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                item(key = "header_in_progress") {
                    ListHeader(stringResource(R.string.header_in_progress))
                }
                items(inProgress, key = { "task_${it.id}" }) { todo ->
                    SwipeToDeleteTaskItem(
                        todo = todo,
                        onToggle = { viewModel.toggleTodoDone(todo) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onEdit = { onEditTask(todo.id) },
                        neutralColor = neutralCardColor
                    )
                }

                item(key = "header_completed") {
                    Spacer(Modifier.height(20.dp))
                    ListHeader(stringResource(R.string.header_completed))
                }
                items(completed, key = { "task_${it.id}" }) { todo ->
                    SwipeToDeleteTaskItem(
                        todo = todo,
                        onToggle = { viewModel.toggleTodoDone(todo) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onEdit = { onEditTask(todo.id) },
                        neutralColor = neutralCardColor
                    )
                }

                if (inProgress.isEmpty() && completed.isEmpty()) {
                    item(key = "empty_state") {
                        Text(
                            stringResource(
                                R.string.empty_tasks,
                                selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM"))
                            ),
                            color = onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val todayMillis = remember {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selected = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        selectedDate = selected
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.dialog_ok), color = AppGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel), color = onSurfaceVariant)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = AppGold,
                    headlineContentColor = onBackgroundColor,
                    selectedDayContainerColor = AppGold,
                    selectedDayContentColor = Color.Black,
                    todayContentColor = AppGold,
                    todayDateBorderColor = AppGold
                )
            )
        }
    }

    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            viewModel = viewModel,
            onDismiss = { showManageCategoriesDialog = false },
            onError = { errorKey ->
                val errorId = try { context.resources.getIdentifier(errorKey, "string", context.packageName) } catch (e: Exception) { 0 }
                categoryErrorMessage = if (errorId != 0) context.getString(errorId) else errorKey
                showManageCategoriesDialog = false
                showCategoryErrorDialog = true
            }
        )
    }

    if (showCategoryErrorDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryErrorDialog = false },
            title = { Text(stringResource(R.string.dialog_error), color = AppGold) },
            text = { Text(categoryErrorMessage, color = onBackgroundColor) },
            confirmButton = {
                TextButton(onClick = { showCategoryErrorDialog = false }) {
                    Text(stringResource(R.string.dialog_ok), color = AppGold)
                }
            },
            containerColor = surfaceColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isVertical: Boolean = false,
    neutralColor: Color // 🔥 تمرير اللون المحايد
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        enabled = true,
        label = {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = if (isVertical) Modifier.fillMaxWidth().padding(vertical = 4.dp) else Modifier,
                textAlign = if (isVertical) TextAlign.Center else TextAlign.Start
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppGold,
            selectedLabelColor = Color.Black,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = neutralColor // 🔥 استخدام اللون المحايد
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            borderWidth = 1.dp,
            selectedBorderColor = Color.Transparent
        ),
        modifier = if (isVertical) Modifier.fillMaxWidth() else Modifier
    )
}

@Composable
fun ManageCategoriesDialog(
    viewModel: TodoViewModel,
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_manage_categories), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it }) { categoryName ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(categoryName, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { viewModel.deleteCategory(categoryName, onError) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) // 🔥 فاصل رمادي خفيف
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_done), color = AppGold) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ListHeader(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun SwipeToDeleteTaskItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    isCompact: Boolean = false,
    neutralColor: Color // 🔥 تمرير اللون المحايد
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "swipe_animation"
    )

    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    val maxSwipe = with(density) { 200.dp.toPx() }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 4.dp else 8.dp)
    ) {
        if (animatedOffsetX < -10f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(if (isCompact) 12.dp else 16.dp))
                    .background(Color(0xFF8B0000)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 24.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(todo.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -swipeThreshold) showDeleteDialog = true
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (offsetX + dragAmount)
                            offsetX = if (newOffset > 0) 0f else if (newOffset < -maxSwipe) -maxSwipe else newOffset
                        }
                    )
                }
        ) {
            TaskItem(todo = todo, onToggle = onToggle, onDelete = onDelete, onEdit = onEdit, neutralColor = neutralColor)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.delete_toast), color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_ok), color = Color(0xFF8B0000))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Modern Task Item - تصميم نظيف بدون أيقونات الكاتيجوري التقليدية
 */
@Composable
fun TaskItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    neutralColor: Color // 🔥 تمرير اللون المحايد
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = neutralColor) // 🔥 استخدام اللون المحايد
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top // محاذاة للأعلى عشان الديسكريبشن لو طويل
        ) {
            // دائرة الحالة (Status Check)
            Box(
                modifier = Modifier
                    .padding(top = 2.dp) // موازنة مع أول سطر نص
                    .size(24.dp)
                    .border(2.dp, if (todo.isDone) AppGold else Color.Gray, CircleShape)
                    .clip(CircleShape)
                    .background(if (todo.isDone) AppGold else Color.Transparent)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (todo.isDone) Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.Black)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                // السطر الأول: العنوان + نوع الكاتيجوري على اليمين
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = todo.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // الكاتيجوري كـ Tag بسيط على اليمين
                    Surface(
                        color = AppGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = CategoryLabel(todo.category),
                            color = AppGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // السطر الثاني: الديسكريبشن
                if (todo.description.isNotBlank()) {
                    Text(
                        text = todo.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // السطر الثالث: الوقت (لو موجود)
                if (todo.time.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = try {
                                LocalTime.parse(todo.time).format(DateTimeFormatter.ofPattern("hh:mm a"))
                            } catch (e: Exception) { todo.time },
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // قائمة الخيارات (3 نقط)
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_edit)) },
                        onClick = { onEdit(); menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_delete), color = Color.Red) },
                        onClick = { onDelete(); menuExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun DateButton(date: LocalDate, isSelected: Boolean, onClick: () -> Unit, isVertical: Boolean = false, taskCount: Int = 0, neutralColor: Color) {
    val locale = Locale.getDefault()
    val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale).uppercase()
    val dayNumber = date.dayOfMonth.toString()

    // 🔥 استخدام اللون المحايد
    val containerColor = if (isSelected) AppGold else neutralColor
    val contentColor = if (isSelected) Color.Black else (if (date == LocalDate.now()) AppGold else MaterialTheme.colorScheme.onBackground)

    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "scale")

    Box(modifier = Modifier.then(if (isVertical) Modifier.fillMaxWidth() else Modifier.padding(end = 8.dp)).scale(scale).clip(RoundedCornerShape(18.dp)).background(containerColor).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        if (isVertical) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = contentColor)
                    if (taskCount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(18.dp).background(if (isSelected) Color.White.copy(alpha = 0.3f) else AppGold.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(taskCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = contentColor)
                        }
                    }
                }
                Text(dayNumber, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.7f))
                Text(dayNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = contentColor)
                if (taskCount > 0) {
                    Box(Modifier.size(6.dp).background(if (isSelected) Color.Black else AppGold, CircleShape))
                }
            }
        }
    }
}