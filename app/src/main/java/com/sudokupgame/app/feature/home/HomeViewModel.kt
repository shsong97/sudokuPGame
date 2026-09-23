package com.sudokupgame.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.SavedGame
import com.sudokupgame.app.data.nextPuzzleToPlay
import com.sudokupgame.engine.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Loaded(val savedGame: SavedGame?) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    /** 불러오기 전에는 Loading. 버튼 배치가 바뀌며 잘못 눌리는 일을 막기 위해 구분한다. */
    val uiState: StateFlow<HomeUiState> = gameRepository.savedGame
        .map<SavedGame?, HomeUiState> { HomeUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    /** 새 게임으로 시작할 퍼즐 id (아직 클리어하지 않은 첫 퍼즐). */
    suspend fun puzzleForNewGame(difficulty: Difficulty): String? =
        nextPuzzleToPlay(puzzleRepository.puzzles(), gameRepository.records.first(), difficulty)
}
