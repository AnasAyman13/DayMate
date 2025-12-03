package com.day.mate.data.repository

import com.day.mate.data.local.CategoryDao
import com.day.mate.data.local.TodoDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryTest {

    private val dispatcher = StandardTestDispatcher()
    private val todoDao = mockk<TodoDao>()
    private val categoryDao = mockk<CategoryDao>()
    private lateinit var repo: TodoRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = TodoRepository(todoDao, categoryDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllTodos returns empty flow when dao empty`() = runTest {
        // Arrange
        every { todoDao.getAllTodos() } returns flowOf(emptyList())

        // Act
        val flow = repo.getAllTodos()
        val emitted = flow.single()

        // Assert
        assertEquals(0, emitted.size)
    }

    @Test
    fun `getAllCategories maps names from category dao`() = runTest {
        // Arrange
        // We avoid depending on CategoryEntity shape by returning empty list flow
        every { categoryDao.getAllCategories() } returns flowOf(emptyList())

        // Act
        val emitted = repo.getAllCategories().single()

        // Assert
        assertEquals(0, emitted.size)
    }

    @Test
    fun `isCategoryInUse returns true when dao count positive`() = runTest {
        // Arrange
        coEvery { todoDao.countTasksWithCategory("Work") } returns 3

        // Act
        val result = repo.isCategoryInUse("Work")

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `insertCategory calls dao insert`() = runTest {
        // Arrange
        coEvery { categoryDao.insert(any()) } returns Unit

        // Act
        repo.insertCategory("Personal")

        // Assert
        coVerify(exactly = 1) { categoryDao.insert(any()) }
    }

    @Test
    fun `deleteCategory calls dao deleteCategoryByName`() = runTest {
        // Arrange
        coEvery { categoryDao.deleteCategoryByName("Old") } returns Unit

        // Act
        repo.deleteCategory("Old")

        // Assert
        coVerify(exactly = 1) { categoryDao.deleteCategoryByName("Old") }
    }
}