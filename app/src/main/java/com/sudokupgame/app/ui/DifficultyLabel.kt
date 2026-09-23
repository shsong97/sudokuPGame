package com.sudokupgame.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.engine.Difficulty

@Composable
fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
        Difficulty.EXPERT -> R.string.difficulty_expert
    },
)
