package com.day.mate.ui.theme.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    activeRoute: String?
) {
    val items = listOf(
        BottomNavItem.TimeLine,
        BottomNavItem.Todo,
        BottomNavItem.Pomodoro,
        BottomNavItem.Prayer,
        BottomNavItem.Media,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = activeRoute ?: navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val containerColor = if (isDark) {
        Color(0xFF1E2126).copy(alpha = 0.98f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }

    // --- تعديل المسافة السفلية هنا ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 40.dp) // رفعت الشريط من 22 إلى 40
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp),
            shape = RoundedCornerShape(38.dp),
            shadowElevation = if (isDark) 0.dp else 12.dp,
            tonalElevation = if (isDark) 0.dp else 8.dp,
            border = BorderStroke(1.dp, borderColor),
            color = containerColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    CustomBottomNavItem(
                        item = item,
                        selected = selected,
                        isDark = isDark,
                        onClick = {
                            if (!selected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    // أنيميشن تكبير الأيقونة (700ms)
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 0.9f,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    val selectedContentColor = if (isDark) Color(0xFF26C6DA) else Color(0xFF00ACC1)
    val unselectedContentColor = if (isDark) Color(0xFF90A4AE) else MaterialTheme.colorScheme.onSurfaceVariant

    val indicatorColor = if (selected) {
        if (isDark) Color(0xFF37474F).copy(alpha = 0.8f) else Color(0xFFE0F7FA)
    } else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 34.dp)
                .background(
                    color = indicatorColor,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (selected) item.selectedIconRes else item.iconRes),
                contentDescription = stringResource(item.titleRes),
                tint = if (selected) selectedContentColor else unselectedContentColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
        }

        // أنيميشن ظهور النص (700ms)
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = tween(700)) + expandVertically(animationSpec = tween(700)),
            exit = fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400))
        ) {
            Text(
                text = stringResource(item.titleRes),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = selectedContentColor,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}