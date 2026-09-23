package com.sudokupgame.app.feature.puzzles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.SettingsRepository
import com.sudokupgame.engine.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PuzzleItem(
    val id: String,
    val number: Int,
    val isCleared: Boolean,
    val bestTimeSeconds: Long?,
    val isInProgress: Boolean,
)

data class PuzzlesUiState(
    val loading: Boolean = true,
    val items: Map<Difficulty, List<PuzzleItem>> = emptyMap(),
    val savedPuzzleId: String? = null,
    val mode: GameMode = GameMode.NUMBER,
)

@HiltViewModel
class PuzzlesViewModel @Inject constructor(
    puzzleRepository: PuzzleRepository,
    gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<PuzzlesUiState> = combine(
        flow { emit(puzzleRepository.puzzles()) },
        gameRepository.records,
        gameRepository.savedGame,
        settingsRepository.settings,
    ) { puzzles, records, saved, settings ->
        val recordById = records.associateBy { it.puzzleId }
        PuzzlesUiState(
            loading = false,
            items = puzzles.groupBy { it.difficulty }.mapValues { (_, list) ->
                list.mapIndexed { i, puzzle ->
                    val record = recordById[puzzle.id]
                    PuzzleItem(
                        id = puzzle.id,
                        number = i + 1,
                        isCleared = record?.isCleared == true,
                        bestTimeSeconds = record?.bestTimeSeconds,
                        isInProgress = saved?.puzzleId == puzzle.id,
                    )
                }
            },
            savedPuzzleId = saved?.puzzleId,
            mode = settings.lastGameMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PuzzlesUiState())

    fun setMode(mode: GameMode) {
        viewModelScope.launch { settingsRepository.update { it.copy(lastGameMode = mode) } }
    }
}
