package com.sudokupgame.app.navigation

import com.sudokupgame.app.data.GameMode
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object PuzzlesRoute

@Serializable
data class GameRoute(
    val puzzleId: String,
    val mode: GameMode = GameMode.NUMBER,
    /** 데일리 챌린지로 시작하면 그 날짜 (ISO, 예: 2026-11-06). */
    val dailyDate: String? = null,
)

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute
