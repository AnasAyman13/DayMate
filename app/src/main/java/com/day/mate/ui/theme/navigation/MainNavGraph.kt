package com.day.mate.ui.theme.screens.media

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.day.mate.data.local.RetrofitInstance
import com.day.mate.data.repository.PrayerRepository
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.screens.PrayerScreen
import com.day.mate.ui.screens.settings.SettingsScreenContainer
import com.day.mate.ui.theme.AppGold
import com.day.mate.ui.theme.navigation.BottomNavigationBar
import com.day.mate.ui.theme.navigation.BottomNavItem
import com.day.mate.ui.theme.screens.settings.DeveloperScreen
import com.day.mate.ui.theme.screens.timeline.TimelineViewModel
import com.day.mate.ui.theme.screens.pomodoro.PomodoroScreen
import com.day.mate.ui.theme.screens.settings.HelpSupportScreen
import com.day.mate.ui.theme.screens.settings.TermsScreen
import com.day.mate.ui.theme.screens.timeline.TimelineScreen
import com.day.mate.ui.theme.screens.timeline.TimelineViewModelFactory
import com.day.mate.ui.theme.screens.todo.CreateTaskScreen
import com.day.mate.ui.theme.screens.todo.TasksScreen
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory

/**
 * MainNavGraph
 *
 * Main navigation graph for the entire application.
 * Handles all screen navigation and manages ViewModels.
 * Uses MaterialTheme colors for proper dark/light mode support.
 */
@Composable
fun MainNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    // Initialize database and repositories
    val database = remember { AppDatabase.getInstance(context) }
    val todoRepository = remember {
        TodoRepository(database.todoDao(), database.categoryDao())
    }
    val prayerRepository = remember {
        PrayerRepository(RetrofitInstance.api)
    }

    // Create ViewModels with factories
    val todoFactory = remember(todoRepository) { TodoViewModelFactory(todoRepository) }
    val timelineFactory = remember(todoRepository, prayerRepository) {
        TimelineViewModelFactory(todoRepository, prayerRepository)
    }
    val todoViewModel: TodoViewModel = viewModel(factory = todoFactory)

    // Sync data from Firestore on app start
    LaunchedEffect(Unit) {
        todoViewModel.syncFromFirestore()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show FAB only on Timeline and Todo screens
    val showFab = currentRoute == BottomNavItem.TimeLine.route ||
            currentRoute == BottomNavItem.Todo.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        todoViewModel.clearForm()
                        navController.navigate("task_screen/new")
                    },
                    containerColor = AppGold,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TimeLine.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ========== Main Screens ==========

            // Timeline Screen
            composable(BottomNavItem.TimeLine.route) {
                val timelineViewModel: TimelineViewModel = viewModel(
                    modelClass = TimelineViewModel::class.java,
                    factory = timelineFactory
                )
                TimelineScreen(viewModel = timelineViewModel)
            }

            // Todo Screen
            composable(BottomNavItem.Todo.route) {
                TasksScreen(
                    viewModel = todoViewModel,
                    onEditTask = { taskId ->
                        navController.navigate("task_screen/$taskId")
                    }
                )
            }

            // Pomodoro Screen
            composable(BottomNavItem.Pomodoro.route) {
                PomodoroScreen(isDarkTheme = true)
            }

            // Media Vault Screen (with Biometric Lock)
            composable(BottomNavItem.Media.route) {
                BiometricLockScreen(navController = navController)
            }

            // Prayer Times Screen
            composable(BottomNavItem.Prayer.route) {
                PrayerScreen()
            }

            // Settings Screen
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContainer(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ========== Vault Screens ==========

            // Media Vault Main Screen
            composable("media_vault") {
                VaultScreen(navController = navController)
            }

            // Vault Viewer Screen
            composable(
                route = "viewer/{uri}/{type}",
                arguments = listOf(
                    navArgument("uri") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val uri = backStackEntry.arguments?.getString("uri") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: "PHOTO"
                VaultViewerScreen(
                    navController = navController,
                    uri = uri,
                    type = type
                )
            }

            // ========== Task Management ==========

            // Create/Edit Task Screen
            composable(
                route = "task_screen/{taskId}",
                arguments = listOf(
                    navArgument("taskId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val taskIdString = backStackEntry.arguments?.getString("taskId")
                CreateTaskScreen(
                    navController = navController,
                    viewModel = todoViewModel,
                    taskIdString = taskIdString
                )
            }

            // ========== Settings Sub-Screens ==========

            // Developers Screen
            composable("developers") {
                DeveloperScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Terms of Service Screen
            composable("terms") {
                TermsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Help & Support Screen
            composable("help_support") {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}