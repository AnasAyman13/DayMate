package com.day.mate.data.repository

import com.day.mate.data.local.todo.CategoryDao
import com.day.mate.data.local.todo.CategoryEntity
import com.day.mate.data.local.todo.TodoDao
import com.day.mate.data.local.todo.TodoEntity
import com.day.mate.data.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TodoRepository
 *
 * Repository for managing Todo and Category data.
 * Acts as a single source of truth between DAO and ViewModel.
 */
class TodoRepository(
    private val todoDao: TodoDao,
    private val categoryDao: CategoryDao
) {

    // ========== Todo Functions ==========

    /**
     * Get all todos as Flow
     */
    fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { list ->
            list.map { entity -> entity.toModel() }
        }
    }

    /**
     * Get todo by ID
     */
    suspend fun getTodoById(id: Int): Todo? {
        return todoDao.getTodoById(id)?.toModel()
    }

    /**
     * Insert new todo and return generated ID
     */
    suspend fun insert(todo: Todo): Int {
        val newId = todoDao.insert(todo.toEntity())
        return newId.toInt()
    }

    /**
     * Mark all tasks as done for a specific date
     */
    suspend fun markAllTasksAsDone(dateString: String) {
        todoDao.markAllTasksAsDoneByDate(dateString)
    }

    /**
     * Update existing todo
     */
    suspend fun update(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    /**
     * Delete todo
     */
    suspend fun delete(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }

    /**
     * Clear all todos
     */
    suspend fun clearAllTodos() {
        todoDao.clearAll()
    }

    // ========== Category Functions ==========

    /**
     * Get all categories as Flow
     */
    fun getAllCategories(): Flow<List<String>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.name }
        }
    }

    /**
     * Insert new category
     */
    suspend fun insertCategory(name: String) {
        categoryDao.insert(CategoryEntity(name = name))
    }

    /**
     * Check if category is in use by any todo
     */
    suspend fun isCategoryInUse(name: String): Boolean {
        return todoDao.countTasksWithCategory(name) > 0
    }

    /**
     * Delete category by name
     */
    suspend fun deleteCategory(name: String) {
        categoryDao.deleteCategoryByName(name)
    }
}

// ========== Mapping Functions ==========

/**
 * Convert TodoEntity to Todo model
 */
fun TodoEntity.toModel(): Todo {
    return Todo(
        id = id.toInt(),
        remoteId = remoteId ?: "", // Handle null from DB
        title = title,
        description = description,
        category = category,
        date = date,
        time = time,
        remindMe = remindMe,
        isDone = isDone
    )
}

/**
 * Convert Todo model to TodoEntity
 */
fun Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id.toLong(),
        remoteId = remoteId ?: "",
        title = title,
        description = description,
        category = category,
        date = date,
        time = time,
        remindMe = remindMe,
        isDone = isDone
    )
}