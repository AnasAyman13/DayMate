package com.day.mate.ui.theme.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.day.mate.data.repository.TodoRepository
import java.lang.IllegalArgumentException

/**
 * Factory for creating [TodoViewModel] instances.
 *
 * This is necessary because the [TodoViewModel] has dependencies (specifically [TodoRepository])
 * that must be injected at creation time, which standard ViewModel constructors do not support.
 *
 * @property repository The repository dependency to be passed to the ViewModel.
 */
class TodoViewModelFactory(
    private val repository: TodoRepository
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given [ViewModel] class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return The created ViewModel instance.
     * @throws IllegalArgumentException if the requested ViewModel class is not [TodoViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}