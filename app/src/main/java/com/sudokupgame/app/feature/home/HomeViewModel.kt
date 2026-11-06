package com.sudokupgame.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.DailyRecord
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.SavedGame
import com.sudokupgame.app.data.SettingsRepository
import com.sudokupgame.app.data.currentStreak
import com.sudokupgame.app.data.dailyDifficulty
import com.sudokupgame.app.data.dailyPuzzleId
import com.sudokupgame.app.data.nextPuzzleToPlay
import com.sudokupgame.engine.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Loaded(val savedGame: SavedGame?) : HomeUiState
}

/** 홈 화면의 오늘의 챌린지 카드. */
data class DailyUiState(
    val date: LocalDate,
    val puzzleId: String,
    val difficulty: Difficulty,
    /** 오늘 클리어했으면 그 기록. */
    val todayRecord: DailyRecord?,
    val streak: Int,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    /** 새 게임에 쓸 모드 (마지막으로 고른 것). */
    val mode: StateFlow<GameMode> = settingsRepository.settings
        .map { it.lastGameMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameMode.NUMBER)

    fun setMode(mode: GameMode) {
        viewModelScope.launch { settingsRepository.update { it.copy(lastGameMode = mode) } }
    }

    /** 불러오기 전에는 Loading. 버튼 배치가 바뀌며 잘못 눌리는 일을 막기 위해 구분한다. */
    val uiState: StateFlow<HomeUiState> = gameRepository.savedGame
        .map<SavedGame?, HomeUiState> { HomeUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    /** 화면이 다시 보일 때마다 바꿔서, 자정이 지나면 오늘의 챌린지를 새 날짜로 다시 고른다. */
    private val refresh = MutableStateFlow(0)

    fun refreshDaily() {
        refresh.value++
    }

    /** 불러오기 전이거나 오늘 난이도의 퍼즐이 없으면 null. */
    val daily: StateFlow<DailyUiState?> = combine(
        flow { emit(puzzleRepository.puzzles()) },
        gameRepository.dailyRecords,
        refresh,
    ) { puzzles, records, _ ->
        val today = LocalDate.now(clock)
        dailyPuzzleId(puzzles, today)?.let { id ->
            DailyUiState(
                date = today,
                puzzleId = id,
                difficulty = dailyDifficulty(today),
                todayRecord = records.find { it.date == today },
                streak = currentStreak(records.mapTo(mutableSetOf()) { it.date }, today),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 새 게임으로 시작할 퍼즐 id (아직 클리어하지 않은 첫 퍼즐). */
    suspend fun puzzleForNewGame(difficulty: Difficulty): String? =
        nextPuzzleToPlay(puzzleRepository.puzzles(), gameRepository.records.first(), difficulty)
}
