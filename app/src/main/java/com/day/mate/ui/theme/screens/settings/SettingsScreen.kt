package com.day.mate.ui.screens.settings

import android.app.Activity
import android.content.Intent
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

@Composable
fun SettingsScreenContainer(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {

            Toast.makeText(context, "Logged out succsusfuly", Toast.LENGTH_SHORT).show()
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
        onToggleNotifications = { viewModel.toggleNotifications(it) },
        onToggleLanguage = { lang ->
            activity?.let { LocaleUtils.setLocaleAndRestart(it, lang) }
        },
        onLogout = { viewModel.onLogoutClicked() },
        onNavigate = { route -> navController.navigate(route) }
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onBackClick: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleCloudSync: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleLanguage: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val savedLang = LocaleUtils.getSavedLanguage(context) ?: java.util.Locale.getDefault().language
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.desc_back_button))
            }
            Text(
                stringResource(R.string.settings_title_and_profile),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(16.dp))

        ProfileHeader(user = state.user)

        Spacer(Modifier.height(20.dp))

        SettingsCard(title = stringResource(R.string.settings_appearance)) {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                checked = state.darkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

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

        SettingsCard(title = stringResource(R.string.settings_data_sync)) {
            SettingsToggleRow(
                icon = Icons.Outlined.CloudSync,
                title = stringResource(R.string.settings_cloud_sync),
                checked = state.cloudSyncEnabled,
                onCheckedChange = onToggleCloudSync
            )
        }

        SettingsCard(title = stringResource(R.string.settings_account)) {
            SettingsClickableRow(
                Icons.Outlined.Notifications,
                stringResource(R.string.settings_notifications)
            ) {
                onToggleNotifications(!state.notificationsEnabled)
            }
            SettingsClickableRow(
                Icons.Outlined.Password,
                stringResource(R.string.settings_change_password)
            ) { }
        }

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
                    .background(Color(0x1AFF0000), shape = RoundedCornerShape(12.dp)),
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

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
