package com.sudokupgame.app.feature.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.PlaceholderScreen

@Composable
fun StatsScreen(onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.stats_title), onBack = onBack)
}
