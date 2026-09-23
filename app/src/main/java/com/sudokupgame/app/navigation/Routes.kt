package com.sudokupgame.app.navigation

import com.sudokupgame.app.data.GameMode
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object PuzzlesRoute

@Serializable
data class GameRoute(val puzzleId: String, val mode: GameMode = GameMode.NUMBER)

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute
