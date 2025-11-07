package com.day.mate.ui.screens.settings

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

import com.day.mate.data.model.User
import com.day.mate.R
import com.day.mate.ui.theme.screens.auth.AuthActivity

@Composable
fun SettingsScreenContainer(
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isLoggedOut) {
        if(state.isLoggedOut){
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    SettingsScreen(
        state = state,
        onBackClick = onBackClick,
        onEditProfile = { viewModel.onEditProfileClicked() },
        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
        onToggleCloudSync = { viewModel.toggleCloudSync(it) },
        onToggleNotifications = { viewModel.toggleNotifications(it) },
        onManageStorage = { viewModel.onManageStorageClicked() },
        onLogout = { viewModel.onLogoutClicked() }
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onBackClick: () -> Unit,
    onEditProfile: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleCloudSync: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onManageStorage: () -> Unit,
    onLogout: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null)
            }
            Text(
                stringResource(R.string.settings_title_and_profile),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Profile header
        ProfileHeader(
            user = state.user,
            onEdit = onEditProfile
        )

        Spacer(Modifier.height(20.dp))

        // Appearance Card
        SettingsCard(title = stringResource(R.string.settings_appearance)) {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                checked = state.darkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )
        }

        // Data & Sync
        SettingsCard(title = stringResource(R.string.settings_data_sync)) {
            SettingsToggleRow(
                icon = Icons.Outlined.CloudSync,
                title = stringResource(R.string.settings_cloud_sync),
                checked = state.cloudSyncEnabled,
                onCheckedChange = onToggleCloudSync
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsClickableRow(
                icon = Icons.Outlined.Folder,
                title = stringResource(R.string.settings_manage_storage),
                onClick = onManageStorage
            )
        }

        // Account
        SettingsCard(title = stringResource(R.string.settings_account)) {
            SettingsClickableRow(Icons.Outlined.Notifications, stringResource(R.string.settings_notifications)) {}
            SettingsClickableRow(Icons.Outlined.Password, stringResource(R.string.settings_change_password)) {}
        }

        // Support & Legal
        SettingsCard(title = stringResource(R.string.settings_support_legal)) {
            SettingsClickableRow(Icons.Outlined.Help, stringResource(R.string.settings_help_support)) {}
            SettingsClickableRow(Icons.Outlined.Gavel, stringResource(R.string.settings_terms_service)) {}
        }

        Spacer(Modifier.height(12.dp))

        // Logout
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
                Text(stringResource(R.string.settings_log_out), color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileHeader(user: User, onEdit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(user.email, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onEdit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3313DAEC)),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(40.dp)
        ) {
            Text(stringResource(R.string.settings_edit_profile), color = Color(0xFF13DAEC))
        }
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    val mockUser = User(
        id = "1",
        name = "Alex Doe",
        email = "alex.doe@daymate.com",
    )
    val state = SettingsState(user = mockUser,)
    MaterialTheme {
        SettingsScreen(
            state = state,
            onBackClick = {},
            onEditProfile = {},
            onToggleDarkMode = {},
            onToggleCloudSync = {},
            onToggleNotifications = {},
            onManageStorage = {},
            onLogout = {}
        )
    }
}
