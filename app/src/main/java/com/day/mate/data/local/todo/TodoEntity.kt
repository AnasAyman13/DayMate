package com.day.mate.data.local.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TodoEntity
 *
 * Data class representing a single to-do task entry in the local SQLite database
 * managed by Room. This entity includes fields for synchronization and scheduling.
 *
 * @property id Primary key for the Room database (auto-generated).
 * @property remoteId Identifier used for synchronizing with a remote database (e.g., Firebase Firestore).
 * @property title The main title or name of the task.
 * @property description Detailed description of the task.
 * @property category The category or tag assigned to the task (e.g., Work, Study).
 * @property isDone Completion status of the task (true if completed).
 * @property date The scheduled date for the task (stored as an ISO string, e.g., "2025-12-07").
 * @property time The scheduled time for the task (stored as a time string, e.g., "12:30").
 * @property remindMe Flag indicating whether a reminder/notification should be scheduled for this task.
 * @property link Optional external link or reference associated with the task.
 */
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = "",
    val title: String,
    val description: String,
    val category: String = "",
    val isDone: Boolean = false,

    // --- Scheduling and Optional Fields ---
    val date: String = "",
    val time: String = "",
    val remindMe: Boolean = false,
    val link: String = ""
)