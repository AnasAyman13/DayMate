package com.day.mate.data.repository

import com.day.mate.data.local.media.VaultDao
import com.day.mate.data.local.media.VaultItem

class VaultRepository(private val dao: VaultDao) {
    suspend fun getItems() = dao.getAllItems()
    suspend fun addItems(items: List<VaultItem>) = dao.insertItems(items)
    suspend fun updateItem(item: VaultItem) = dao.updateItem(item) // دالة جديدة
    suspend fun deleteItem(item: VaultItem) {
        dao.deleteItem(item)
        if (item.isFolder) {
            dao.deleteItemsInFolder(item.id) // حذف ما بداخل المجلد
        }
    }
}