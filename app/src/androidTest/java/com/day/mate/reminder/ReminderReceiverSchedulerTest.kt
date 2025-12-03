package com.day.mate.data.local.reminder

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.day.mate.data.model.Todo
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for ReminderReceiver and ReminderScheduler.
 *
 * Notes:
 * - Avoids JVM-only java.time usage in the test itself.
 * - Calls scheduleDailyReminder and cancel methods which use java.util.Calendar / AlarmManager inside app code.
 * - Does not attempt to assert AlarmManager internal state (would require heavy mocking).
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class ReminderReceiverSchedulerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun reminderReceiver_handlesBroadcastWithoutThrowing() {
        val receiver = ReminderReceiver()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderConstants.EXTRA_TODO_ID, 42)
            putExtra(ReminderConstants.EXTRA_TODO_TITLE, "Test Title")
            putExtra(ReminderConstants.EXTRA_TODO_DESCRIPTION, "Test Description")
            // Not using java.time here; receiver only reads extras and delegates to NotificationHelper
        }

        // Should not throw
        receiver.onReceive(context, intent)
    }

    @Test
    fun scheduler_dailyAndCancelPaths_doNotThrow() = runBlocking {
        val scheduler = ReminderScheduler(context)

        // scheduleDailyReminder uses Calendar internally and should not crash
        scheduler.scheduleDailyReminder(hour = 0, minute = 1)

        // schedulePomodoroBreak requires LocalDateTime - avoid invoking it here to prevent java.time issues in some androidTest environments.

        // cancelPomodoroBreak and cancel should execute and not throw
        scheduler.cancelPomodoroBreak()
        scheduler.cancel(123)
    }

    @Test
    fun schedule_withTodo_noRemindMeDoesNothing() = runBlocking {
        // Build a Todo that has remindMe = false so schedule(todo) will return early (no java.time work executed)
        val todo = Todo(
            id = 1,
            remoteId = "",
            title = "T",
            description = "D",
            category = "",
            date = "1970-01-01",
            time = "00:00",
            remindMe = false,
            isDone = false
        )

        val scheduler = ReminderScheduler(context)
        // Should be a quick no-op (returns early because remindMe == false)
        scheduler.schedule(todo)
    }
}