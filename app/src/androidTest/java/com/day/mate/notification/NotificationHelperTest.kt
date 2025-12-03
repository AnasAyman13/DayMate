package com.day.mate.data.local.reminder

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for NotificationHelper.
 *
 * These tests call into the real NotificationHelper implementation from the app.
 * They avoid JVM-only APIs and use ApplicationProvider to get a Context.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class NotificationHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun createChannelAndShowGeneralNotification_doesNotThrow() {
        NotificationHelper.createNotificationChannelIfNeeded(context)

        // Try showing a general notification; should not throw in instrumentation environment
        NotificationHelper.showGeneralNotification(
            context = context,
            title = "Integration Test",
            content = "Content",
            notificationId = 9999
        )

        val nm = NotificationManagerCompat.from(context)
        assertNotNull(nm)
    }

    @Test
    fun showTodoReminderNotification_doesNotThrow() {
        NotificationHelper.createNotificationChannelIfNeeded(context)

        // Show a todo reminder notification (uses PendingIntent internally)
        NotificationHelper.showTodoReminderNotification(
            context = context,
            todoId = 1,
            title = "Reminder",
            description = "It's time"
        )
    }
}