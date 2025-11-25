package com.day.mate.util

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(base))
    }
}
