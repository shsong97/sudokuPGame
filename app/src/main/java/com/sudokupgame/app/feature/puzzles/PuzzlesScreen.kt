package com.sudokupgame.app.feature.puzzles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.PlaceholderScreen

@Composable
fun PuzzlesScreen(onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.puzzles_title), onBack = onBack)
}
