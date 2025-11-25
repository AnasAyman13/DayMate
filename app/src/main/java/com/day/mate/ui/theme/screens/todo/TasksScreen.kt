package com.day.mate.ui.theme.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.data.model.Todo
import com.day.mate.ui.theme.*
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.foundation.verticalScroll
import kotlin.math.roundToInt

@Composable
private fun getCategoryStyle(category: String): CategoryStyle {
    return when (category.lowercase(Locale.ROOT)) {
        "study" -> CategoryStyle(AppCyan.copy(alpha = 0.7f), Icons.Default.School)
        "work" -> CategoryStyle(Color(0xFF9C27B0), Icons.Default.Work)
        "personal" -> CategoryStyle(Color(0xFF4CAF50), Icons.Default.Person)
        "shopping" -> CategoryStyle(Color(0xFFE91E63), Icons.Default.ShoppingCart)
        "general" -> CategoryStyle(Color(0xFF03A9F4), Icons.Default.Label)
        else -> CategoryStyle(DarkTextHint, Icons.Default.Label)
    }
}
private data class CategoryStyle(val color: Color, val icon: ImageVector)


@Composable
fun TasksScreen(
    viewModel: TodoViewModel,
    onEditTask: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val today = LocalDate.now()
    val weekDays = (-3L..3L).map { today.plusDays(it) }
    var selectedDate by remember { mutableStateOf(today) }
    val dateDialogState = rememberMaterialDialogState()

    val categories by viewModel.categories.collectAsState()
    val allCategoriesText = stringResource(id = R.string.form_category_all)
    val filters = listOf(allCategoriesText) + categories
    var selectedFilter by remember { mutableStateOf(filters.first()) }

    val allTodos by viewModel.todos.collectAsState()

    // حساب عدد الـ tasks لكل يوم (للـ badge)
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
        // Landscape Layout
        Row(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Left Panel - Date & Category Selection
            Column(
                Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(DarkBg)
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.todo_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // Date Selection - Scrollable
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
                                taskCount = tasksPerDay[day.toString()] ?: 0
                            )
                        }

                        // Calendar Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkField)
                                .clickable { dateDialogState.show() }
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
                                    tint = DarkText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.go_to_date),
                                    color = DarkText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Category Filters - Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filters.forEach { filter ->
                            CategoryButton(
                                text = filter,
                                isSelected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                isVertical = true
                            )
                        }

                        // Settings Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkField)
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
                                    tint = DarkTextHint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.manage_categories),
                                    color = DarkTextHint,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Right Panel - Tasks List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
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
                        onEdit = { onEditTask(todo.id) }
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
                        onEdit = { onEditTask(todo.id) }
                    )
                }

                if (inProgress.isEmpty() && completed.isEmpty()) {
                    item(key = "empty_state") {
                        Text(
                            stringResource(R.string.empty_tasks, selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM"))),
                            color = DarkTextHint,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        // Portrait Layout - الكود الأصلي بالظبط
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 20.dp)
        ) {
            Text(
                stringResource(id = R.string.todo_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onBackground
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
                        taskCount = tasksPerDay[day.toString()] ?: 0
                    )
                }
                IconButton(
                    onClick = { dateDialogState.show() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkField)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.go_to_date),
                        tint = DarkText
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filters.forEach { filter ->
                    CategoryButton(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
                IconButton(
                    onClick = { showManageCategoriesDialog = true },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(DarkField)
                ) {
                    Icon(Icons.Default.Settings, stringResource(R.string.manage_categories), tint = DarkTextHint)
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
                        onEdit = { onEditTask(todo.id) }
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
                        onEdit = { onEditTask(todo.id) }
                    )
                }

                if (inProgress.isEmpty() && completed.isEmpty()) {
                    item(key = "empty_state") {
                        Text(
                            stringResource(R.string.empty_tasks, selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM"))),
                            color = DarkTextHint,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Dialog للتقويم - مختلف حسب الـ orientation
    if (isLandscape) {
        // Landscape - مع scroll
        MaterialDialog(
            dialogState = dateDialogState,
            backgroundColor = DarkBg,
            buttons = {
                positiveButton(stringResource(R.string.dialog_ok))
                negativeButton(stringResource(R.string.dialog_cancel))
            }
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState())
            ) {
                datepicker(
                    initialDate = selectedDate,
                    title = stringResource(R.string.select_date),
                    onDateChange = { selectedDate = it },
                    colors = com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults.colors(
                        headerBackgroundColor = MaterialTheme.colorScheme.primary,
                        headerTextColor = MaterialTheme.colorScheme.onPrimary,
                        calendarHeaderTextColor = DarkText,
                        dateActiveBackgroundColor = AppGold,
                        dateActiveTextColor = DarkBg,
                        dateInactiveTextColor = DarkText
                    )
                )
            }
        }
    } else {
        // Portrait - بدون scroll (عادي)
        MaterialDialog(
            dialogState = dateDialogState,
            backgroundColor = DarkBg,
            buttons = {
                positiveButton(stringResource(R.string.dialog_ok))
                negativeButton(stringResource(R.string.dialog_cancel))
            }
        ) {
            datepicker(
                initialDate = selectedDate,
                title = stringResource(R.string.select_date),
                onDateChange = { selectedDate = it },
                colors = com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults.colors(
                    headerBackgroundColor = MaterialTheme.colorScheme.primary,
                    headerTextColor = MaterialTheme.colorScheme.onPrimary,
                    calendarHeaderTextColor = DarkText,
                    dateActiveBackgroundColor = AppGold,
                    dateActiveTextColor = DarkBg,
                    dateInactiveTextColor = DarkText
                )
            )
        }
    }

    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            viewModel = viewModel,
            onDismiss = { showManageCategoriesDialog = false },
            onError = { errorKey ->
                val errorId = try {
                    context.resources.getIdentifier(errorKey, "string", context.packageName)
                } catch (e: Exception) { 0 }

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
            text = { Text(categoryErrorMessage, color = DarkText) },
            confirmButton = {
                TextButton(onClick = { showCategoryErrorDialog = false }) {
                    Text(stringResource(R.string.dialog_ok), color = AppGold)
                }
            },
            containerColor = DarkBg
        )
    }
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
        title = { Text(stringResource(R.string.dialog_manage_categories), color = DarkText) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it }) { categoryName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(categoryName, color = DarkText)
                        IconButton(onClick = {
                            viewModel.deleteCategory(categoryName, onError)
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.dialog_delete) + " $categoryName",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Divider(color = DarkField)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_done), color = AppGold)
            }
        },
        containerColor = DarkBg
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
    isCompact: Boolean = false
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
        // Background Delete Icon - بس يظهر لما offsetX أقل من 0
        if (animatedOffsetX < -10f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(if (isCompact) 12.dp else 16.dp))
                    .background(Color(0xFF8B0000)), // أحمر غامق
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }

        // Task Card with Swipe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(todo.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -swipeThreshold) {
                                showDeleteDialog = true
                                offsetX = 0f
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (offsetX + dragAmount)
                            offsetX = when {
                                newOffset > 0 -> 0f
                                newOffset < -maxSwipe -> -maxSwipe
                                else -> newOffset
                            }
                        }
                    )
                }
        ) {
            TaskItem(
                todo = todo,
                onToggle = onToggle,
                onDelete = onDelete,
                onEdit = onEdit,
                isCompact = isCompact
            )
        }
    }
    // Dialog للتأكيد
    if (showDeleteDialog) {12
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    stringResource(R.string.dialog_delete),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_toast),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok), color = Color(0xFF8B0000))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel), color = DarkTextHint)
                }
            },
            containerColor = DarkBg
        )
    }
}

@Composable
fun TaskItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    isCompact: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val style = getCategoryStyle(todo.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkField
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Icon(
                imageVector = if (todo.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.desc_toggle_task),
                tint = if (todo.isDone) AppGold else DarkTextHint,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onToggle() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(style.color.copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = stringResource(R.string.desc_category_icon),
                    tint = style.color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title, Description, and Time Column
            Column(
                Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = todo.title,
                    color = if (todo.isDone) DarkTextHint else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )


                if (todo.description.isNotBlank()) {
                    Text(
                        text = todo.description,
                        color = DarkTextHint,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Time
                if (todo.time.isNotBlank()) {
                    Text(
                        text = try {
                            LocalTime.parse(todo.time).format(DateTimeFormatter.ofPattern("hh:mm a"))
                        } catch (e: Exception) { todo.time },
                        color = if (todo.isDone) DarkTextHint else AppGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Menu (3 dots)
            Box {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.desc_task_options),
                    tint = DarkTextHint,
                    modifier = Modifier.clickable { menuExpanded = true }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(DarkField.copy(alpha = 0.9f))
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_edit), color = DarkText) },
                        onClick = {
                            onEdit()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun DateButton(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    isVertical: Boolean = false,
    taskCount: Int = 0
) {
    val locale = Locale.getDefault()
    val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale).uppercase()
    val dayNumber = date.dayOfMonth.toString()
    val isToday = date == LocalDate.now()

    val containerColor = if (isSelected) AppGold else DarkField
    val contentColor = if (isSelected) DarkBg else (if (isToday) AppGold else DarkText)

    // Animation for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .then(
                if (isVertical) Modifier.fillMaxWidth()
                else Modifier.padding(end = 8.dp)
            )
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isVertical) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    if (taskCount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    if (isSelected) DarkBg.copy(alpha = 0.3f)
                                    else AppGold.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                taskCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DarkBg else AppGold
                            )
                        }
                    }
                }
                Text(
                    dayNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        } else {
            Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        dayNumber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    // Badge للتاسكات
                    if (taskCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isSelected) DarkBg else AppGold,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isVertical: Boolean = false
) {
    if (isVertical) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else DarkField)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else DarkText
            )
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else DarkField,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else DarkText
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}