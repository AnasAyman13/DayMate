package com.day.mate.data.local.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CategoryEntity
 *
 * Data class representing a category (or tag) used to group Todo tasks.
 * Stored in the local Room database.
 *
 * @property id Primary key for the Room database (auto-generated).
 * @property name The unique name of the category (e.g., "Work", "Study").
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)