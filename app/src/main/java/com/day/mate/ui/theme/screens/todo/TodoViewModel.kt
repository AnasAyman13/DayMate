package com.day.mate.ui.theme.screens.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.model.Todo
import com.day.mate.data.repository.TodoRepository
import com.day.mate.reminder.TaskReminderReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val DEFAULT_CATEGORIES = listOf("General", "Study", "Work", "Personal")

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _categories = MutableStateFlow(emptyList<String>())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()
    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()
    private val _time = MutableStateFlow(LocalTime.now())
    val time: StateFlow<LocalTime> = _time.asStateFlow()
    private val _remindMe = MutableStateFlow(true)
    val remindMe: StateFlow<Boolean> = _remindMe.asStateFlow()


    init {
        loadTodos()
        loadCategories()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            repository.getAllTodos().collect { todoList ->
                _todos.value = todoList
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
                if (categoryList.isEmpty()) {
                    DEFAULT_CATEGORIES.forEach { addCategory(it) }
                }
            }
        }
    }

    fun addCategory(newCategory: String) {
        val trimmed = newCategory.trim()
        if (trimmed.isNotBlank() && _categories.value.none { it.equals(trimmed, ignoreCase = true) }) {
            viewModelScope.launch {
                repository.insertCategory(trimmed)
            }
        }
    }

    fun deleteCategory(name: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (DEFAULT_CATEGORIES.any { it.equals(name, ignoreCase = true) }) {
                onError("error_delete_default_category") // (Using string key)
                return@launch
            }

            if (repository.isCategoryInUse(name)) {
                onError("error_delete_category_in_use") // (Using string key)
                return@launch
            }

            repository.deleteCategory(name)

            if (_category.value.equals(name, ignoreCase = true)) {
                _category.value = "General"
            }
        }
    }

    fun onTitleChange(newValue: String) { _title.value = newValue }
    fun onDescriptionChange(newValue: String) { _description.value = newValue }
    fun onCategoryChange(newValue: String) { _category.value = newValue }
    fun onRemindMeChange(newValue: Boolean) { _remindMe.value = newValue }
    fun onDateChange(newDate: LocalDate) { _date.value = newDate }
    fun onTimeChange(newTime: LocalTime) { _time.value = newTime }

    fun clearForm() {
        _title.value = ""
        _description.value = ""
        _category.value = "General"
        _date.value = LocalDate.now()
        _time.value = LocalTime.now().plusHours(1)
        _remindMe.value = true
    }

    fun loadTaskById(id: Int) {
        viewModelScope.launch {
            val taskToEdit = repository.getTodoById(id)
            if (taskToEdit != null) {
                _title.value = taskToEdit.title
                _description.value = taskToEdit.description
                _category.value = taskToEdit.category
                _date.value = LocalDate.parse(taskToEdit.date)
                _time.value = try { LocalTime.parse(taskToEdit.time) } catch (e: Exception) { LocalTime.now() }
                _remindMe.value = taskToEdit.remindMe
            }
        }
    }

    fun createTask() {
        val titleValue = title.value.trim()
        if (titleValue.isEmpty()) return

        viewModelScope.launch {
            val newTodo = Todo(
                id = 0,
                remoteId = "",
                title = titleValue,
                description = description.value.trim(),
                category = category.value,
                date = _date.value.toString(),
                time = _time.value.format(DateTimeFormatter.ofPattern("HH:mm")),
                remindMe = remindMe.value,
                isDone = false
            )

            val newLocalId = repository.insert(newTodo)

            val insertedTodo = newTodo.copy(id = newLocalId)

            val remoteId = addTodoToFirestore(insertedTodo)

            repository.update(insertedTodo.copy(remoteId = remoteId))
        }
    }

    fun updateTask(id: Int) {
        val titleValue = title.value.trim()
        if (titleValue.isEmpty()) return

        viewModelScope.launch {
            val oldTask = repository.getTodoById(id)
            val oldIsDoneStatus = oldTask?.isDone ?: false

            val updatedTodo = Todo(
                id = id,
                remoteId = oldTask?.remoteId ?: "",
                title = titleValue,
                description = description.value.trim(),
                category = category.value,
                date = _date.value.toString(),
                time = _time.value.format(DateTimeFormatter.ofPattern("HH:mm")),
                remindMe = remindMe.value,
                isDone = oldIsDoneStatus
            )
            repository.update(updatedTodo)
            updateTodoInFirestore(updatedTodo)
        }
    }

    private suspend fun addTodoToFirestore(todo: Todo): String {
        val userId = auth.currentUser?.uid ?: return ""

        val docRef = db.collection("users")
            .document(userId)
            .collection("tasks")
            .document()

        val remoteId = docRef.id

        val data = mapOf(
            "remoteId" to remoteId,
            "title" to todo.title,
            "description" to todo.description,
            "category" to todo.category,
            "date" to todo.date,
            "time" to todo.time,
            "remindMe" to todo.remindMe,
            "isDone" to todo.isDone
        )

        docRef.set(data).await()

        return remoteId
    }

    private fun updateTodoInFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return
        if (todo.remoteId.isEmpty()) return

        val taskMap = mapOf(
            "remoteId" to todo.remoteId,
            "title" to todo.title,
            "description" to todo.description,
            "category" to todo.category,
            "date" to todo.date,
            "time" to todo.time,
            "remindMe" to todo.remindMe,
            "isDone" to todo.isDone
        )

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(todo.remoteId)
            .set(taskMap)
    }
    private fun deleteTodoFromFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return
        val remoteId = todo.remoteId

        if (remoteId.isEmpty()) return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(remoteId)
            .delete()
    }
    private fun updateDoneStateInFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return
        val remoteId = todo.remoteId
        if (remoteId.isEmpty()) return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(remoteId)
            .update("isDone", todo.isDone)
    }

    private suspend fun loadTasksFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = db.collection("users")
            .document(userId)
            .collection("tasks")
            .get()
            .await()

        val tasks = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            Todo(
                id = 0,
                remoteId = data["remoteId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "",
                date = data["date"] as? String ?: "",
                time = data["time"] as? String ?: "",
                remindMe = data["remindMe"] as? Boolean ?: false,
                isDone = data["isDone"] as? Boolean ?: false
            )
        }


        repository.clearAllTodos()

        tasks.forEach { task ->
            val newId = repository.insert(task)
            repository.update(task.copy(id = newId))
        }
    }

     fun syncFromFirestore() {
        viewModelScope.launch {
            loadTasksFromFirestore()
        }
    }

    fun toggleTodoDone(todo: Todo) {
        viewModelScope.launch {
            val updated = todo.copy(isDone = !todo.isDone)
            repository.update(updated)
            updateDoneStateInFirestore(updated)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.delete(todo)
            deleteTodoFromFirestore(todo)
        }
    }

    fun scheduleReminder(context: Context, todo: Todo) {
        try {
            if (!todo.remindMe) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val localDate = LocalDate.parse(todo.date)
            val localTime = LocalTime.parse(todo.time)

            // تحويل التاريخ والوقت إلى milliseconds
            val triggerTime = LocalDateTime.of(localDate, localTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("title", todo.title)
                putExtra("description", todo.description)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todo.id,   // مهم يكون unique
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(context: Context, todo: Todo) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, TaskReminderReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todo.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}