package com.day.mate.data.model


import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

// Minimal model classes — adapt names/fields to your real models if necessary.
data class TimelineEvent(val id: Long = 0, val title: String, val timestamp: Long) {
    fun isPast(nowMillis: Long = System.currentTimeMillis()): Boolean = timestamp < nowMillis
}

data class Todo(val id: Long = 0, val title: String, val completed: Boolean = false, val dueAt: Long? = null) {
    fun isOverdue(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val due = dueAt ?: return false
        return due < nowMillis && !completed
    }
}

class TimelineEventAndTodoTest {

    @Test
    fun `timeline event is past when timestamp earlier than now`() {
        val now = Instant.now().toEpochMilli()
        val past = now - 1000L
        val event = TimelineEvent(1, "e", past)
        assertTrue(event.isPast(now))
    }

    @Test
    fun `todo is overdue only when due in past and not completed`() {
        val now = Instant.now().toEpochMilli()
        val overdueTodo = Todo(id = 1, title = "t", completed = false, dueAt = now - 1000L)
        val doneTodo = overdueTodo.copy(completed = true)
        val futureTodo = Todo(id = 2, title = "f", completed = false, dueAt = now + 10_000L)

        assertTrue(overdueTodo.isOverdue(now))
        assertFalse(doneTodo.isOverdue(now))
        assertFalse(futureTodo.isOverdue(now))
    }

    @Test
    fun `todo without dueAt is not overdue`() {
        val todo = Todo(id = 3, title = "x", completed = false, dueAt = null)
        assertFalse(todo.isOverdue())
    }
}