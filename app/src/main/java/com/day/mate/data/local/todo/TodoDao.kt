package com.day.mate.data.local.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.day.mate.data.local.todo.TodoEntity
import kotlinx.coroutines.flow.Flow

/**
 * TodoDao
 *
 * Data Access Object interface for managing [TodoEntity] records in the local Room database.
 * Provides methods for CRUD operations, query filtering, and bulk updates.
 */
@Dao
interface TodoDao {

    /**
     * Retrieves all [TodoEntity] records from the database, ordered by date (descending)
     * and time (descending).
     *
     * @return A Flow of the list of all Todo tasks.
     */
    @Query("SELECT * FROM todos ORDER BY date DESC, time DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    /**
     * Retrieves a single [TodoEntity] by its local primary key ID.
     *
     * @param id The local ID of the task.
     * @return The Todo task, or null if not found.
     */
    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Int): TodoEntity?

    /**
     * Inserts a new [TodoEntity] into the database.
     *
     * @param todo The task entity to insert.
     * @return The row ID of the newly inserted task.
     */
    @Insert
    suspend fun insert(todo: TodoEntity): Long

    /**
     * Updates an existing [TodoEntity] in the database.
     *
     * @param todo The task entity with updated values.
     */
    @Update
    suspend fun update(todo: TodoEntity)

    /**
     * Deletes a specific [TodoEntity] from the database.
     *
     * @param todo The task entity to delete.
     */
    @Delete
    suspend fun delete(todo: TodoEntity)

    /**
     * Deletes all [TodoEntity] records from the database.
     */
    @Query("DELETE FROM todos")
    suspend fun clearAll()

    /**
     * Marks all incomplete tasks ([isDone] = 0) for a specific date as completed ([isDone] = 1).
     *
     * @param dateString The date string (e.g., "2025-12-07") identifying tasks to update.
     */
    @Query("UPDATE todos SET isDone = 1 WHERE date = :dateString AND isDone = 0")
    suspend fun markAllTasksAsDoneByDate(dateString: String)

    /**
     * Counts the total number of tasks associated with a specific category name.
     * Used for validating if a category is in use before deletion.
     *
     * @param categoryName The name of the category to count tasks for.
     * @return The total count of tasks in that category.
     */
    @Query("SELECT COUNT(*) FROM todos WHERE category = :categoryName")
    suspend fun countTasksWithCategory(categoryName: String): Int
    @Query("SELECT * FROM todos WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getTodoByRemoteId(remoteId: String): TodoEntity?
    @Query("SELECT COUNT(id) FROM todos WHERE date = :dateString AND isDone = 0")
    suspend fun countIncompleteTasksByDate(dateString: String): Int
}