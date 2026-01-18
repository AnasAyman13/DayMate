package com.day.mate.ui.theme.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Floating Bottom Navigation Bar
 * - Wider layout (less outer horizontal padding)
 * - Safe inner padding so indicator won't be clipped
 * - No navigationBarsPadding (so no black/white strip below)
 * - Slightly lifted look (bigger bottom outer padding + shadow + border)
 */
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ بدل padding(horizontal=.., vertical=..) عشان نسخة Compose عندك
            .padding(
                start = 10.dp,
                top = 10.dp,
                end = 10.dp,
                bottom = 18.dp // ✅ أكبر شوية عشان يبان مرفوع/عايم
            )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ) {
            NavigationBar(
                modifier = Modifier
                    .height(72.dp)
                    // ✅ inset داخلي يمنع قصّ الـ indicator عند الأطراف
                    .padding(start = 14.dp, top = 0.dp, end = 14.dp, bottom = 0.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                windowInsets = WindowInsets(0)
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selected) item.selectedIconRes else item.iconRes
                                ),
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = {
                            if (selected) {
                                Text(
                                    text = stringResource(item.titleRes),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}
