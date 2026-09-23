package com.sudokupgame.app.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.PlaceholderScreen

@Composable
fun GameScreen(puzzleId: String, onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.game_title, puzzleId), onBack = onBack)
}
