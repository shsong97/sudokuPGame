package com.sudokupgame.app.data

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val highlightSameDigit: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val showTimer: Boolean = true,
    val vibration: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
