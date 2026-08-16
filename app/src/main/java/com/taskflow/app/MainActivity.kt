package com.taskflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taskflow.app.presentation.addtask.AddTaskScreen
import com.taskflow.app.presentation.dashboard.DashboardSettingsScreen
import com.taskflow.app.presentation.designsystem.TaskFlowTheme
import com.taskflow.app.presentation.taskdetail.TaskDetailScreen
import com.taskflow.app.presentation.tasklist.TaskListScreen
import com.taskflow.app.presentation.theme.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = koinViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()

            RequestNotificationPermissionIfNeeded()

            TaskFlowTheme(darkTheme = isDarkTheme) {
                TaskFlowNavHost(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = themeViewModel::onToggleTheme
                )
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Resultado tratado apenas visualmente via estado do sistema. */ }
    )

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private object Routes {
    const val TASK_LIST = "task_list"
    const val ADD_TASK = "add_task"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val DASHBOARD_SETTINGS = "dashboard_settings"

    fun taskDetail(taskId: Long) = "task_detail/$taskId"
    fun editTask(taskId: Long) = "edit_task/$taskId"
}

private const val SCREEN_TRANSITION_DURATION_MS = 320


@Composable
private fun TaskFlowNavHost(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val enterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 4 },
        animationSpec = tween(SCREEN_TRANSITION_DURATION_MS)
    ) + fadeIn(tween(SCREEN_TRANSITION_DURATION_MS))

    val exitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(SCREEN_TRANSITION_DURATION_MS)
    ) + fadeOut(tween(SCREEN_TRANSITION_DURATION_MS))

    val popEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(SCREEN_TRANSITION_DURATION_MS)
    ) + fadeIn(tween(SCREEN_TRANSITION_DURATION_MS))

    val popExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 4 },
        animationSpec = tween(SCREEN_TRANSITION_DURATION_MS)
    ) + fadeOut(tween(SCREEN_TRANSITION_DURATION_MS))

    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onAddTaskClick = { navController.navigate(Routes.ADD_TASK) },
                onTaskClick = { taskId -> navController.navigate(Routes.taskDetail(taskId)) },
                onOpenDashboardSettings = { navController.navigate(Routes.DASHBOARD_SETTINGS) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable(Routes.DASHBOARD_SETTINGS) {
            DashboardSettingsScreen()
        }
        composable(Routes.ADD_TASK) {
            AddTaskScreen(onTaskSaved = { navController.popBackStack() })
        }
        composable(Routes.TASK_DETAIL) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onEditClick = { editingTaskId -> navController.navigate(Routes.editTask(editingTaskId)) }
            )
        }
        composable(Routes.EDIT_TASK) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: return@composable
            AddTaskScreen(taskId = taskId, onTaskSaved = { navController.popBackStack() })
        }
    }
}
