package com.day.mate.data.local.media

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Id تلقائي
    val uri: String,
    val type: VaultType,
    val name: String = "Untitled",
    val isFolder: Boolean = false, // هل هو مجلد؟
    val parentId: Int? = null      // رقم المجلد الأب (لو null يبقى في الرئيسية)
)

enum class VaultType {
    PHOTO, VIDEO, AUDIO, DOCUMENT
}

// محول الأنواع للـ Room
