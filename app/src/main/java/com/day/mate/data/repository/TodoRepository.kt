package com.day.mate.data.repository

import com.day.mate.data.local.CategoryDao
import com.day.mate.data.local.CategoryEntity
import com.day.mate.data.local.TodoDao
import com.day.mate.data.local.TodoEntity
import com.day.mate.data.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepository(
    private val todoDao: TodoDao,
    private val categoryDao: CategoryDao
) {

    // --- Todo Functions ---
    fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { list ->
            list.map { entity -> entity.toModel() }
        }
    }

    suspend fun getTodoById(id: Int): Todo? {
        return todoDao.getTodoById(id)?.toModel()
    }

    suspend fun insert(todo: Todo): Int {
        val newId = todoDao.insert(todo.toEntity()).toInt()
        return newId
    }
    suspend fun markAllTasksAsDone(dateString: String) {
        todoDao.markAllTasksAsDoneByDate(dateString)
    }

    suspend fun update(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    suspend fun delete(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }
    suspend fun clearAllTodos() {
        todoDao.clearAll()
    }

    // --- Category Functions ---
    fun getAllCategories(): Flow<List<String>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.name }
        }
    }

    suspend fun insertCategory(name: String) {
        categoryDao.insert(CategoryEntity(name = name))
    }

    suspend fun isCategoryInUse(name: String): Boolean {
        return todoDao.countTasksWithCategory(name) > 0
    }

    suspend fun deleteCategory(name: String) {
        categoryDao.deleteCategoryByName(name)
    }
}



// --- Mapping functions ---
fun TodoEntity.toModel(): Todo {
    return Todo(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        category = category,
        date = date,
        time = time,
        remindMe = remindMe,
        isDone = isDone
    )
}

fun Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        category = category,
        date = date,
        time = time,
        remindMe = remindMe,
        isDone = isDone
    )
}
