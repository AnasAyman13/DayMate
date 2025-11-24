package com.day.mate

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.day.mate.ui.onboardingActivity1.DayMateDarkTheme
import com.day.mate.ui.onboardingActivity1.OnboardingActivity1
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("DayMatePrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        setContent {
            DayMateDarkTheme {
                SplashScreenComposable {
                    if (isFirstTime) {

                        sharedPref.edit().putBoolean("isFirstTime", false).apply()

                        startActivity(Intent(this, OnboardingActivity1::class.java))

                        // ⭐ ترانزيشن الخروج من الـ Splash
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                    } else {

                        startActivity(Intent(this, AuthActivity::class.java))

                        // ⭐ ترانزيشن انزلاق
                        overridePendingTransition(
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                        )
                    }

                    finish()
                }
            }
        }
    }
}
