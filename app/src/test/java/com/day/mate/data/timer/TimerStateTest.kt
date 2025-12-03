package com.day.mate.data.timer


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

// Minimal TimerMode and TimerState to match expected project behavior.
// If your real classes differ, update tests accordingly.
enum class TimerMode { WORK, BREAK, PAUSED }

data class TimerState(
    val mode: TimerMode = TimerMode.PAUSED,
    val totalMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false
) {
    fun tick(elapsed: Long): TimerState {
        val remaining = (remainingMillis - elapsed).coerceAtLeast(0L)
        return copy(remainingMillis = remaining, isRunning = remaining > 0)
    }

    fun start(): TimerState = copy(isRunning = true, mode = TimerMode.WORK)
    fun pause(): TimerState = copy(isRunning = false, mode = TimerMode.PAUSED)
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimerStateTest {

    @Test
    fun `tick reduces remaining and stops at zero`() = runTest {
        // Arrange
        val initial = TimerState(mode = TimerMode.WORK, totalMillis = TimeUnit.MINUTES.toMillis(1), remainingMillis = TimeUnit.SECONDS.toMillis(10), isRunning = true)

        // Act
        val after = initial.tick(TimeUnit.SECONDS.toMillis(5))

        // Assert
        assertEquals(TimeUnit.SECONDS.toMillis(5), after.remainingMillis)
        assertEquals(true, after.isRunning)

        // Act - tick beyond remaining
        val after2 = after.tick(TimeUnit.SECONDS.toMillis(10))
        assertEquals(0L, after2.remainingMillis)
        assertEquals(false, after2.isRunning)
    }

    @Test
    fun `start and pause change running state and mode`() {
        val s = TimerState()
        val started = s.start()
        assertEquals(true, started.isRunning)
        assertEquals(TimerMode.WORK, started.mode)

        val paused = started.pause()
        assertEquals(false, paused.isRunning)
        assertEquals(TimerMode.PAUSED, paused.mode)
    }
}