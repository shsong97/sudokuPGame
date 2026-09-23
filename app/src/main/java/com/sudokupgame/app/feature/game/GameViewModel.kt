package com.sudokupgame.app.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.Settings
import com.sudokupgame.app.data.SettingsRepository
import com.sudokupgame.app.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

sealed interface GameUiState {
    data object Loading : GameUiState

    data object NotFound : GameUiState

    data class Ready(val game: GameState, val nextPuzzleId: String?) : GameUiState
}

/** 화면에 한 번만 알리는 이벤트. */
sealed interface GameEvent {
    data object Mistake : GameEvent
}

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val puzzleRepository: PuzzleRepository,
    private val gameRepository: GameRepository,
    settingsRepository: SettingsRepository,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val puzzleId: String = checkNotNull(savedStateHandle[PUZZLE_ID_KEY])

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val settings: StateFlow<Settings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val writeMutex = Mutex()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val puzzle = puzzleRepository.puzzle(puzzleId)
            if (puzzle == null) {
                _uiState.value = GameUiState.NotFound
                return@launch
            }
            val saved = gameRepository.getSavedGame()?.takeIf { it.puzzleId == puzzleId }
            val game = if (saved != null) GameState.restore(puzzle, saved) else GameState.new(puzzle)
            _uiState.value = GameUiState.Ready(game, puzzleRepository.nextPuzzleId(puzzleId))
            launch { autoSave() }
            runTimer()
        }
    }

    private suspend fun runTimer() {
        while (true) {
            delay(1_000)
            updateGame { it.tick() }
        }
    }

    /**
     * 칸·실수·상태가 바뀌면 0.5초 뒤 저장한다. 시간은 매초 바뀌므로 10초 단위로만 저장해
     * 앱이 강제 종료돼도 잃는 시간이 10초를 넘지 않게 한다.
     */
    @OptIn(FlowPreview::class)
    private suspend fun autoSave() {
        _uiState.filterIsInstance<GameUiState.Ready>()
            .map { it.game }
            .distinctUntilChanged { old, new ->
                old.cells == new.cells && old.mistakes == new.mistakes && old.status == new.status &&
                    old.elapsedSeconds / TIME_SAVE_INTERVAL_SECONDS == new.elapsedSeconds / TIME_SAVE_INTERVAL_SECONDS
            }
            .debounce(SAVE_DEBOUNCE_MILLIS)
            .collect { persist(it) }
    }

    private fun persist(game: GameState) {
        when (game.status) {
            GameStatus.PLAYING, GameStatus.PAUSED -> write { gameRepository.saveGame(game.toSavedGame()) }
            GameStatus.WON, GameStatus.LOST -> Unit // 끝난 게임은 onFinished에서 정리한다.
        }
    }

    /**
     * 저장은 앱 스코프에서 해서, 화면을 떠나 ViewModel이 정리돼도 끝까지 마친다.
     * 호출 순서대로 하나씩 실행해 오래된 상태가 나중에 덮어쓰지 않게 한다.
     */
    private fun write(block: suspend () -> Unit) {
        appScope.launch(start = CoroutineStart.UNDISPATCHED) {
            writeMutex.withLock { block() }
        }
    }

    /** 화면을 떠날 때 마지막 경과 시간까지 저장한다. */
    override fun onCleared() {
        (uiState.value as? GameUiState.Ready)?.let { persist(it.game) }
    }

    fun onCellClick(index: Int) = updateGame { it.select(index) }

    fun onDigit(digit: Int) = updateGame { it.input(digit, settings.value.autoRemoveNotes) }

    fun onErase() = updateGame { it.erase() }

    fun onUndo() = updateGame { it.undo() }

    fun onToggleNotes() = updateGame { it.toggleNotesMode() }

    /** 일시정지하고 바로 저장한다 (앱이 백그라운드로 갈 때도 호출). */
    fun onPause() {
        updateGame { it.pause() }
        (uiState.value as? GameUiState.Ready)?.let { persist(it.game) }
    }

    fun onResume() = updateGame { it.resume() }

    fun onRestart() = updateGame { it.restart() }

    private fun updateGame(transform: (GameState) -> GameState) {
        val state = _uiState.value as? GameUiState.Ready ?: return
        val old = state.game
        val new = transform(old)
        if (new === old) return
        _uiState.value = state.copy(game = new)

        if (new.mistakes > old.mistakes) _events.tryEmit(GameEvent.Mistake)
        if (old.status != new.status && (new.status == GameStatus.WON || new.status == GameStatus.LOST)) {
            onFinished(new)
        }
    }

    private fun onFinished(game: GameState) {
        write {
            gameRepository.recordResult(
                puzzleId = game.puzzleId,
                difficulty = game.difficulty,
                won = game.status == GameStatus.WON,
                elapsedSeconds = game.elapsedSeconds,
            )
            gameRepository.clearSavedGame()
        }
    }

    companion object {
        /** GameRoute.puzzleId 와 같은 이름. 타입 안전 라우트는 속성 이름을 키로 쓴다. */
        const val PUZZLE_ID_KEY = "puzzleId"
        private const val SAVE_DEBOUNCE_MILLIS = 500L
        private const val TIME_SAVE_INTERVAL_SECONDS = 10
    }
}
