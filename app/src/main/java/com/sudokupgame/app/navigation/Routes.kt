package com.sudokupgame.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object PuzzlesRoute

@Serializable
data class GameRoute(val puzzleId: String)

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute
