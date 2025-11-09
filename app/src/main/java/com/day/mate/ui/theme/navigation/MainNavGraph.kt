package com.day.mate.ui.theme.screens.media

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.day.mate.data.local.AppDatabase
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.screens.PrayerScreen
import com.day.mate.ui.screens.settings.SettingsScreenContainer
import com.day.mate.ui.theme.AppGold
import com.day.mate.ui.theme.DarkBg
import com.day.mate.ui.theme.navigation.BottomNavigationBar
import com.day.mate.ui.theme.navigation.BottomNavItem
import com.day.mate.ui.theme.screens.TimeLineScreen
import com.day.mate.ui.theme.screens.pomodoro.PomodoroScreen
import com.day.mate.ui.theme.screens.todo.CreateTaskScreen
import com.day.mate.ui.theme.screens.todo.TasksScreen
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    // --- إعداد الـ Repository و ViewModel ---
    val repository = remember {
        val database = AppDatabase.getInstance(context)
        val todoDao = database.todoDao()
        val categoryDao = database.categoryDao()
        TodoRepository(todoDao, categoryDao)
    }
    val factory = remember(repository) { TodoViewModelFactory(repository) }
    val viewModel: TodoViewModel = viewModel(factory = factory)

    // --- إدارة حالة الـ BackStack ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showFab = currentRoute == BottomNavItem.TimeLine.route ||
            currentRoute == BottomNavItem.Todo.route

    Scaffold(
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        viewModel.clearForm()
                        navController.navigate("task_screen/new")
                    },
                    containerColor = AppGold,
                    contentColor = DarkBg
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TimeLine.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- الشاشات الرئيسية ---
            composable(BottomNavItem.TimeLine.route) { TimeLineScreen() }
            composable(BottomNavItem.Todo.route) {
                TasksScreen(
                    viewModel = viewModel,
                    onEditTask = { taskId ->
                        navController.navigate("task_screen/$taskId")
                    }
                )
            }
            composable(BottomNavItem.Pomodoro.route) { PomodoroScreen() }
            composable(BottomNavItem.Media.route) { VaultScreen(navController = navController) }
            composable(BottomNavItem.Prayer.route) { PrayerScreen() }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContainer(onBackClick = { navController.popBackStack() })
            }

            // --- شاشة عرض الملفات ---
            composable(
                route = "viewer/{uri}/{type}",
                arguments = listOf(
                    navArgument("uri") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val uri = backStackEntry.arguments?.getString("uri") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: "PHOTO"
                VaultViewerScreen(navController = navController, uri = uri, type = type)
            }

            // --- إنشاء/تعديل مهمة ---
            composable(
                route = "task_screen/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskIdString = backStackEntry.arguments?.getString("taskId")
                CreateTaskScreen(
                    navController = navController,
                    viewModel = viewModel,
                    taskIdString = taskIdString
                )
            }
        }
    }
}
