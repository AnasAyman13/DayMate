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
// --- ✅ [الترجمة] ---
import androidx.compose.ui.res.stringResource
// --------------------
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// --- ✅ [الترجمة] ---
import com.day.mate.R
// --------------------
import com.day.mate.ui.theme.*
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: TodoViewModel,
    taskIdString: String?
) {
    val isEditMode = taskIdString != "new"
    val taskId = if (isEditMode) taskIdString?.toIntOrNull() else null

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

    val isButtonEnabled by remember(title) {
        derivedStateOf { title.isNotBlank() }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        TopAppBar(
            title = {
                Text(
                    text = stringResource(if (isEditMode) R.string.todo_edit_task else R.string.todo_create_task),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.desc_back_button),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = { Spacer(modifier = Modifier.width(68.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // --- Rest of the Screen (Scrollable) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {


            TaskTextField(value = title, onValueChange = viewModel::onTitleChange, label = stringResource(R.string.form_title))
            Spacer(Modifier.height(16.dp))

            Box {
                TaskTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    onValueChange = {},
                    label = stringResource(R.string.form_date),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, "") }
                )
                Spacer(modifier = Modifier.matchParentSize().clickable { dateDialogState.show() })
            }
            Spacer(Modifier.height(16.dp))

            Box {
                TaskTextField(
                    value = time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                    onValueChange = {},
                    label = stringResource(R.string.form_time),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.AccessTime, "") }
                )
                Spacer(modifier = Modifier.matchParentSize().clickable { timeDialogState.show() })
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.form_remind_me), modifier = Modifier.padding(start = 8.dp).weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Switch(
                    checked = remindMe,
                    onCheckedChange = viewModel::onRemindMeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = AppGold,
                        uncheckedThumbColor = DarkField,
                        uncheckedTrackColor = DarkTextHint
                    )
                )
            }
            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.form_category), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    CategoryButton(
                        text = category,
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.onCategoryChange(category) }
                    )
                }
                Button(
                    onClick = { showAddCategoryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkField, contentColor = DarkText),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.form_add_tag), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            TaskTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = stringResource(R.string.form_description),
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEditMode && taskId != null) {
                        viewModel.updateTask(taskId)
                    } else {
                        viewModel.createTask()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppGold,
                    contentColor = DarkBg,
                    disabledContainerColor = DarkField,
                    disabledContentColor = DarkTextHint
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


    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(stringResource(R.string.dialog_add_category), color = DarkText) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.dialog_category_name)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkField, unfocusedContainerColor = DarkField,
                        focusedBorderColor = AppGold, unfocusedBorderColor = DarkField,
                        focusedLabelColor = AppGold, unfocusedLabelColor = DarkTextHint,
                        focusedTextColor = DarkText, unfocusedTextColor = DarkText,
                        cursorColor = AppGold
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
                ) { Text(stringResource(R.string.dialog_add), color = AppGold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text(stringResource(R.string.dialog_cancel), color = DarkTextHint) }
            },
            containerColor = DarkBg
        )
    }

    MaterialDialog(dialogState = dateDialogState, buttons = {
        positiveButton(stringResource(R.string.dialog_ok), textStyle = TextStyle(color = AppGold))
        negativeButton(stringResource(R.string.dialog_cancel), textStyle = TextStyle(color = DarkTextHint))
    }) { datepicker(initialDate = date, title = stringResource(R.string.select_date), onDateChange = viewModel::onDateChange) }

    MaterialDialog(dialogState = timeDialogState, buttons = {
        positiveButton(stringResource(R.string.dialog_ok), textStyle = TextStyle(color = AppGold))
        negativeButton(stringResource(R.string.dialog_cancel), textStyle = TextStyle(color = DarkTextHint))
    }) { timepicker(initialTime = time, title = stringResource(R.string.select_time), onTimeChange = viewModel::onTimeChange) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkField, unfocusedContainerColor = DarkField,
            disabledContainerColor = DarkField, disabledBorderColor = DarkField,
            disabledLabelColor = DarkTextHint, disabledTextColor = DarkText,
            disabledTrailingIconColor = DarkTextHint,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = DarkField,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = DarkTextHint,
            focusedTextColor = DarkText, unfocusedTextColor = DarkText,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = DarkTextHint
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        trailingIcon = trailingIcon
    )
}

// (CategoryButton definition is in TasksScreen.kt)