package com.sudokupgame.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
                onNewGame = { navController.navigate(GameRoute(puzzleId = "E001")) },
                onPuzzles = { navController.navigate(PuzzlesRoute) },
                onStats = { navController.navigate(StatsRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<PuzzlesRoute> {
            PuzzlesScreen(onBack = onBack)
        }
        composable<GameRoute> { entry ->
            GameScreen(puzzleId = entry.toRoute<GameRoute>().puzzleId, onBack = onBack)
        }
        composable<StatsRoute> {
            StatsScreen(onBack = onBack)
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = onBack)
        }
    }
}
