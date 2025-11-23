package com.day.mate.ui.theme.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
@Composable
fun TermsScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Terms of Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(18.dp)
        ) {
            Text(
                text = """
                    Terms of Service

                    1. By using DayMate, you agree to use the app for personal, non-commercial purposes only.
                    2. The app does not collect or share any personal data with third parties.
                    3. DayMate provides productivity tools such as timers, tasks, and reminders; we are not responsible for any missed notifications or data loss.
                    4. User data (like tasks or notes) is stored locally unless otherwise stated. Backing up is the user's responsibility.
                    5. These terms may change at any time, and continued app use means your acceptance of the new terms.

                    For inquiries: DayMate.Team@gmail.com
                """.trimIndent(),
                fontSize = 15.sp
            )
        }
    }
}
