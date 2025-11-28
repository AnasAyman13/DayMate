package com.day.mate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey val id: Int,
    val uri: String,
    val type: VaultType,
    val name: String = "Untitled"
)

enum class VaultType {
    PHOTO,
    VIDEO,
    AUDIO,  // ✅ إضافة
    DOCUMENT
}