package com.day.mate.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY date DESC, time DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Int): TodoEntity?

    @Insert
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("DELETE FROM todos")
    suspend fun clearAll()
    @Query("UPDATE todos SET isDone = 1 WHERE date = :dateString AND isDone = 0")
    suspend fun markAllTasksAsDoneByDate(dateString: String)
    // (New Function)
    @Query("SELECT COUNT(*) FROM todos WHERE category = :categoryName")
    suspend fun countTasksWithCategory(categoryName: String): Int
}