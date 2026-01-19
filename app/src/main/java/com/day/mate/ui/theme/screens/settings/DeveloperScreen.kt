@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.painter.Painter
import com.day.mate.R

data class Developer(
    val name: String,
    val isTeamLeader: Boolean,
    val github: String,
    val linkedin: String
)

val developersList = listOf(
    Developer("Anas Ayman El-Gebaili", true, "https://github.com/AnasAyman13", "https://www.linkedin.com/in/anasayman13"),
    Developer("Mohamed Yousry Azzam", false, "https://github.com/MohamedYousry22", "https://www.linkedin.com/in/mohamed-yousry-a4a63627a"),
    Developer("Menna Ayman", false, "https://github.com/MennaAyman1697", "https://www.linkedin.com/in/mennaayman1697"),
    Developer("Eslam Fawzy", false, "https://github.com/eslamfawzy72", "https://www.linkedin.com/in/eslamfawzyy"),
    Developer("Youssef Barakat", false, "https://github.com/YoussefBarakat05", "https://www.linkedin.com/in/youssef-barakat05/"),
    Developer("Basem Mohamed", false, "https://github.com/Basem-Mohamed-1", "https://www.linkedin.com/in/basem-mohamed-341a65328/")
)

@Composable
fun DeveloperScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.forgrnd),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Software Engineers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 10.dp), // بادينج الجوانب

            // 🔥 الحل: إضافة مساحة 80dp في أسفل القائمة لرفع العناصر فوق الـ Bar
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(developersList) { dev ->
                DeveloperCard(
                    dev = dev,
                    onGitHub = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dev.github))
                        context.startActivity(intent)
                    },
                    onLinkedIn = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dev.linkedin))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DeveloperCard(
    dev: Developer,
    onGitHub: () -> Unit,
    onLinkedIn: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(17.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                2.dp,
                if (dev.isTeamLeader) Color(0xFFD4AF37) else Color.Transparent,
                RoundedCornerShape(17.dp)
            )
    ) {
        Box(
            Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF23304C),
                            Color(0xFF15243B)
                        )
                    )
                )
                .padding(start = 18.dp, top = 13.dp, end = 18.dp, bottom = 14.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        dev.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (dev.isTeamLeader) {
                        Spacer(Modifier.width(10.dp))
                        Box(
                            Modifier
                                .background(
                                    Color(0xFFD4AF37).copy(alpha = 0.23f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Team Leader",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD4AF37)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SocialButton(
                        icon = painterResource(id = R.drawable.ic_github),
                        label = "GitHub",
                        color = Color.White,
                        onClick = onGitHub
                    )
                    SocialButton(
                        icon = painterResource(id = R.drawable.ic_linkedin_full),
                        label = "LinkedIn",
                        color = Color(0xFF0077B5),
                        onClick = onLinkedIn
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialButton(
    icon: Painter,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.13f),
        shadowElevation = 1.dp,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(11.dp))
            Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}