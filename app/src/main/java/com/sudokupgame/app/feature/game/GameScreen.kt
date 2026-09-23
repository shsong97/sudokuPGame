package com.sudokupgame.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.data.Settings
import com.sudokupgame.app.ui.PlaceholderScreen
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onNextPuzzle: (String) -> Unit,
    onPuzzleList: () -> Unit,
    onHome: () -> Unit,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // 앱이 백그라운드로 가면 자동 일시정지 + 저장.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onPause() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                GameEvent.Mistake -> if (settings.vibration) haptic.performHapticFeedback(HapticFeedbackType.Reject)
            }
        }
    }

    when (val state = uiState) {
        GameUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        GameUiState.NotFound -> PlaceholderScreen(
            title = stringResource(R.string.game_not_found),
            onBack = onBack,
        )

        is GameUiState.Ready -> {
            GameContent(
                game = state.game,
                settings = settings,
                onBack = onBack,
                onCellClick = viewModel::onCellClick,
                onDigit = viewModel::onDigit,
                onErase = viewModel::onErase,
                onUndo = viewModel::onUndo,
                onToggleNotes = viewModel::onToggleNotes,
                onPause = viewModel::onPause,
                onResume = viewModel::onResume,
            )
            GameResultDialog(
                game = state.game,
                nextPuzzleId = state.nextPuzzleId,
                onNextPuzzle = onNextPuzzle,
                onHome = onHome,
                onRestart = viewModel::onRestart,
                onPuzzleList = onPuzzleList,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameContent(
    game: GameState,
    settings: Settings,
    onBack: () -> Unit,
    onCellClick: (Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val paused = game.status == GameStatus.PAUSED
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_title_format, game.difficulty.label(), game.puzzleId)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    if (game.status == GameStatus.PLAYING || paused) {
                        IconButton(onClick = if (paused) onResume else onPause) {
                            Icon(
                                painterResource(if (paused) R.drawable.ic_play else R.drawable.ic_pause),
                                stringResource(if (paused) R.string.game_resume else R.string.game_pause),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 480.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.game_mistakes, game.mistakes, MAX_MISTAKES),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (game.mistakes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (settings.showTimer) {
                        Text(
                            text = formatTime(game.elapsedSeconds),
                            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Box {
                    SudokuBoard(
                        game = game,
                        onCellClick = onCellClick,
                        highlightSameDigit = settings.highlightSameDigit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (paused) PausedOverlay(onResume, Modifier.matchParentSize())
                }

                Spacer(Modifier.height(16.dp))
                GameToolbar(
                    canUndo = game.canUndo,
                    notesMode = game.notesMode,
                    onUndo = onUndo,
                    onErase = onErase,
                    onToggleNotes = onToggleNotes,
                )
                Spacer(Modifier.height(8.dp))
                NumberPad(game = game, onDigit = onDigit)
            }
        }
    }
}

/** 일시정지 중에는 격자를 가린다. */
@Composable
private fun PausedOverlay(onResume: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.game_paused), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onResume) { Text(stringResource(R.string.game_resume)) }
    }
}

@Composable
private fun GameResultDialog(
    game: GameState,
    nextPuzzleId: String?,
    onNextPuzzle: (String) -> Unit,
    onHome: () -> Unit,
    onRestart: () -> Unit,
    onPuzzleList: () -> Unit,
) {
    when (game.status) {
        GameStatus.WON -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.game_won_title)) },
            text = { Text(stringResource(R.string.game_won_message, formatTime(game.elapsedSeconds), game.mistakes)) },
            confirmButton = {
                if (nextPuzzleId != null) {
                    Button(onClick = { onNextPuzzle(nextPuzzleId) }) { Text(stringResource(R.string.game_next_puzzle)) }
                }
            },
            dismissButton = {
                TextButton(onClick = onHome) { Text(stringResource(R.string.game_home)) }
            },
        )

        GameStatus.LOST -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.game_lost_title)) },
            text = { Text(stringResource(R.string.game_lost_message, MAX_MISTAKES)) },
            confirmButton = {
                Button(onClick = onRestart) { Text(stringResource(R.string.game_restart)) }
            },
            dismissButton = {
                TextButton(onClick = onPuzzleList) { Text(stringResource(R.string.game_other_puzzle)) }
            },
        )

        GameStatus.PLAYING, GameStatus.PAUSED -> Unit
    }
}
