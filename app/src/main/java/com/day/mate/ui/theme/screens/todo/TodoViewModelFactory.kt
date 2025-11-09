package com.day.mate.ui.theme.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.day.mate.data.repository.TodoRepository

class TodoViewModelFactory(
    private val repository: TodoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
