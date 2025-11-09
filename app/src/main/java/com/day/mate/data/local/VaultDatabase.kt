package com.day.mate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [VaultItem::class], version = 1, exportSchema = false) // ✅ exportSchema = false
@TypeConverters(VaultTypeConverter::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vault_database"
                )
                    .fallbackToDestructiveMigration() // 🚨 الحل: السماح بحذف القاعدة وإعادة بنائها
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}