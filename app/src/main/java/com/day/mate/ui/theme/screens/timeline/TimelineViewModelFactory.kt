package com.day.mate.ui.theme.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.day.mate.data.repository.PrayerRepository
import com.day.mate.data.repository.TodoRepository

class TimelineViewModelFactory(
    private val todoRepository: TodoRepository,
    private val prayerRepository: PrayerRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimelineViewModel(todoRepository, prayerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}