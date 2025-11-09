package com.day.mate.data.model

data class Todo(
    // (سبناه زي ما هو عشان الـ AutoGenerate بتاع Room)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val date: String, // (e.g., "2025-11-07")
    val time: String, // (e.g., "13:30")
    val remindMe: Boolean,
    // (شيلنا اللينك)
    val isDone: Boolean
)