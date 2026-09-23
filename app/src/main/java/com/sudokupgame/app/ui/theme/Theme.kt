package com.sudokupgame.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 격자 가독성을 위해 동적 색상(Material You)은 쓰지 않고 고정 팔레트를 사용한다.
private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = AmberDark,
    secondaryContainer = Amber,
    onSecondaryContainer = Color(0xFF261900),
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurfaceVariant,
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = Color(0xFF0E1D3A),
    secondary = Amber,
    secondaryContainer = AmberDark,
    onSecondaryContainer = Color(0xFFFFE08F),
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = DarkSurfaceVariant,
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
    error = ErrorRedLight,
)

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
