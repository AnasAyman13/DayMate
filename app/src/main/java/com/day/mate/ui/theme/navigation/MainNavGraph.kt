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
import com.day.mate.data.local.prayer.AppDatabase
import com.day.mate.data.local.prayer.RetrofitInstance
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

// إضافة الـ Imports المطلوبة للحالة
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

/**
 * MainNavGraph
 *
 * Main navigation graph for the entire application.
 * Handles all screen navigation and manages ViewModels.
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


    var isMediaUnlocked by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        todoViewModel.syncFromFirestore()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val activeBottomNavRoute = remember(currentRoute) {
        when {

            currentRoute?.startsWith("task_screen") == true -> BottomNavItem.Todo.route

            currentRoute == "media_vault" || currentRoute?.startsWith("viewer") == true -> BottomNavItem.Media.route

            currentRoute == "developers" || currentRoute == "terms" || currentRoute == "help_support" -> BottomNavItem.Settings.route

            else -> currentRoute
        }
    }
    val isTaskScreen = currentRoute?.startsWith("task_screen") == true
    val showFab = (activeBottomNavRoute == BottomNavItem.TimeLine.route ||
            activeBottomNavRoute == BottomNavItem.Todo.route) && !isTaskScreen
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

            BottomNavigationBar(navController = navController, activeRoute = activeBottomNavRoute)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TimeLine.route,
            modifier = Modifier.padding(innerPadding)
        ) {

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

            composable(BottomNavItem.Pomodoro.route) {
                PomodoroScreen(isDarkTheme = true)
            }

            composable(BottomNavItem.Media.route) {
                MediaScreenWrapper(
                    navController = navController,
                    isMediaUnlocked = isMediaUnlocked,

                    onUnlockSuccess = { isMediaUnlocked = true }
                )
            }

            composable(BottomNavItem.Prayer.route) {
                PrayerScreen()
            }

            composable(BottomNavItem.Settings.route) {
                SettingsScreenContainer(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("media_vault") {
                VaultScreen(navController = navController)
            }

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


            composable("developers") {
                DeveloperScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("terms") {
                TermsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("help_support") {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
@Composable
fun MediaScreenWrapper(
    navController: NavController,
    isMediaUnlocked: Boolean,
    onUnlockSuccess: () -> Unit
) {
    if (isMediaUnlocked) {
        VaultScreen(navController = navController)
    } else {

        BiometricLockScreen(
            navController = navController,
            onUnlockSuccess = onUnlockSuccess
        )
    }
}