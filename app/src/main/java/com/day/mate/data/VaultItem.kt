package com.day.mate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey val id: Int,
    val uri: String,
    val type: VaultType
)

enum class VaultType {
    PHOTO, VIDEO, DOCUMENT
}