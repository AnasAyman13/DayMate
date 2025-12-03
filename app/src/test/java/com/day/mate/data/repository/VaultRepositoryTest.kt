package com.day.mate.data.repository

import com.day.mate.data.local.VaultDao
import com.day.mate.data.local.VaultItem
import com.day.mate.data.local.VaultType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private val dao = mockk<VaultDao>()
    private lateinit var repo: VaultRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = VaultRepository(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getItems returns list from dao`() = runTest {
        // Arrange
        val items = listOf(VaultItem(id = 1, uri = "u1", type = VaultType.PHOTO, name = "a"))
        coEvery { dao.getAllItems() } returns items

        // Act
        val emitted = repo.getItems()

        // Assert
        assertEquals(items, emitted)
    }

    @Test
    fun `addItems calls dao insertItems`() = runTest {
        // Arrange
        val items = listOf(VaultItem(id = 2, uri = "u2", type = VaultType.DOCUMENT, name = "doc"))
        coEvery { dao.insertItems(items) } returns Unit

        // Act
        repo.addItems(items)

        // Assert
        coVerify(exactly = 1) { dao.insertItems(items) }
    }

    @Test
    fun `deleteItem calls dao deleteItem`() = runTest {
        // Arrange
        val item = VaultItem(id = 3, uri = "u3", type = VaultType.AUDIO, name = "audio")
        coEvery { dao.deleteItem(item) } returns Unit

        // Act
        repo.deleteItem(item)

        // Assert
        coVerify(exactly = 1) { dao.deleteItem(item) }
    }
}