package com.sudokupgame.app.feature.game

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.data.Settings
import com.sudokupgame.app.ui.AdBannerSlot
import com.sudokupgame.app.ui.PlaceholderScreen
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onNextPuzzle: (String, GameMode) -> Unit,
    onPuzzleList: () -> Unit,
    onHome: () -> Unit,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // 앱이 백그라운드로 가면 자동 일시정지 + 저장. 화면 회전으로 다시 만들어질 때는 제외한다.
    val activity = LocalActivity.current
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (activity?.isChangingConfigurations != true) viewModel.onPause()
    }

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
                onHint = viewModel::onHint,
                onRevealHint = viewModel::onRevealHint,
                onDismissHint = viewModel::onDismissHint,
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
    onHint: () -> Unit,
    onRevealHint: () -> Unit,
    onDismissHint: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val paused = game.status == GameStatus.PAUSED
    Scaffold(
        bottomBar = { AdBannerSlot() },
        topBar = {
            TopAppBar(
                title = {
                    val format = if (game.mode == GameMode.ANIMAL) R.string.game_title_animal_format else R.string.game_title_format
                    Text(stringResource(format, game.difficulty.label(), game.puzzleId))
                },
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
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (maxWidth > maxHeight) {
                // 가로: 보드는 높이에 맞추고, 오른쪽에 도구와 3×3 숫자 패드.
                val boardWidth = minOf(maxHeight - INFO_ROW_HEIGHT, maxWidth * 0.6f)
                // 높이가 낮으면(휴대전화 가로 + 하단 광고) 숫자 패드를 한 줄로, 넉넉하면 3×3으로.
                val padColumns = if (maxHeight < 360.dp) 9 else 3
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(Modifier.width(boardWidth)) {
                        InfoRow(game, settings.showTimer)
                        BoardWithOverlay(game, settings, onCellClick, onResume)
                    }
                    Column(
                        Modifier
                            .widthIn(max = 360.dp)
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Controls(game, onDigit, onErase, onUndo, onToggleNotes, onHint, onRevealHint, onDismissHint, padColumns)
                    }
                }
            } else {
                // 세로: 위쪽부터 배치한다. 태블릿에서는 폭을 제한하고, 키가 작은 화면에서는
                // 조작부가 가려지지 않도록 보드를 남은 높이에 맞춰 줄인다.
                val contentWidth = minOf(600.dp, maxWidth, maxHeight - PORTRAIT_CONTROLS_HEIGHT)
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(Modifier.width(contentWidth)) {
                        InfoRow(game, settings.showTimer)
                        BoardWithOverlay(game, settings, onCellClick, onResume)
                        Spacer(Modifier.height(16.dp))
                        Controls(game, onDigit, onErase, onUndo, onToggleNotes, onHint, onRevealHint, onDismissHint, padColumns = 9)
                    }
                }
            }
        }
    }
}

private val INFO_ROW_HEIGHT = 40.dp

/** 세로 화면에서 보드 외에 필요한 높이: 정보 줄 + 여백 + 도구 모음 + 숫자 패드. */
private val PORTRAIT_CONTROLS_HEIGHT = 210.dp

@Composable
private fun InfoRow(game: GameState, showTimer: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = INFO_ROW_HEIGHT)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.game_mistakes, game.mistakes, MAX_MISTAKES),
            style = MaterialTheme.typography.titleSmall,
            color = if (game.mistakes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showTimer) {
            Text(
                text = formatTime(game.elapsedSeconds),
                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BoardWithOverlay(
    game: GameState,
    settings: Settings,
    onCellClick: (Int) -> Unit,
    onResume: () -> Unit,
) {
    Box {
        SudokuBoard(
            game = game,
            onCellClick = onCellClick,
            highlightSameDigit = settings.highlightSameDigit,
            modifier = Modifier.fillMaxWidth(),
        )
        if (game.status == GameStatus.PAUSED) PausedOverlay(onResume, Modifier.matchParentSize())
    }
}

@Composable
private fun Controls(
    game: GameState,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onRevealHint: () -> Unit,
    onDismissHint: () -> Unit,
    padColumns: Int,
) {
    // 힌트가 뜨고 닫힐 때만 전환 효과. 힌트 내용이 바뀌는 것은 즉시 반영.
    AnimatedContent(
        targetState = game.hint,
        contentKey = { it != null },
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
        label = "hint",
    ) { hint ->
        if (hint != null) {
            HintCard(hint = hint, mode = game.mode, onReveal = onRevealHint, onDismiss = onDismissHint)
        } else {
            GameToolbar(
                canUndo = game.canUndo,
                notesMode = game.notesMode,
                onUndo = onUndo,
                onErase = onErase,
                onToggleNotes = onToggleNotes,
                onHint = onHint,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    NumberPad(game = game, onDigit = onDigit, columns = padColumns)
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
    onNextPuzzle: (String, GameMode) -> Unit,
    onHome: () -> Unit,
    onRestart: () -> Unit,
    onPuzzleList: () -> Unit,
) {
    when (game.status) {
        GameStatus.WON -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.game_won_title)) },
            text = {
                Text(stringResource(R.string.game_won_message, formatTime(game.elapsedSeconds), game.mistakes, game.hintsUsed))
            },
            confirmButton = {
                if (nextPuzzleId != null) {
                    Button(onClick = { onNextPuzzle(nextPuzzleId, game.mode) }) { Text(stringResource(R.string.game_next_puzzle)) }
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
