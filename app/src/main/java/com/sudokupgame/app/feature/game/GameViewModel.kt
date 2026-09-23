package com.sudokupgame.app.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.PuzzleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GameUiState {
    data object Loading : GameUiState

    data object NotFound : GameUiState

    data class Ready(val game: GameState, val nextPuzzleId: String?) : GameUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PuzzleRepository,
) : ViewModel() {

    private val puzzleId: String = checkNotNull(savedStateHandle[PUZZLE_ID_KEY])

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val puzzle = repository.puzzle(puzzleId)
            if (puzzle == null) {
                _uiState.value = GameUiState.NotFound
                return@launch
            }
            _uiState.value = GameUiState.Ready(GameState.new(puzzle), repository.nextPuzzleId(puzzleId))
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (true) {
            delay(1_000)
            updateGame { it.tick() }
        }
    }

    fun onCellClick(index: Int) = updateGame { it.select(index) }

    fun onDigit(digit: Int) = updateGame { it.input(digit) }

    fun onErase() = updateGame { it.erase() }

    fun onUndo() = updateGame { it.undo() }

    fun onToggleNotes() = updateGame { it.toggleNotesMode() }

    fun onPause() = updateGame { it.pause() }

    fun onResume() = updateGame { it.resume() }

    fun onRestart() = updateGame { it.restart() }

    private fun updateGame(transform: (GameState) -> GameState) {
        _uiState.update { state ->
            if (state is GameUiState.Ready) state.copy(game = transform(state.game)) else state
        }
    }

    companion object {
        /** GameRoute.puzzleId 와 같은 이름. 타입 안전 라우트는 속성 이름을 키로 쓴다. */
        const val PUZZLE_ID_KEY = "puzzleId"
    }
}
