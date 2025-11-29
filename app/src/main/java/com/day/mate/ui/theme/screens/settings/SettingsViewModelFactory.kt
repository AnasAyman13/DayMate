package com.day.mate.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class for creating SettingsViewModel instances.
 *
 * This factory is required because SettingsViewModel has a non-empty constructor
 * that requires a Context parameter. ViewModelProvider cannot instantiate it directly.
 *
 * Usage:
 * ```
 * val viewModel: SettingsViewModel by viewModels {
 *     SettingsViewModelFactory(applicationContext)
 * }
 * ```
 *
 * @property context Application context passed to SettingsViewModel
 */
class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create
     * @return A newly created ViewModel instance
     * @throws IllegalArgumentException if the ViewModel class is not SettingsViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}