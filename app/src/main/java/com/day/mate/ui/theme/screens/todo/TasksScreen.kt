package com.day.mate.ui.theme.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.compose.ui.text.TextStyle

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
    val today = LocalDate.now()
    val weekDays = (-3L..3L).map { today.plusDays(it) }
    var selectedDate by remember { mutableStateOf(today) }
    val dateDialogState = rememberMaterialDialogState()

    val categories by viewModel.categories.collectAsState()
    val allCategoriesText = stringResource(id = R.string.form_category_all)
    val filters = listOf(allCategoriesText) + categories
    var selectedFilter by remember { mutableStateOf(filters.first()) }

    val allTodos by viewModel.todos.collectAsState()

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
                    onClick = { selectedDate = day }
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
                TaskItem(
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
                TaskItem(
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

    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(stringResource(R.string.dialog_ok), textStyle = TextStyle(color = AppGold))
            negativeButton(stringResource(R.string.dialog_cancel), textStyle = TextStyle(color = DarkTextHint))
        }
    ) {
        datepicker(
            initialDate = selectedDate,
            title = stringResource(R.string.select_date),
            onDateChange = { selectedDate = it }
        )
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

// --- ✅ تعديل TaskItem لعرض الـ Description ---
@Composable
fun TaskItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
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

                // ✅ Description (New Feature)
                if (todo.description.isNotBlank()) {
                    Text(
                        text = todo.description,
                        color = DarkTextHint,
                        fontSize = 13.sp,
                        maxLines = 1, // عشان مياخدش مساحة كبيرة
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
    onClick: () -> Unit
) {
    val locale = Locale.getDefault()
    val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale).uppercase()
    val dayNumber = date.dayOfMonth.toString()
    val isToday = date == LocalDate.now()

    val containerColor = if (isSelected) AppGold else DarkField
    val contentColor = if (isSelected) DarkBg else (if (isToday) AppGold else DarkText)

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
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
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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