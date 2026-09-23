package com.sudokupgame.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sudokupgame.app.feature.game.GameScreen
import com.sudokupgame.app.feature.home.HomeScreen
import com.sudokupgame.app.feature.puzzles.PuzzlesScreen
import com.sudokupgame.app.feature.settings.SettingsScreen
import com.sudokupgame.app.feature.stats.StatsScreen

@Composable
fun SudokuNavHost() {
    val navController = rememberNavController()
    val onBack: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onStartGame = { id, mode -> navController.navigate(GameRoute(id, mode)) },
                onPuzzles = { navController.navigate(PuzzlesRoute) },
                onStats = { navController.navigate(StatsRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<PuzzlesRoute> {
            PuzzlesScreen(
                onBack = onBack,
                onStartGame = { id, mode -> navController.navigate(GameRoute(id, mode)) },
            )
        }
        composable<GameRoute> {
            GameScreen(
                onBack = onBack,
                onNextPuzzle = { id, mode ->
                    navController.navigate(GameRoute(id, mode)) {
                        popUpTo<GameRoute> { inclusive = true }
                    }
                },
                onPuzzleList = {
                    navController.navigate(PuzzlesRoute) {
                        popUpTo<HomeRoute>()
                    }
                },
                onHome = { navController.popBackStack<HomeRoute>(inclusive = false) },
            )
        }
        composable<StatsRoute> {
            StatsScreen(onBack = onBack)
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = onBack)
        }
    }
}
