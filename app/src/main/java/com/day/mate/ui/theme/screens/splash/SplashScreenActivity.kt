package com.day.mate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.day.mate.ui.theme.DayMateTheme
import com.day.mate.ui.onboarding.OnboardingPagerActivity
import kotlin.random.Random

/**
 * SplashScreen
 *
 * The initial activity launched when the application starts. It displays the splash
 * screen animation and determines the next destination (Onboarding or Auth) based on
 * the user's first-time launch status.
 */
class SplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the application is being launched for the first time
        val sharedPref = getSharedPreferences("DayMatePrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        setContent {
            DayMateTheme {
                SplashScreenComposable {
                    if (isFirstTime) {
                        // Navigate to the Onboarding screens for new users
                        startActivity(Intent(this, OnboardingPagerActivity::class.java))

                        // Apply fade out transition
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                    } else {
                        // Navigate directly to the Authentication screen for existing users
                        startActivity(Intent(this, AuthActivity::class.java))

                        // Apply slide transition
                        overridePendingTransition(
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                    }

                    // Finish the splash activity so the user cannot navigate back to it
                    finish()
                }
            }
        }
    }
}
