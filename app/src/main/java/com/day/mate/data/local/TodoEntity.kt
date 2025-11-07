package com.day.mate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "",
    val isDone: Boolean = false,

    // --- الحقول الجديدة ---
    val date: String = "",       // لتخزين التاريخ "10 July 2025"
    val time: String = "",       // لتخزين الوقت "12:30 - 13:00"
    val remindMe: Boolean = false, // للتذكير
    val link: String = ""          // للينك الاختياري
)