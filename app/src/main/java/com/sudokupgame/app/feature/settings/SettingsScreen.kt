package com.sudokupgame.app.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.PlaceholderScreen

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.settings_title), onBack = onBack)
}
