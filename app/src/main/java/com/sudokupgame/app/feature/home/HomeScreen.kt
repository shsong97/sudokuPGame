package com.sudokupgame.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.data.SavedGame
import com.sudokupgame.app.ui.AdBannerSlot
import com.sudokupgame.app.ui.GameModeSelector
import com.sudokupgame.app.ui.OverwriteGameDialog
import com.sudokupgame.app.ui.formatDailyDate
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label
import com.sudokupgame.app.ui.theme.SudokuTheme
import com.sudokupgame.engine.Difficulty
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(
    /** 퍼즐 id, 표시 모드, 데일리 챌린지 날짜(ISO, 아니면 null). */
    onStartGame: (String, GameMode, String?) -> Unit,
    onPuzzles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loaded = uiState as? HomeUiState.Loaded
    val savedGame = loaded?.savedGame
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pendingPuzzleId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDailyDate by rememberSaveable { mutableStateOf<String?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDaily() }

    /** 진행 중인 다른 퍼즐이 있으면 덮어쓸지 먼저 묻는다. */
    val start: (String, String?) -> Unit = { id, dailyDate ->
        val saved = savedGame
        if (saved != null && saved.puzzleId != id) {
            pendingPuzzleId = id
            pendingDailyDate = dailyDate
        } else {
            onStartGame(id, mode, dailyDate)
        }
    }

    HomeContent(
        loading = loaded == null,
        savedGame = savedGame,
        daily = daily,
        mode = mode,
        onModeChange = viewModel::setMode,
        onContinue = { savedGame?.let { onStartGame(it.puzzleId, it.mode, it.dailyDate?.toString()) } },
        onDaily = { daily?.let { start(it.puzzleId, it.date.toString()) } },
        onNewGame = { difficulty ->
            scope.launch {
                val id = viewModel.puzzleForNewGame(difficulty) ?: return@launch
                start(id, null)
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
                onStartGame(pending, mode, pendingDailyDate)
            },
            onDismiss = { pendingPuzzleId = null },
        )
    }
}

@Composable
private fun HomeContent(
    loading: Boolean,
    savedGame: SavedGame?,
    daily: DailyUiState?,
    mode: GameMode,
    onModeChange: (GameMode) -> Unit,
    onContinue: () -> Unit,
    onDaily: () -> Unit,
    onNewGame: (Difficulty) -> Unit,
    onPuzzles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
) {
    var choosingDifficulty by rememberSaveable { mutableStateOf(false) }

    Scaffold(bottomBar = { AdBannerSlot() }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 가운데보다 약간 위에 오도록 위 여백을 아래 여백보다 작게 준다.
            Spacer(Modifier.weight(0.6f))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(32.dp))

            val buttonModifier = Modifier.fillMaxWidth().widthIn(max = 320.dp)
            if (!loading && daily != null) {
                DailyCard(daily = daily, onClick = onDaily, modifier = buttonModifier)
                Spacer(Modifier.height(16.dp))
            }
            if (loading) {
                // 저장된 게임 확인 전에는 버튼을 그리지 않는다 (대부분 수 ms 이내).
            } else if (savedGame != null) {
                Button(onClick = onContinue, modifier = buttonModifier) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.home_continue))
                        Text(
                            text = (if (savedGame.mode == GameMode.ANIMAL) "🐾 " else "") + stringResource(
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
            Spacer(Modifier.weight(1f))
        }
    }

    if (choosingDifficulty) {
        DifficultySheet(
            mode = mode,
            onModeChange = onModeChange,
            onSelect = { difficulty ->
                choosingDifficulty = false
                onNewGame(difficulty)
            },
            onDismiss = { choosingDifficulty = false },
        )
    }
}

@Composable
private fun DailyCard(daily: DailyUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val record = daily.todayRecord
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (record != null) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            },
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.daily_subtitle, formatDailyDate(daily.date), daily.difficulty.label()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (record != null) {
                    Text(
                        text = stringResource(R.string.daily_cleared),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = formatTime(record.elapsedSeconds),
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.daily_play),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                if (daily.streak > 0) {
                    Text(
                        text = stringResource(R.string.daily_streak_short, daily.streak),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySheet(
    mode: GameMode,
    onModeChange: (GameMode) -> Unit,
    onSelect: (Difficulty) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        GameModeSelector(
            selected = mode,
            onSelect = onModeChange,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
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
            daily = DailyUiState(LocalDate.of(2026, 11, 6), "H001", Difficulty.HARD, todayRecord = null, streak = 3),
            mode = GameMode.NUMBER,
            onModeChange = {},
            onContinue = {},
            onDaily = {},
            onNewGame = {},
            onPuzzles = {},
            onStats = {},
            onSettings = {},
        )
    }
}
