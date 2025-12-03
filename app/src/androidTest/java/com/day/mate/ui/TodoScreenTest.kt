package com.day.mate.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder Compose instrumentation test for UI sanity.
 *
 * - Uses createAndroidComposeRule<ComponentActivity>()
 * - Uses ApplicationProvider implicitly via the rule's activity
 *
 * NOTE:
 * The real TodoScreen composable signature wasn't reliably available in the test environment.
 * This placeholder is Ignored by default; remove @Ignore and update to call the real TodoScreen() with its parameters
 * when you adapt this test to the app's composable API.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class TodoScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Ignore("Adapt to real TodoScreen composable and remove this Ignore")
    @Test
    fun placeholder_compose_environment_works() {
        composeTestRule.activity.setContent {
            Text("Placeholder UI")
        }

        composeTestRule.onNodeWithText("Placeholder UI").assertExists()
    }
}