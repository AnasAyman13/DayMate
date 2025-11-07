package com.day.mate.ui.theme.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.model.Todo
import com.day.mate.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

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
                title = titleValue,
                description = description.value.trim(),
                category = category.value,
                date = _date.value.toString(),
                time = _time.value.format(DateTimeFormatter.ofPattern("HH:mm")),
                remindMe = remindMe.value,
                isDone = false
            )
            repository.insert(newTodo)
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
                title = titleValue,
                description = description.value.trim(),
                category = category.value,
                date = _date.value.toString(),
                time = _time.value.format(DateTimeFormatter.ofPattern("HH:mm")),
                remindMe = remindMe.value,
                isDone = oldIsDoneStatus
            )
            repository.update(updatedTodo)
        }
    }

    fun toggleTodoDone(todo: Todo) {
        viewModelScope.launch {
            repository.update(todo.copy(isDone = !todo.isDone))
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }
}