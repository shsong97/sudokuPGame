package com.sudokupgame.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.theme.SudokuTheme

@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onPuzzles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(48.dp))

            val buttonModifier = Modifier.fillMaxWidth().widthIn(max = 320.dp)
            Button(onClick = onNewGame, modifier = buttonModifier) {
                Text(stringResource(R.string.home_new_game))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onPuzzles, modifier = buttonModifier) {
                Text(stringResource(R.string.home_puzzles))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onStats, modifier = buttonModifier) {
                Text(stringResource(R.string.home_stats))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onSettings, modifier = buttonModifier) {
                Text(stringResource(R.string.home_settings))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SudokuTheme {
        HomeScreen(onNewGame = {}, onPuzzles = {}, onStats = {}, onSettings = {})
    }
}
