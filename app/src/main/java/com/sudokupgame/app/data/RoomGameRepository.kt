package com.sudokupgame.app.data

import androidx.room.withTransaction
import com.sudokupgame.app.data.db.PuzzleRecordEntity
import com.sudokupgame.app.data.db.SavedGameEntity
import com.sudokupgame.app.data.db.SudokuDatabase
import com.sudokupgame.engine.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomGameRepository @Inject constructor(
    private val db: SudokuDatabase,
) : GameRepository {

    private val savedGameDao = db.savedGameDao()
    private val recordDao = db.puzzleRecordDao()

    override val savedGame: Flow<SavedGame?> = savedGameDao.observe().map { it?.toModel() }

    override suspend fun getSavedGame(): SavedGame? = savedGameDao.get()?.toModel()

    override suspend fun saveGame(game: SavedGame) {
        savedGameDao.upsert(
            SavedGameEntity(
                puzzleId = game.puzzleId,
                difficulty = game.difficulty.name,
                cells = CellCodec.encode(game.cells),
                mistakes = game.mistakes,
                elapsedSeconds = game.elapsedSeconds,
                notesMode = game.notesMode,
                updatedAt = System.currentTimeMillis(),
                hintsUsed = game.hintsUsed,
            ),
        )
    }

    override suspend fun clearSavedGame() = savedGameDao.clear()

    override val records: Flow<List<PuzzleRecord>> =
        recordDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun recordResult(
        puzzleId: String,
        difficulty: Difficulty,
        won: Boolean,
        elapsedSeconds: Long,
        hintsUsed: Int,
    ) {
        db.withTransaction {
            val old = recordDao.get(puzzleId)
            val best = old?.bestTimeSeconds
            recordDao.upsert(
                PuzzleRecordEntity(
                    puzzleId = puzzleId,
                    difficulty = difficulty.name,
                    wins = (old?.wins ?: 0) + if (won) 1 else 0,
                    losses = (old?.losses ?: 0) + if (won) 0 else 1,
                    bestTimeSeconds = if (won) minOf(best ?: Long.MAX_VALUE, elapsedSeconds) else best,
                    totalWinTimeSeconds = (old?.totalWinTimeSeconds ?: 0) + if (won) elapsedSeconds else 0,
                    lastPlayedAt = System.currentTimeMillis(),
                    hintsUsed = (old?.hintsUsed ?: 0) + hintsUsed,
                ),
            )
        }
    }

    private fun SavedGameEntity.toModel() = SavedGame(
        puzzleId = puzzleId,
        difficulty = Difficulty.valueOf(difficulty),
        cells = CellCodec.decode(cells),
        mistakes = mistakes,
        elapsedSeconds = elapsedSeconds,
        notesMode = notesMode,
        hintsUsed = hintsUsed,
    )

    private fun PuzzleRecordEntity.toModel() = PuzzleRecord(
        puzzleId = puzzleId,
        difficulty = Difficulty.valueOf(difficulty),
        wins = wins,
        losses = losses,
        bestTimeSeconds = bestTimeSeconds,
        totalWinTimeSeconds = totalWinTimeSeconds,
        hintsUsed = hintsUsed,
    )
}
