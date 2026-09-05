package com.thackman.ai.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thackman.ai.viewmodel.HomeViewModel

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateChat = { navController.navigate("chat") },
                onNavigateCommandCenter = { navController.navigate("command_center") },
                onNavigateTasks = { navController.navigate("tasks") }
            )
        }
        composable("chat") {
            AIChatScreen(
                viewModel = homeViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("command_center") {
            CommandCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("tasks") {
            TasksScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
