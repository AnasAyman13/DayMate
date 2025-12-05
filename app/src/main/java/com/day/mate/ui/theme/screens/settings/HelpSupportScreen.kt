@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R

/**
 * HelpSupportScreen
 *
 * Provides users with information on how to seek support, primarily via email.
 *
 * @param onBack Callback executed when the navigation back button is pressed.
 */
@Composable
fun HelpSupportScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_help_support)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.desc_back_button))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Added vertical scroll
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = "If you have any questions, issues, or suggestions about DayMate, you can contact the development team.\n\nEmail: DayMate.Team@gmail.com\n\nWe aim to respond as quickly as possible. Thank you for supporting DayMate!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(24.dp)) // Increased spacing

            // Clickable Email Contact Card
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(11.dp))
                    .clickable {
                        // Intent to open email client pre-filled with the support email
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:DayMate.Team@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                    .padding(vertical = 13.dp, horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "DayMate.Team@gmail.com",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}