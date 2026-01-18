package com.day.mate.ui.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.day.mate.viewmodel.AuthViewModel
import java.util.Locale

@Composable
fun AuthNavGraph(
    viewModel: AuthViewModel,
    onAuthDone: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ✅ Shared language state for Login + SignUp
    val systemLangIsArabic = LocalConfiguration.current.locales[0].language == "ar"
    var langTag by rememberSaveable { mutableStateOf(if (systemLangIsArabic) "ar" else "en") }
    val isArabic = langTag == "ar"

    val localizedContext = remember(langTag, context) { context.createAuthLocalizedContext(langTag) }
    val t: (Int) -> String = remember(localizedContext) { { id -> localizedContext.getString(id) } }

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val toggleLang = { langTag = if (isArabic) "en" else "ar" }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoggedIn = onAuthDone,
                    onNavigateToSignUp = { navController.navigate("signup") },
                    onForgotPassword = {},
                    onGoogleSignInClicked = {},

                    // ✅ shared
                    t = t,
                    isArabic = isArabic,
                    onToggleLang = toggleLang
                )
            }

            composable("signup") {
                SignUpScreen(
                    viewModel = viewModel,
                    onSignedUp = onAuthDone,
                    onNavigateToSignIn = { navController.popBackStack() },

                    // ✅ shared
                    t = t,
                    isArabic = isArabic,
                    onToggleLang = toggleLang
                )
            }
        }
    }
}

private fun Context.createAuthLocalizedContext(langTag: String): Context {
    val locale = Locale.forLanguageTag(langTag)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
