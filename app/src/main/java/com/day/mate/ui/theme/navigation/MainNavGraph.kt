package com.day.mate.ui.theme.screens.media

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.day.mate.ui.theme.navigation.BottomNavItem
import com.day.mate.ui.theme.navigation.BottomNavigationBar
import com.day.mate.ui.theme.screens.pomodoro.PomodoroScreen
import com.day.mate.ui.theme.screens.settings.DeveloperScreen
import com.day.mate.ui.theme.screens.settings.HelpSupportScreen
import com.day.mate.ui.theme.screens.settings.TermsScreen
import com.day.mate.ui.theme.screens.timeline.TimelineScreen
import com.day.mate.ui.theme.screens.timeline.TimelineViewModel
import com.day.mate.ui.theme.screens.timeline.TimelineViewModelFactory
import com.day.mate.ui.theme.screens.todo.CreateTaskScreen
import com.day.mate.ui.theme.screens.todo.TasksScreen
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import com.day.mate.ui.theme.screens.todo.TodoViewModelFactory

@Composable
fun MainNavGraph(startRouteFromIntent: String? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    // Init DB & repos
    val database = remember { AppDatabase.getInstance(context) }
    val todoRepository = remember { TodoRepository(database.todoDao(), database.categoryDao()) }
    val prayerRepository = remember { PrayerRepository(RetrofitInstance.api) }

    // ViewModels
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

    val pomodoroRoute = BottomNavItem.Pomodoro.route
    val actualStartRoute = remember(startRouteFromIntent) {
        if (startRouteFromIntent == pomodoroRoute) pomodoroRoute
        else BottomNavItem.TimeLine.route
    }

    // ✅ اخفاء البوتوم ناف في شاشة الـ Preview (viewer)
    val showBottomBar = currentRoute?.startsWith("viewer") != true

    // Active Bottom Nav route (mapping)
    val activeBottomNavRoute = remember(currentRoute) {
        when {
            currentRoute?.startsWith("task_screen") == true -> BottomNavItem.Todo.route
            currentRoute == "media_vault" || currentRoute?.startsWith("viewer") == true -> BottomNavItem.Media.route
            currentRoute == "developers" || currentRoute == "terms" || currentRoute == "help_support" -> BottomNavItem.Settings.route
            else -> currentRoute
        }
    }

    val isTaskScreen = currentRoute?.startsWith("task_screen") == true

    // FAB: يظهر في todo فقط
    val showFab = (activeBottomNavRoute == BottomNavItem.Todo.route) && !isTaskScreen

    // Floating nav sizes
    val floatingNavSpace = 92.dp
    val navOffset = 14.dp
    val bottomClearance = floatingNavSpace + navOffset + 8.dp

    val prayerRoute = BottomNavItem.Prayer.route
    val settingsRoute = BottomNavItem.Settings.route
    val timelineRoute = BottomNavItem.TimeLine.route
    val todoRoute = BottomNavItem.Todo.route
    val createtaskscreenRoute = BottomNavItem.CreateTaskScreen.route
    val mediaRoute = BottomNavItem.Media.route

    // ✅ لو البوتوم ناف مخفية (في viewer) يبقى مفيش أي bottom padding إضافي
    val navHostBottomPadding =
        if (!showBottomBar) {
            0.dp
        } else {
            if (
                activeBottomNavRoute == prayerRoute ||
                activeBottomNavRoute == settingsRoute ||
                activeBottomNavRoute == todoRoute ||
                activeBottomNavRoute == timelineRoute ||
                createtaskscreenRoute == timelineRoute ||
                activeBottomNavRoute == mediaRoute
            ) 0.dp else bottomClearance
        }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                if (showFab) {
                    FloatingActionButton(
                        modifier = Modifier.padding(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = floatingNavSpace + navOffset + 4.dp
                        ),
                        onClick = {
                            todoViewModel.clearForm()
                            navController.navigate("task_screen/new")
                        },
                        containerColor = AppGold,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            modifier = Modifier.size(18.dp) // ⬅️ تصغير الأيقونة سنة
                        )                    }
                }
            }
        ) { innerPadding ->

            val animDuration = 600

            NavHost(
                navController = navController,
                startDestination = actualStartRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = navHostBottomPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(animDuration, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                initialScale = 0.90f,
                                animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                            )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(animDuration, easing = FastOutSlowInEasing)) +
                            scaleOut(
                                targetScale = 0.90f,
                                animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                            )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(animDuration, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                initialScale = 0.90f,
                                animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                            )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(animDuration, easing = FastOutSlowInEasing)) +
                            scaleOut(
                                targetScale = 0.90f,
                                animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                            )
                }
            ) {

                composable(BottomNavItem.TimeLine.route) {
                    val timelineViewModel: TimelineViewModel = viewModel(
                        modelClass = TimelineViewModel::class.java,
                        factory = timelineFactory
                    )
                    TimelineScreen(viewModel = timelineViewModel)
                }

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
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val taskIdString = backStackEntry.arguments?.getString("taskId")
                    CreateTaskScreen(
                        navController = navController,
                        viewModel = todoViewModel,
                        taskIdString = taskIdString
                    )
                }

                composable("developers") {
                    DeveloperScreen(onBack = { navController.popBackStack() })
                }

                composable("terms") {
                    TermsScreen(onBack = { navController.popBackStack() })
                }

                composable("help_support") {
                    HelpSupportScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        // ✅ عرض البوتوم ناف بشرط (تختفي في viewer)
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navOffset)
            ) {
                BottomNavigationBar(
                    navController = navController,
                    activeRoute = activeBottomNavRoute
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
