package com.sudokupgame.app.feature.puzzles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.ui.AdBannerSlot
import com.sudokupgame.app.ui.GameModeSelector
import com.sudokupgame.app.ui.OverwriteGameDialog
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label
import com.sudokupgame.engine.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzlesScreen(
    onBack: () -> Unit,
    onStartGame: (String, GameMode) -> Unit,
    viewModel: PuzzlesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pendingPuzzleId by rememberSaveable { mutableStateOf<String?>(null) }

    val onPuzzleClick: (String) -> Unit = { id ->
        val saved = state.savedPuzzleId
        if (saved != null && saved != id) pendingPuzzleId = id else onStartGame(id, state.mode)
    }

    Scaffold(
        bottomBar = { AdBannerSlot() },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.puzzles_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.navigate_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GameModeSelector(
                selected = state.mode,
                onSelect = viewModel::setMode,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                Difficulty.entries.forEachIndexed { index, difficulty ->
                    val items = state.items[difficulty].orEmpty()
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                stringResource(
                                    R.string.puzzles_tab,
                                    difficulty.label(),
                                    items.count { it.isCleared },
                                    items.size,
                                ),
                            )
                        },
                    )
                }
            }
            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                PuzzleGrid(
                    items = state.items[Difficulty.entries[selectedTab]].orEmpty(),
                    onClick = onPuzzleClick,
                )
            }
        }
    }

    val pending = pendingPuzzleId
    val saved = state.savedPuzzleId
    if (pending != null && saved != null) {
        OverwriteGameDialog(
            savedPuzzleId = saved,
            onConfirm = {
                pendingPuzzleId = null
                onStartGame(pending, state.mode)
            },
            onDismiss = { pendingPuzzleId = null },
        )
    }
}

@Composable
private fun PuzzleGrid(items: List<PuzzleItem>, onClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item -> PuzzleTile(item, onClick = { onClick(item.id) }) }
    }
}

@Composable
private fun PuzzleTile(item: PuzzleItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container = when {
        item.isInProgress -> colors.secondaryContainer
        item.isCleared -> colors.primary.copy(alpha = 0.12f)
        else -> colors.surfaceContainer
    }
    val numberDescription = stringResource(R.string.puzzles_item_description, item.number)
    val statusDescription = when {
        item.isInProgress -> stringResource(R.string.puzzles_in_progress)
        item.isCleared -> stringResource(R.string.puzzles_item_cleared, formatTime(item.bestTimeSeconds ?: 0))
        else -> null
    }
    Surface(
        onClick = onClick,
        color = container,
        shape = MaterialTheme.shapes.medium,
        border = if (item.isInProgress) BorderStroke(2.dp, colors.secondary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = listOfNotNull(numberDescription, statusDescription).joinToString(", ") },
    ) {
        Box(Modifier.clearAndSetSemantics {}) {
            if (item.isCleared) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp),
                )
            }
            Text(
                text = item.number.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isCleared) colors.primary else colors.onSurface,
                modifier = Modifier.align(Alignment.Center),
            )
            val caption = when {
                item.isInProgress -> stringResource(R.string.puzzles_in_progress)
                item.bestTimeSeconds != null -> formatTime(item.bestTimeSeconds)
                else -> null
            }
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                )
            }
        }
    }
}
