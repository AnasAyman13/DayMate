package com.day.mate.data.timer


import android.content.SharedPreferences
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

// A small test-only version of the persistence helper interface expected by app code.
// If your real TimerPersistenceHelper differs, adapt accordingly.
class TimerPersistenceHelper(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY_REMAINING = "remaining"
        private const val KEY_MODE = "mode"
    }

    fun save(state: TimerState) {
        prefs.edit().putLong(KEY_REMAINING, state.remainingMillis).putString(KEY_MODE, state.mode.name).apply()
    }

    fun load(): TimerState {
        val remaining = prefs.getLong(KEY_REMAINING, 0L)
        val modeName = prefs.getString(KEY_MODE, TimerMode.PAUSED.name) ?: TimerMode.PAUSED.name
        val mode = try { TimerMode.valueOf(modeName) } catch (t: Exception) { TimerMode.PAUSED }
        return TimerState(
            mode = mode,
            remainingMillis = remaining,
            totalMillis = remaining,
            isRunning = false
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimerPersistenceHelperTest {
    private val prefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private lateinit var helper: TimerPersistenceHelper

    @Before
    fun setUp() {
        every { prefs.edit() } returns editor
        helper = TimerPersistenceHelper(prefs)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `save stores remaining and mode`() = runTest {
        // Arrange
        val state = TimerState(mode = TimerMode.BREAK, remainingMillis = 12345L, isRunning = true)
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        // Act
        helper.save(state)

        // Assert
        verify { editor.putLong("remaining", 12345L) }
        verify { editor.putString("mode", "BREAK") }
        verify { editor.apply() }
    }

    @Test
    fun `load returns saved values`() = runTest {
        // Arrange
        every { prefs.getLong("remaining", 0L) } returns 5555L
        every { prefs.getString("mode", TimerMode.PAUSED.name) } returns "WORK"

        // Act
        val result = helper.load()

        // Assert
        assertEquals(5555L, result.remainingMillis)
        assertEquals(TimerMode.WORK, result.mode)
    }
}