package com.day.mate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.day.mate.ui.onboarding.OnboardingPagerActivity
import com.day.mate.ui.theme.DayMateTheme

class SplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("DayMatePrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        setContent {
            DayMateTheme {
                SplashScreenComposable {
                    if (isFirstTime) {
                        // ✅ الانتقال إلى شاشة الـ Onboarding الجديدة
                        startActivity(Intent(this, OnboardingPagerActivity::class.java))

                        // ⭐ ترانزيشن الخروج من الـ Splash
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )

                    } else {
                        // ✅ الانتقال مباشرة إلى شاشة المصادقة
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