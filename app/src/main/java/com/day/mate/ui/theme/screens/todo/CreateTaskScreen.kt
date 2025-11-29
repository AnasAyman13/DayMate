package com.day.mate.ui.theme.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.day.mate.R
import com.day.mate.ui.theme.AppGold
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * CreateTaskScreen
 *
 * Screen for creating or editing tasks.
 * Supports both dark and light mode through MaterialTheme.
 *
 * @param navController Navigation controller for back navigation
 * @param viewModel ViewModel managing task data
 * @param taskIdString Task ID for editing, or "new" for creating
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: TodoViewModel,
    taskIdString: String?
) {
    val isEditMode = taskIdString != "new"
    val taskId = if (isEditMode) taskIdString?.toIntOrNull() else null

    //  Use MaterialTheme colors (respects user's dark/light mode preference)
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val fieldColor = MaterialTheme.colorScheme.surfaceVariant
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = AppGold
    val dialogBgColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(key1 = taskId) {
        if (isEditMode && taskId != null) {
            viewModel.loadTaskById(taskId)
        }
    }

    val title by viewModel.title.collectAsState()
    val date by viewModel.date.collectAsState()
    val time by viewModel.time.collectAsState()
    val remindMe by viewModel.remindMe.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedCategory by viewModel.category.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()

    // Validate that date/time is not in the past
    val isDateTimeValid by remember(date, time) {
        derivedStateOf {
            val selectedDateTime = LocalDateTime.of(date, time)
            val now = LocalDateTime.now()
            !selectedDateTime.isBefore(now)
        }
    }

    val isButtonEnabled by remember(title, isDateTimeValid) {
        derivedStateOf { title.isNotBlank() && isDateTimeValid }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showPastDateError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(if (isEditMode) R.string.todo_edit_task else R.string.todo_create_task),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.desc_back_button),
                        tint = textColor
                    )
                }
            },
            actions = { Spacer(modifier = Modifier.width(68.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Title field
            TaskTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = stringResource(R.string.form_title),
                fieldColor = fieldColor,
                textColor = textColor,
                hintColor = hintColor,
                accentColor = accentColor
            )
            Spacer(Modifier.height(16.dp))

            // Date picker
            Box {
                TaskTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    onValueChange = {},
                    label = stringResource(R.string.form_date),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, "") },
                    isError = !isDateTimeValid,
                    fieldColor = fieldColor,
                    textColor = textColor,
                    hintColor = hintColor,
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier
                    .matchParentSize()
                    .clickable { dateDialogState.show() })
            }
            Spacer(Modifier.height(16.dp))

            // Time picker
            Box {
                TaskTextField(
                    value = time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                    onValueChange = {},
                    label = stringResource(R.string.form_time),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.AccessTime, "") },
                    isError = !isDateTimeValid,
                    fieldColor = fieldColor,
                    textColor = textColor,
                    hintColor = hintColor,
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier
                    .matchParentSize()
                    .clickable { timeDialogState.show() })
            }

            // Error message for past date/time
            if (!isDateTimeValid) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.error_past_date),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Remind me switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(R.string.form_remind_me),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = textColor
                )
                Switch(
                    checked = remindMe,
                    onCheckedChange = viewModel::onRemindMeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = fieldColor,
                        uncheckedTrackColor = hintColor
                    )
                )
            }
            Spacer(Modifier.height(24.dp))

            // Category selection
            Text(
                stringResource(R.string.form_category),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { categoryKey ->
                    // نستخدم CategoryLabel للعرض، ونبعت الـ key للـ ViewModel
                    CategoryButton(
                        text = CategoryLabel(categoryKey),
                        isSelected = selectedCategory == categoryKey,
                        onClick = { viewModel.onCategoryChange(categoryKey) }
                    )
                }
                Button(
                    onClick = { showAddCategoryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = fieldColor,
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.form_add_tag), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Description field
            TaskTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = stringResource(R.string.form_description),
                singleLine = false,
                modifier = Modifier.height(100.dp),
                fieldColor = fieldColor,
                textColor = textColor,
                hintColor = hintColor,
                accentColor = accentColor
            )
            Spacer(Modifier.height(32.dp))

            // Create/Save button
            Button(
                onClick = {
                    if (isDateTimeValid) {
                        if (isEditMode && taskId != null) {
                            viewModel.updateTask(taskId)
                        } else {
                            viewModel.createTask()
                        }
                        navController.popBackStack()
                    } else {
                        showPastDateError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = fieldColor,
                    disabledContentColor = hintColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(if (isEditMode) R.string.form_save_changes else R.string.todo_create_task),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Add category dialog
    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(stringResource(R.string.dialog_add_category), color = textColor) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.dialog_category_name)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = fieldColor,
                        unfocusedContainerColor = fieldColor,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = fieldColor,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = hintColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCategory(newCategoryName)
                        viewModel.onCategoryChange(newCategoryName)
                        showAddCategoryDialog = false
                    }
                ) { Text(stringResource(R.string.dialog_add), color = accentColor) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel), color = hintColor)
                }
            },
            containerColor = dialogBgColor
        )
    }

    // Date picker dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(stringResource(R.string.dialog_ok), textStyle = TextStyle(color = accentColor))
            negativeButton(stringResource(R.string.dialog_cancel), textStyle = TextStyle(color = hintColor))
        }
    ) {
        datepicker(
            initialDate = date,
            title = stringResource(R.string.select_date),
            allowedDateValidator = { selectedDate ->
                !selectedDate.isBefore(LocalDate.now())
            },
            onDateChange = viewModel::onDateChange
        )
    }

    // Time picker dialog
    MaterialDialog(
        dialogState = timeDialogState,
        buttons = {
            positiveButton(stringResource(R.string.dialog_ok), textStyle = TextStyle(color = accentColor))
            negativeButton(stringResource(R.string.dialog_cancel), textStyle = TextStyle(color = hintColor))
        }
    ) {
        timepicker(
            initialTime = time,
            title = stringResource(R.string.select_time),
            onTimeChange = viewModel::onTimeChange
        )
    }
}

/**
 * TaskTextField
 *
 * Reusable outlined text field for task input.
 * Supports custom colors for theming.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    fieldColor: Color,
    textColor: Color,
    hintColor: Color,
    accentColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldColor,
            unfocusedContainerColor = fieldColor,
            disabledContainerColor = fieldColor,
            disabledBorderColor = fieldColor,
            disabledLabelColor = hintColor,
            disabledTextColor = textColor,
            disabledTrailingIconColor = hintColor,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = fieldColor,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = hintColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = accentColor,
            focusedTrailingIconColor = accentColor,
            unfocusedTrailingIconColor = hintColor,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorTrailingIconColor = MaterialTheme.colorScheme.error
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        trailingIcon = trailingIcon
    )
}
