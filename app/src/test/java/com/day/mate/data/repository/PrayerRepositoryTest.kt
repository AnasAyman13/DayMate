package com.day.mate.data.repository


import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Minimal interface used by the repository in tests.
// If your real PrayerApiService has different names/signatures, update these tests accordingly.
interface PrayerApiService {
    suspend fun getDailyPrayers(): List<String>
}

class PrayerRepository(private val api: PrayerApiService) {
    suspend fun fetchDailyPrayers(): Result<List<String>> =
        try {
            Result.success(api.getDailyPrayers())
        } catch (t: Throwable) {
            Result.failure(t)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<PrayerApiService>()
    private lateinit var repo: PrayerRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = PrayerRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchDailyPrayers returns list on success`() = runTest {
        // Arrange
        val expected = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        coEvery { api.getDailyPrayers() } returns expected

        // Act
        val result = repo.fetchDailyPrayers()

        // Assert
        assert(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `fetchDailyPrayers returns failure when api throws`() = runTest {
        // Arrange
        val error = RuntimeException("network")
        coEvery { api.getDailyPrayers() } throws error

        // Act
        val result = repo.fetchDailyPrayers()

        // Assert
        assert(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}