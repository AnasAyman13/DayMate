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

    private val dao = VaultDatabase.getDatabase(app).vaultDao()
    private val repo = VaultRepository(dao)

    private val _selectedFilter = MutableStateFlow("الكل")
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

    /** 🎯 تطبيق الفلترة مع دعم العربي والإنجليزي */
    fun filteredItems(): List<VaultItem> {
        return when (selectedFilter.value) {
            "الصور", "Photos" -> items.value.filter { it.type == VaultType.PHOTO }
            "الفيديوهات", "Videos" -> items.value.filter { it.type == VaultType.VIDEO }
            "الصوتيات", "Audio" -> items.value.filter { it.type == VaultType.AUDIO }
            "المستندات", "Documents" -> items.value.filter { it.type == VaultType.DOCUMENT }
            "الكل", "All" -> items.value
            else -> items.value
        }
    }

    /** 🌐 دالة مساعدة لتحويل الفلتر من عربي لإنجليزي (اختياري) */
    private fun normalizeFilter(filter: String): String {
        return when (filter) {
            "الكل" -> "All"
            "الصور" -> "Photos"
            "الفيديوهات" -> "Videos"
            "الصوتيات" -> "Audio"
            "المستندات" -> "Documents"
            else -> filter
        }
    }

    /** 🌐 دالة مساعدة لتحويل الفلتر من إنجليزي لعربي (اختياري) */
    private fun localizeFilter(filter: String): String {
        return when (filter) {
            "All" -> "الكل"
            "Photos" -> "الصور"
            "Videos" -> "الفيديوهات"
            "Audio" -> "الصوتيات"
            "Documents" -> "المستندات"
            else -> filter
        }
    }
}