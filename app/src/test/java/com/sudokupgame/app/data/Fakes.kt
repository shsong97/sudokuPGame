package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePuzzleRepository(private val list: List<Puzzle>) : PuzzleRepository {
    override suspend fun puzzles(): List<Puzzle> = list
}

class FakeGameRepository : GameRepository {
    val saved = MutableStateFlow<SavedGame?>(null)
    val recordList = MutableStateFlow<List<PuzzleRecord>>(emptyList())
    var saveCount = 0

    override val savedGame: Flow<SavedGame?> = saved

    override suspend fun getSavedGame(): SavedGame? = saved.value

    override suspend fun saveGame(game: SavedGame) {
        saveCount++
        saved.value = game
    }

    override suspend fun clearSavedGame() {
        saved.value = null
    }

    override val records: Flow<List<PuzzleRecord>> = recordList

    override suspend fun recordResult(puzzleId: String, difficulty: Difficulty, won: Boolean, elapsedSeconds: Long) {
        val old = recordList.value.find { it.puzzleId == puzzleId }
            ?: PuzzleRecord(puzzleId, difficulty, 0, 0, null, 0)
        val new = old.copy(
            wins = old.wins + if (won) 1 else 0,
            losses = old.losses + if (won) 0 else 1,
            bestTimeSeconds = if (won) minOf(old.bestTimeSeconds ?: Long.MAX_VALUE, elapsedSeconds) else old.bestTimeSeconds,
        )
        recordList.value = recordList.value.filter { it.puzzleId != puzzleId } + new
    }
}

class FakeSettingsRepository(initial: Settings = Settings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<Settings> = state

    override suspend fun update(transform: (Settings) -> Settings) {
        state.value = transform(state.value)
    }
}
