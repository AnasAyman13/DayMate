package com.day.mate.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VaultRepository(private val dao: VaultDao) {

    suspend fun getItems(): List<VaultItem> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

    suspend fun addItems(items: List<VaultItem>) = withContext(Dispatchers.IO) {
        dao.insertItems(items)
    }

    suspend fun deleteItem(item: VaultItem) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
    }
}
