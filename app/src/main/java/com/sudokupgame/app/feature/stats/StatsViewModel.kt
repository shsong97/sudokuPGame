package com.sudokupgame.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    puzzleRepository: PuzzleRepository,
    gameRepository: GameRepository,
) : ViewModel() {

    /** 불러오기 전에는 null. */
    val stats: StateFlow<List<DifficultyStats>?> = combine(
        flow { emit(puzzleRepository.puzzles()) },
        gameRepository.records,
        ::computeStats,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
