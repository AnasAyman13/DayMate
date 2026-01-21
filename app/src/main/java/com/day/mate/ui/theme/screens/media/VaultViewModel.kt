package com.day.mate.ui.theme.screens.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.media.VaultDatabase
import com.day.mate.data.local.media.VaultItem
import com.day.mate.data.local.media.VaultType
import com.day.mate.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = VaultDatabase.getDatabase(app).vaultDao()
    private val repo = VaultRepository(dao)

    private val _currentFolderId = MutableStateFlow<Int?>(null)
    val currentFolderId = _currentFolderId.asStateFlow()

    private val _items = MutableStateFlow<List<VaultItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _foldersOnly = MutableStateFlow<List<VaultItem>>(emptyList())
    val foldersOnly = _foldersOnly.asStateFlow()

    private val _selectedFilter = MutableStateFlow("الكل")
    val selectedFilter = _selectedFilter.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val allItems = repo.getItems()
            // 1. نجلب العناصر الموجودة في المسار الحالي فقط
            val itemsInCurrentFolder = allItems.filter { it.parentId == _currentFolderId.value }

            // 2. نطبق الفلتر المختار
            _items.value = filterList(itemsInCurrentFolder)

            // 3. نحدث قائمة المجلدات المتاحة للنقل (كل المجلدات ماعدا الحالي لتجنب الأخطاء)
            _foldersOnly.value = allItems.filter { it.isFolder && it.id != _currentFolderId.value }
        }
    }

    // 🔥 دالة التحقق من تكرار الاسم (Case Insensitive)
    fun isNameTaken(name: String): Boolean {
        return _items.value.any {
            it.name.trim().equals(name.trim(), ignoreCase = true)
        }
    }

    // 🔥🔥 منطق الفلترة الجديد بناءً على طلبك 🔥🔥
    private fun filterList(list: List<VaultItem>): List<VaultItem> {
        val filter = _selectedFilter.value

        return when (filter) {
            "الكل", "All" -> list // يظهر المجلدات والملفات

            "مجلدات", "Folders" -> list.filter { it.isFolder } // يظهر المجلدات فقط

            "ملفات", "Files" -> list.filter { !it.isFolder && it.type == VaultType.DOCUMENT } // ملفات فقط (بدون مجلدات)

            "الصور", "Photos" -> list.filter { !it.isFolder && it.type == VaultType.PHOTO } // صور فقط (بدون مجلدات)

            "الفيديوهات", "Videos" -> list.filter { !it.isFolder && it.type == VaultType.VIDEO } // فيديو فقط (بدون مجلدات)

            "الصوتيات", "Audio" -> list.filter { !it.isFolder && it.type == VaultType.AUDIO } // صوت فقط (بدون مجلدات)

            else -> list
        }
    }

    fun openFolder(folderId: Int) {
        _currentFolderId.value = folderId
        // عند الدخول لمجلد، نرجع الفلتر لـ "الكل" عشان المستخدم يشوف المحتوى
        _selectedFilter.value = if(_selectedFilter.value.matches(Regex("[A-Za-z]+"))) "All" else "الكل"
        loadItems()
    }

    fun goBack(): Boolean {
        if (_currentFolderId.value != null) {
            _currentFolderId.value = null
            loadItems()
            return true
        }
        return false
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = VaultItem(
                uri = "", type = VaultType.DOCUMENT,
                name = name.trim(),
                isFolder = true,
                parentId = _currentFolderId.value
            )
            repo.addItems(listOf(folder))
            loadItems()
        }
    }

    fun addItems(newItems: List<VaultItem>) {
        viewModelScope.launch {
            // 🔥 معالجة الأسماء المكررة وإضافة رقم تلقائي (1), (2)
            val currentNames = _items.value.map { it.name.lowercase() }
            val processedItems = newItems.map { item ->
                var finalName = item.name
                // قص الاسم لو طويل جداً (أكتر من 50) للحفاظ على التنسيق
                if (finalName.length > 50) {
                    val ext = finalName.substringAfterLast(".", "")
                    val base = finalName.substringBeforeLast(".")
                    finalName = base.take(45) + if(ext.isNotEmpty()) ".$ext" else ""
                }

                var count = 1
                while (currentNames.contains(finalName.lowercase())) {
                    val nameWithoutExt = item.name.substringBeforeLast(".")
                    val ext = item.name.substringAfterLast(".", "")
                    val dot = if(ext.isNotEmpty()) "." else ""
                    finalName = "$nameWithoutExt ($count)$dot$ext"
                    count++
                }
                item.copy(name = finalName, parentId = _currentFolderId.value)
            }
            repo.addItems(processedItems)
            loadItems()
        }
    }

    fun renameItem(item: VaultItem, newName: String) {
        viewModelScope.launch {
            repo.updateItem(item.copy(name = newName.trim()))
            loadItems()
        }
    }

    fun moveItemToFolder(item: VaultItem, targetFolderId: Int?) {
        viewModelScope.launch {
            repo.updateItem(item.copy(parentId = targetFolderId))
            loadItems()
        }
    }

    fun removeItem(item: VaultItem) {
        viewModelScope.launch {
            repo.deleteItem(item)
            loadItems()
        }
    }

    fun selectFilter(filter: String) {
        _selectedFilter.value = filter
        loadItems()
    }
}