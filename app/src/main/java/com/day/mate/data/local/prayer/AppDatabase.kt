package com.day.mate.data.local.prayer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.day.mate.data.local.todo.CategoryDao
import com.day.mate.data.local.todo.CategoryEntity
import com.day.mate.data.local.todo.TodoDao
import com.day.mate.data.local.todo.TodoEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Database(
    entities = [TodoEntity::class, CategoryEntity::class],
    version = 3, // (Or +1 from your last version)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao
    abstract fun categoryDao(): CategoryDao // (Added the new Dao)

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

object RetrofitInstance {
    val api: PrayerApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrayerApiService::class.java)
    }
}