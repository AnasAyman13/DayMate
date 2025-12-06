package com.day.mate.data.local.todo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.day.mate.data.local.todo.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * CategoryDao
 *
 * Data Access Object interface for managing [CategoryEntity] records in the local Room database.
 * Handles category creation, retrieval, and deletion.
 */
@Dao
interface CategoryDao {

    /**
     * Retrieves all [CategoryEntity] records from the database, sorted alphabetically by name.
     *
     * @return A Flow of the list of all categories.
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Inserts a new [CategoryEntity] into the database.
     * If a category with the same primary key already exists, it is replaced.
     *
     * @param category The category entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(category: CategoryEntity)

    /**
     * Deletes a category based on its name.
     *
     * @param name The name of the category to delete.
     */
    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)
}