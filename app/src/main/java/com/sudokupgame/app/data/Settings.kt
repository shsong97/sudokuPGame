package com.sudokupgame.app.data

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val highlightSameDigit: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val showTimer: Boolean = true,
    val vibration: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** 새 게임·퍼즐 목록에서 마지막으로 고른 모드. */
    val lastGameMode: GameMode = GameMode.NUMBER,
)
