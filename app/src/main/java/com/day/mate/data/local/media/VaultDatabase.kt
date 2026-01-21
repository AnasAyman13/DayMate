package com.day.mate.data.local.media

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [VaultItem::class], version = 2, exportSchema = false)
@TypeConverters(VaultTypeConverter::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, VaultDatabase::class.java, "vault_database")
                    .fallbackToDestructiveMigration() // هيمسح الداتا القديمة عشان غيرنا الهيكل
                    .build().also { INSTANCE = it }
            }
        }
    }
}