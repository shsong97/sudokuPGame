package com.sudokupgame.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.data.SavedGame
import com.sudokupgame.app.ui.OverwriteGameDialog
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label
import com.sudokupgame.app.ui.theme.SudokuTheme
import com.sudokupgame.engine.Difficulty
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onStartGame: (String) -> Unit,
    onPuzzles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loaded = uiState as? HomeUiState.Loaded
    val savedGame = loaded?.savedGame
    val scope = rememberCoroutineScope()
    var pendingPuzzleId by rememberSaveable { mutableStateOf<String?>(null) }

    HomeContent(
        loading = loaded == null,
        savedGame = savedGame,
        onContinue = { savedGame?.let { onStartGame(it.puzzleId) } },
        onNewGame = { difficulty ->
            scope.launch {
                val id = viewModel.puzzleForNewGame(difficulty) ?: return@launch
                val saved = savedGame
                if (saved != null && saved.puzzleId != id) pendingPuzzleId = id else onStartGame(id)
            }
        },
        onPuzzles = onPuzzles,
        onStats = onStats,
        onSettings = onSettings,
    )

    val pending = pendingPuzzleId
    val saved = savedGame
    if (pending != null && saved != null) {
        OverwriteGameDialog(
            savedPuzzleId = saved.puzzleId,
            onConfirm = {
                pendingPuzzleId = null
                onStartGame(pending)
            },
            onDismiss = { pendingPuzzleId = null },
        )
    }
}

@Composable
private fun HomeContent(
    loading: Boolean,
    savedGame: SavedGame?,
    onContinue: () -> Unit,
    onNewGame: (Difficulty) -> Unit,
    onPuzzles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
) {
    var choosingDifficulty by rememberSaveable { mutableStateOf(false) }

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
            if (loading) {
                // 저장된 게임 확인 전에는 버튼을 그리지 않는다 (대부분 수 ms 이내).
            } else if (savedGame != null) {
                Button(onClick = onContinue, modifier = buttonModifier) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.home_continue))
                        Text(
                            text = stringResource(
                                R.string.home_continue_detail,
                                savedGame.difficulty.label(),
                                savedGame.puzzleId,
                                formatTime(savedGame.elapsedSeconds),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { choosingDifficulty = true },
                    modifier = buttonModifier,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(R.string.home_new_game))
                }
            } else {
                Button(onClick = { choosingDifficulty = true }, modifier = buttonModifier) {
                    Text(stringResource(R.string.home_new_game))
                }
            }
            if (!loading) Spacer(Modifier.height(12.dp))
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

    if (choosingDifficulty) {
        DifficultySheet(
            onSelect = { difficulty ->
                choosingDifficulty = false
                onNewGame(difficulty)
            },
            onDismiss = { choosingDifficulty = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySheet(
    onSelect: (Difficulty) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.new_game_choose_difficulty),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        for (difficulty in Difficulty.entries) {
            ListItem(
                headlineContent = { Text(difficulty.label()) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onSelect(difficulty) },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    SudokuTheme {
        HomeContent(
            loading = false,
            savedGame = null,
            onContinue = {},
            onNewGame = {},
            onPuzzles = {},
            onStats = {},
            onSettings = {},
        )
    }
}
