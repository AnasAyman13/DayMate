package com.day.mate.ui.screens

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun Context.createLocalizedContext(langTag: String): Context {
    val locale = Locale.forLanguageTag(langTag)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
