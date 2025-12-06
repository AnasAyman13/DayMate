package com.day.mate.data.local.media

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items")
    suspend fun getAllItems(): List<VaultItem>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertItems(items: List<VaultItem>)

    @Delete
    suspend fun deleteItem(item: VaultItem)
}