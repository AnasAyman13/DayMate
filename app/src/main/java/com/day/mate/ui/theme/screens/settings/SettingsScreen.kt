package com.day.mate.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.day.mate.AuthActivity
import com.day.mate.R
import com.day.mate.data.model.User
import com.day.mate.util.LocaleUtils

/**
 * Opens the system notification settings screen for this app, allowing the user to manage
 * notification permissions and channels directly within Android Settings.
 *
 * @param activity The current Activity context, required to start the new Intent.
 */
fun openAppNotificationSettings(activity: Activity) {
    val intent = Intent().apply {
        when {
            // Android 8.0+ (Oreo and above): direct app notification settings
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.packageName)
            }
            // Android 5.0 - 7.1: legacy action for app notification settings
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                @Suppress("DEPRECATION")
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                @Suppress("DEPRECATION")
                putExtra("app_package", activity.packageName)
                @Suppress("DEPRECATION")
                putExtra("app_uid", activity.applicationInfo.uid)
            }
            // Older versions: open app details page as a fallback
            else -> {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:${activity.packageName}")
            }
        }
    }
    activity.startActivity(intent)
}

/**
 * SettingsScreenContainer
 *
 * Main container for the settings screen that manages the ViewModel and state logic.
 * Initializes the [SettingsViewModel] using the [SettingsViewModelFactory].
 *
 * @param navController Navigation controller for navigating between screens.
 * @param onBackClick Callback executed when the back button is pressed.
 */
@Composable
fun SettingsScreenContainer(
    navController: NavHostController,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Create ViewModel with Factory that provides Context dependency
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context.applicationContext)
    )

    // Load user data when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    val state by viewModel.uiState.collectAsState()

    // Observe logout state and navigate to AuthActivity when user logs out
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_logged_out_successfully),
                Toast.LENGTH_SHORT
            ).show()
            // Clear back stack and start AuthActivity
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    SettingsScreen(
        state = state,
        onBackClick = onBackClick,
        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
        onToggleCloudSync = { viewModel.toggleCloudSync(it) },
        // When user taps notifications row, open system notification settings for this app
        onToggleNotifications = {
            activity?.let { openAppNotificationSettings(it) }
        },
        // Toggle between Arabic and English and restart the app with new locale
        onToggleLanguage = { lang ->
            activity?.let { LocaleUtils.setLocaleAndRestart(it, lang) }
        },
        onLogout = { viewModel.onLogoutClicked() },
        onChangePassword = { viewModel.onChangePasswordClicked(context) },
        onNavigate = { route -> navController.navigate(route) }
    )
}

/**
 * SettingsScreen
 *
 * Stateless UI composable for the settings screen.
 * Displays user profile, appearance settings, account settings, and support options.
 *
 * @param state Current UI state containing user data and settings.
 * @param onBackClick Callback when the back button is pressed.
 * @param onToggleDarkMode Callback when dark mode is toggled.
 * @param onToggleCloudSync Callback when cloud sync is toggled.
 * @param onToggleNotifications Callback when the notifications row is clicked (to open system settings).
 * @param onToggleLanguage Callback when the language is changed.
 * @param onLogout Callback when logout is clicked.
 * @param onNavigate Callback for navigation to other screens.
 * @param onChangePassword Callback when change password is clicked.
 */
@Composable
fun SettingsScreen(
    state: SettingsState,
    onBackClick: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleCloudSync: (Boolean) -> Unit,
    onToggleNotifications: () -> Unit,
    onToggleLanguage: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onChangePassword: () -> Unit
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current

    // Get currently saved language or fall back to device default
    val savedLang = LocaleUtils.getSavedLanguage(context)
        ?: java.util.Locale.getDefault().language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Top app bar row (back button + title)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.desc_back_button)
                )
            }
            Text(
                stringResource(R.string.settings_title_and_profile),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(16.dp))

        // User profile (name + email)
        ProfileHeader(user = state.user)

        Spacer(Modifier.height(20.dp))

        // Appearance section (dark mode + language)
        SettingsCard(title = stringResource(R.string.settings_appearance)) {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                checked = state.darkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Language change item: toggles between Arabic and English
            SettingsClickableRow(
                icon = Icons.Outlined.Language,
                title = if (savedLang == "en")
                    stringResource(R.string.settings_change_to_arabic)
                else
                    stringResource(R.string.settings_change_to_english),
                onClick = {
                    val nextLang = if (savedLang == "en") "ar" else "en"
                    onToggleLanguage(nextLang)
                }
            )
        }

        // Account section (notifications + change password)
        SettingsCard(title = stringResource(R.string.settings_account)) {
            // Open system notification settings for this app
            SettingsClickableRow(
                Icons.Outlined.Notifications,
                stringResource(R.string.settings_notifications)
            ) {
                onToggleNotifications()
            }
            SettingsClickableRow(
                Icons.Outlined.Password,
                stringResource(R.string.settings_change_password)
            ) { onChangePassword() }
        }

        // Support & Legal section (help, terms, developers)
        SettingsCard(title = stringResource(R.string.settings_support_legal)) {
            SettingsClickableRow(
                Icons.Outlined.Help,
                stringResource(R.string.settings_help_support)
            ) { onNavigate("help_support") }
            SettingsClickableRow(
                Icons.Outlined.Gavel,
                stringResource(R.string.settings_terms_service)
            ) { onNavigate("terms") }
            SettingsClickableRow(
                Icons.Outlined.Code,
                stringResource(R.string.settings_developers)
            ) { onNavigate("developers") }
        }

        Spacer(Modifier.height(12.dp))

        // Logout button
        Button(
            onClick = { onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x1AFF0000), shape = RoundedCornerShape(12.dp)), // Semi-transparent red background
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.settings_log_out),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

/**
 * ProfileHeader
 *
 * Displays user profile information (name and email).
 *
 * @param user User data to display.
 */
@Composable
private fun ProfileHeader(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            user.email,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * SettingsCard
 *
 * Reusable card container for a settings group/section.
 *
 * @param title Section title.
 * @param content Card content composable.
 */
@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            content()
        }
    }
}

/**
 * SettingsToggleRow
 *
 * Reusable row with icon, label, and Switch for boolean settings.
 *
 * @param icon Icon to display.
 * @param title Setting title/label.
 * @param checked Current toggle state.
 * @param onCheckedChange Callback when the toggle state is changed.
 */
@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * SettingsClickableRow
 *
 * Reusable clickable row with icon, label, and chevron arrow.
 *
 * @param icon Icon to display.
 * @param title Setting title/label.
 * @param onClick Callback when the row is clicked.
 */
@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Preview for SettingsScreen
 */
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    val mockUser = User(
        id = "1",
        name = "John Doe",
        email = "john.doe@example.com"
    )
    // NOTE: SettingsState is assumed to be defined elsewhere in the project
    // Using a placeholder data class for Preview purposes only.
    data class MockSettingsState(
        val user: User,
        val darkModeEnabled: Boolean,
        val cloudSyncEnabled: Boolean,
        val notificationsEnabled: Boolean,
        val isLoggedOut: Boolean = false,
        val isLoading: Boolean = false
    )

    // Using MockSettingsState structure to call the actual Composable.
    // Assuming SettingsState is structurally similar to MockSettingsState.
    val state = MockSettingsState(
        user = mockUser,
        darkModeEnabled = false,
        cloudSyncEnabled = true,
        notificationsEnabled = true
    )
    MaterialTheme {
        SettingsScreen(
            state = state as SettingsState, // Casting for compilation in context
            onBackClick = {},
            onToggleDarkMode = {},
            onToggleCloudSync = {},
            onToggleNotifications = {},
            onToggleLanguage = {},
            onLogout = {},
            onNavigate = {},
            onChangePassword = {}
        )
    }
}