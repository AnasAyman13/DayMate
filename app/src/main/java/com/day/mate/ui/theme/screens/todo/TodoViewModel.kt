package com.day.mate.ui.theme.screens.todo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.reminder.ReminderScheduler
import com.day.mate.data.model.Todo
import com.day.mate.data.repository.TodoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TodoViewModel
 *
 * Manages todo tasks, acting as the bridge between the UI (Compose screens) and the data layer
 * (local Room database and Firebase Firestore synchronization). It handles all business logic
 * including CRUD operations, state management for the task creation form, and reminder scheduling.
 *
 * @property repository The data repository responsible for local (Room) and remote (Firestore) operations.
 */
class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val DEFAULT_CATEGORIES = listOf("General", "Study", "Work", "Personal")

    // ==========================================================
    // State Flows for UI Consumption
    // ==========================================================

    /**
     * Holds the current list of all Todo items fetched from the local database.
     */
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    /**
     * Holds the list of available task categories.
     */
    private val _categories = MutableStateFlow(emptyList<String>())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Task Form State Variables
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

    private var reminderScheduler: ReminderScheduler? = null

    init {
        // Start loading data immediately when the ViewModel is created
        loadTodos()
        loadCategories()
    }

    /**
     * Initializes the reminder scheduler component, which requires an application context.
     * Must be called by the Activity/Application context holder.
     *
     * @param context The application context required for scheduling alarms.
     */
    fun initReminderScheduler(context: Context) {
        reminderScheduler = ReminderScheduler(context)
    }

    /**
     * Loads all [Todo] items from the repository and collects them into the [_todos] StateFlow.
     * This is typically an observable Flow from the Room database.
     */
    private fun loadTodos() {
        viewModelScope.launch {
            repository.getAllTodos().collect { todoList ->
                _todos.value = todoList
            }
        }
    }

    /**
     * Loads all defined categories from the repository and collects them into the [_categories] StateFlow.
     * If no categories exist, the [DEFAULT_CATEGORIES] are automatically inserted.
     */
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

    /**
     * Inserts a new category into the local database if it is not blank and does not already exist.
     *
     * @param newCategory The name of the category to add.
     */
    fun addCategory(newCategory: String) {
        val trimmed = newCategory.trim()
        if (trimmed.isNotBlank() && _categories.value.none { it.equals(trimmed, ignoreCase = true) }) {
            viewModelScope.launch {
                repository.insertCategory(trimmed)
            }
        }
    }

    /**
     * Attempts to delete a category after performing necessary validations.
     * Deletion is blocked if the category is a default category or if it is currently in use by a task.
     *
     * @param name The name of the category to delete.
     * @param onError Lambda function called with a localized error string resource key if validation fails.
     */
    fun deleteCategory(name: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (DEFAULT_CATEGORIES.any { it.equals(name, ignoreCase = true) }) {
                onError("error_delete_default_category")
                return@launch
            }

            if (repository.isCategoryInUse(name)) {
                onError("error_delete_category_in_use")
                return@launch
            }

            repository.deleteCategory(name)

            // Reset the currently selected category if it was the one deleted
            if (_category.value.equals(name, ignoreCase = true)) {
                _category.value = "General"
            }
        }
    }

    // ==========================================================
    // Form State Update Functions
    // ==========================================================
    fun onTitleChange(newValue: String) { _title.value = newValue }
    fun onDescriptionChange(newValue: String) { _description.value = newValue }
    fun onCategoryChange(newValue: String) { _category.value = newValue }
    fun onRemindMeChange(newValue: Boolean) { _remindMe.value = newValue }

    /**
     * Updates the task date in the form state.
     * @param newDate The new date chosen by the user.
     */
    fun onDateChange(newDate: LocalDate) { _date.value = newDate }

    /**
     * Updates the task time in the form state.
     * @param newTime The new time chosen by the user.
     */
    fun onTimeChange(newTime: LocalTime) { _time.value = newTime }

    /**
     * Resets all task form state variables to their default, empty, or current time values.
     */
    fun clearForm() {
        _title.value = ""
        _description.value = ""
        _category.value = "General"
        _date.value = LocalDate.now()
        _time.value = LocalTime.now().plusHours(1) // Set a reasonable default time, e.g., one hour from now
        _remindMe.value = true
    }

    /**
     * Loads the details of an existing task into the form state for editing.
     *
     * @param id The local ID of the task to be loaded.
     */
    fun loadTaskById(id: Int) {
        viewModelScope.launch {
            val taskToEdit = repository.getTodoById(id)
            if (taskToEdit != null) {
                _title.value = taskToEdit.title
                _description.value = taskToEdit.description
                _category.value = taskToEdit.category
                _date.value = LocalDate.parse(taskToEdit.date)
                _time.value = try {
                    LocalTime.parse(taskToEdit.time)
                } catch (e: Exception) {
                    // Fallback to current time if parsing fails
                    LocalTime.now()
                }
                _remindMe.value = taskToEdit.remindMe
            }
        }
    }

    /**
     * Creates a new [Todo] task using the current form state.
     *
     * This operation involves:
     * 1. Inserting the task locally (to get a local ID).
     * 2. Synchronizing the task with Firestore (to get a remote ID).
     * 3. Updating the local task with the new remote ID.
     * 4. Scheduling a reminder if the [remindMe] flag is true.
     */
    fun createTask() {
        val titleValue = title.value.trim()
        if (titleValue.isEmpty()) return

        viewModelScope.launch {
            val newTodo = Todo(
                id = 0, // Placeholder for local ID
                remoteId = "", // Placeholder for remote ID
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

            // Update local entry with the remote ID
            repository.update(insertedTodo.copy(remoteId = remoteId))

            // Schedule reminder
            reminderScheduler?.schedule(insertedTodo.copy(remoteId = remoteId))
        }
    }

    /**
     * Updates an existing [Todo] task based on the current form state.
     *
     * This operation involves:
     * 1. Updating the task locally.
     * 2. Updating the task in Firestore.
     * 3. Cancelling the old reminder and scheduling a new one if required.
     *
     * @param id The local ID of the task to be updated.
     */
    fun updateTask(id: Int) {
        val titleValue = title.value.trim()
        if (titleValue.isEmpty()) return

        viewModelScope.launch {
            val oldTask = repository.getTodoById(id)
            // Preserve the original 'isDone' status and remote ID
            val oldIsDoneStatus = oldTask?.isDone ?: false
            val oldRemoteId = oldTask?.remoteId ?: ""

            val updatedTodo = Todo(
                id = id,
                remoteId = oldRemoteId,
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

            // Cancel old reminder
            reminderScheduler?.cancel(id)

            // Schedule new reminder if enabled
            if (updatedTodo.remindMe) {
                reminderScheduler?.schedule(updatedTodo)
            }
        }
    }

    /**
     * Adds a [Todo] task document to Firebase Firestore under the current user's collection.
     *
     * @param todo The task object to be added.
     * @return The generated remote document ID (Firestore ID). Returns an empty string if the user is not authenticated.
     */
    private suspend fun addTodoToFirestore(todo: Todo): String {
        val userId = auth.currentUser?.uid ?: return ""

        // Generate a new document reference to get a unique ID
        val docRef = db.collection("users")
            .document(userId)
            .collection("tasks")
            .document()

        val remoteId = docRef.id

        // Map task data for storage
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

    /**
     * Updates an existing [Todo] task document in Firestore using its remote ID.
     *
     * @param todo The task object containing the remote ID and updated fields.
     */
    private fun updateTodoInFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return

        val rId = todo.remoteId
        if (rId.isNullOrEmpty()) return

        val taskMap = mapOf(
            "remoteId" to rId,
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
            .document(rId)
            .set(taskMap) // set() performs an overwrite or creates if not existing
    }

    /**
     * Deletes a [Todo] task document from Firestore using its remote ID.
     *
     * @param todo The task object containing the remote ID.
     */
    private fun deleteTodoFromFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return

        val rId = todo.remoteId
        if (rId.isNullOrEmpty()) return // Safely check for remote ID presence

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(rId)
            .delete()
    }

    /**
     * Updates only the `isDone` status field for a task document in Firestore.
     *
     * @param todo The task object containing the remote ID and the new status.
     */
    private fun updateDoneStateInFirestore(todo: Todo) {
        val userId = auth.currentUser?.uid ?: return

        val rId = todo.remoteId
        if (rId.isNullOrEmpty()) return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(rId)
            .update("isDone", todo.isDone)
    }

    /**
     * Fetches all tasks from Firestore for the current user, clears the local database,
     * and inserts the remote tasks locally. This is a destructive sync operation.
     */
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
                id = 0, // ID will be generated upon local insertion
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

        // Clear local database to prepare for sync
        repository.clearAllTodos()

        // Insert tasks from Firestore and update with their new local IDs
        tasks.forEach { task ->
            val newId = repository.insert(task)
            repository.update(task.copy(id = newId))
        }
    }

    /**
     * Initiates the synchronization process to load all tasks from Firestore into the local database.
     */
    fun syncFromFirestore() {
        viewModelScope.launch {
            loadTasksFromFirestore()
        }
    }

    /**
     * Toggles the completion status (isDone) of a [Todo] task.
     * Updates the status locally and in Firestore.
     *
     * @param todo The task to toggle.
     */
    fun toggleTodoDone(todo: Todo) {
        viewModelScope.launch {
            val updated = todo.copy(isDone = !todo.isDone)
            repository.update(updated)
            updateDoneStateInFirestore(updated)
            if (updated.isDone) {
                reminderScheduler?.cancel(updated.id)
            } else {
                if (updated.remindMe) {
                    reminderScheduler?.schedule(updated)
                }
            }
        }
    }

    /**
     * Deletes a [Todo] task from both the local database and Firestore,
     * and cancels any associated reminder.
     *
     * @param todo The task to delete.
     */
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.delete(todo)
            deleteTodoFromFirestore(todo)
            reminderScheduler?.cancel(todo.id)
        }
    }
}