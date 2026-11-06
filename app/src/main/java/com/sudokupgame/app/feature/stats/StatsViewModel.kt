package com.sudokupgame.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.DailyStats
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.computeDailyStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    puzzleRepository: PuzzleRepository,
    gameRepository: GameRepository,
    clock: Clock,
) : ViewModel() {

    /** 불러오기 전에는 null. */
    val stats: StateFlow<List<DifficultyStats>?> = combine(
        flow { emit(puzzleRepository.puzzles()) },
        gameRepository.records,
        ::computeStats,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 불러오기 전에는 null. */
    val dailyStats: StateFlow<DailyStats?> = gameRepository.dailyRecords
        .map { computeDailyStats(it, LocalDate.now(clock)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
