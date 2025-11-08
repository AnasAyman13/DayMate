package com.day.mate.data

import androidx.room.TypeConverter

class VaultTypeConverter {
    @TypeConverter
    fun fromType(type: VaultType): String = type.name

    @TypeConverter
    fun toType(value: String): VaultType = VaultType.valueOf(value)
}
