package com.day.mate.data.local.media

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items")
    suspend fun getAllItems(): List<VaultItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<VaultItem>)

    @Update
    suspend fun updateItem(item: VaultItem)

    @Delete
    suspend fun deleteItem(item: VaultItem)

    // لحذف محتويات المجلد عند حذفه (اختياري بس مفيد)
    @Query("DELETE FROM vault_items WHERE parentId = :folderId")
    suspend fun deleteItemsInFolder(folderId: Int)
}