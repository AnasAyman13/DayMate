@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.day.mate.R
import com.day.mate.ui.theme.AppGold
import java.time.*
import java.util.Locale
import java.time.format.DateTimeFormatter

// دالة مساعدة لترجمة الكاتيجوري
@Composable
fun getCategoryLabel(category: String): String {
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
fun CreateTaskScreen(
    navController: NavController,
    viewModel: TodoViewModel,
    taskIdString: String?
) {
    val isEditMode = taskIdString != "new"
    val taskId = if (isEditMode) taskIdString?.toIntOrNull() else null

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val fieldColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = AppGold

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val isDateTimeValid by remember(date, time) {
        derivedStateOf {
            val selectedDateTime = LocalDateTime.of(date, time)
            !selectedDateTime.isBefore(LocalDateTime.now())
        }
    }

    val isButtonEnabled by remember(title, isDateTimeValid) {
        derivedStateOf { title.isNotBlank() && isDateTimeValid }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isEditMode) R.string.todo_edit_task else R.string.todo_create_task),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            TaskTextField(
                value = title, onValueChange = viewModel::onTitleChange,
                label = stringResource(R.string.form_title),
                fieldColor = fieldColor, textColor = textColor, hintColor = hintColor, accentColor = accentColor
            )

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                TaskTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    onValueChange = {}, label = stringResource(R.string.form_date),
                    enabled = false, trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = accentColor) },
                    isError = !isDateTimeValid,
                    fieldColor = fieldColor, textColor = textColor, hintColor = hintColor, accentColor = accentColor
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.clickable { showTimePicker = true }) {
                TaskTextField(
                    value = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())),
                    onValueChange = {}, label = stringResource(R.string.form_time),
                    enabled = false, trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = accentColor) },
                    isError = !isDateTimeValid,
                    fieldColor = fieldColor, textColor = textColor, hintColor = hintColor, accentColor = accentColor
                )
            }

            if (!isDateTimeValid) {
                Text(
                    text = stringResource(R.string.error_past_date),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.form_remind_me), modifier = Modifier.weight(1f), color = textColor)
                Switch(
                    checked = remindMe, onCheckedChange = viewModel::onRemindMeChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = accentColor, checkedThumbColor = Color.White)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.form_category), fontWeight = FontWeight.Bold, color = textColor)
            Row(
                Modifier.padding(vertical = 10.dp).horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(getCategoryLabel(cat)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.Black,
                            labelColor = hintColor
                        )
                    )
                }
                Box(
                    modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.2f), CircleShape).clickable { showAddCategoryDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            TaskTextField(
                value = description, onValueChange = viewModel::onDescriptionChange,
                label = stringResource(R.string.form_description),
                singleLine = false, modifier = Modifier.height(120.dp),
                fieldColor = fieldColor, textColor = textColor, hintColor = hintColor, accentColor = accentColor
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEditMode && taskId != null) viewModel.updateTask(taskId)
                    else viewModel.createTask()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
            ) {
                Text(stringResource(if (isEditMode) R.string.form_save_changes else R.string.todo_create_task), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // 🔥 سكرول زيادة 160dp لضمان الراحة عند الكتابة أو وجود بارات النظام
            Spacer(Modifier.height(160.dp))
        }
    }

    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(stringResource(R.string.dialog_add_category), color = textColor) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { if (it.length <= 30) newCategoryName = it },
                    label = { Text(stringResource(R.string.dialog_category_name)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedLabelColor = accentColor),
                    singleLine = true,
                    supportingText = { Text("${newCategoryName.length}/30", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(newCategoryName)
                        viewModel.onCategoryChange(newCategoryName)
                        showAddCategoryDialog = false
                    }
                }) { Text(stringResource(R.string.dialog_add), color = accentColor) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text(stringResource(R.string.dialog_cancel), color = hintColor) }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(yearRange = 2020..2060)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.onDateChange(selectedDate)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.dialog_ok), color = accentColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.dialog_cancel), color = hintColor) }
            }
        ) {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DatePicker(state = datePickerState)
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = LocalTime.now().hour, initialMinute = LocalTime.now().minute)

        // استخدام Dialog بدلاً من AlertDialog لتجنب القيود في الـ Landscape
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .padding(24.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // 🔥 سكرول داخلي خاص باختيار التوقيت للاندسكيب
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.select_time), Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(Modifier.height(20.dp))

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = accentColor,
                            periodSelectorSelectedContainerColor = accentColor,
                            periodSelectorSelectedContentColor = Color.Black
                        )
                    )

                    Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.dialog_cancel), color = hintColor) }
                        TextButton(onClick = {
                            viewModel.onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        }) { Text(stringResource(R.string.dialog_ok), color = accentColor, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    modifier: Modifier = Modifier, enabled: Boolean = true, singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null, isError: Boolean = false,
    fieldColor: Color, textColor: Color, hintColor: Color, accentColor: Color
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, enabled = enabled, isError = isError,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine, trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldColor, unfocusedContainerColor = fieldColor,
            disabledContainerColor = fieldColor, focusedBorderColor = accentColor,
            unfocusedBorderColor = Color.Transparent, disabledBorderColor = Color.Transparent,
            focusedLabelColor = accentColor, unfocusedLabelColor = hintColor, disabledLabelColor = hintColor,
            focusedTextColor = textColor, unfocusedTextColor = textColor, disabledTextColor = textColor,
            cursorColor = accentColor, disabledTrailingIconColor = accentColor
        )
    )
}