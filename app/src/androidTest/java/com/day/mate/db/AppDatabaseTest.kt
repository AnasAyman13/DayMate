package com.day.mate.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android instrumentation tests for Room database wiring.
 *
 * - Uses ApplicationProvider.getApplicationContext()
 * - Uses Room.inMemoryDatabaseBuilder()
 * - Avoids JVM-only APIs in androidTest.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var todoDao: TodoDao
    private lateinit var categoryDao: CategoryDao
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        todoDao = db.todoDao()
        categoryDao = db.categoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun databaseAndDaosAvailable() {
        assertNotNull(db)
        assertNotNull(todoDao)
        assertNotNull(categoryDao)
    }

    @Test
    fun daoQueriesReturnEmptyOnFreshDb() = runBlocking {
        // getAllTodos returns Flow<List<TodoEntity>>
        val todos = todoDao.getAllTodos().first()
        assert(todos.isEmpty())

        // getAllCategories returns Flow<List<CategoryEntity>>
        val cats = categoryDao.getAllCategories().first()
        assert(cats.isEmpty())
    }
}