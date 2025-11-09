package com.day.mate.ui.theme.screens.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.VaultDatabase
import com.day.mate.data.local.VaultItem
import com.day.mate.data.repository.VaultRepository
import com.day.mate.data.local.VaultType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = VaultDatabase.Companion.getDatabase(app).vaultDao()
    private val repo = VaultRepository(dao)

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _items = MutableStateFlow<List<VaultItem>>(emptyList())
    val items = _items.asStateFlow()

    init {
        loadItems()
    }

    /** 🔄 تحميل كل العناصر من قاعدة البيانات */
    private fun loadItems() {
        viewModelScope.launch {
            _items.value = repo.getItems()
        }
    }

    /** ➕ إضافة عناصر جديدة */
    fun addItems(newItems: List<VaultItem>) {
        viewModelScope.launch {
            repo.addItems(newItems)
            loadItems()
        }
    }

    /** 🗑️ حذف عنصر */
    fun removeItem(item: VaultItem) {
        viewModelScope.launch {
            repo.deleteItem(item)
            loadItems()
        }
    }

    /** 🏷️ اختيار الفلتر */
    fun selectFilter(filter: String) {
        _selectedFilter.value = filter
    }
/** 🎯 تطبيق الفلترة */
    fun filteredItems(): List<VaultItem> {
        return when (selectedFilter.value) {
            "Photos" -> items.value.filter { it.type == VaultType.PHOTO }
            "Videos" -> items.value.filter { it.type == VaultType.VIDEO }
            "Documents" -> items.value.filter { it.type == VaultType.DOCUMENT }
            else -> items.value
        }
    }
}