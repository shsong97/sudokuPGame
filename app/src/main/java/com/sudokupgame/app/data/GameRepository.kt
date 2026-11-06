package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val savedGame: Flow<SavedGame?>

    suspend fun getSavedGame(): SavedGame?

    suspend fun saveGame(game: SavedGame)

    suspend fun clearSavedGame()

    val records: Flow<List<PuzzleRecord>>

    /** 끝난 게임 결과를 퍼즐 기록에 더한다. */
    suspend fun recordResult(
        puzzleId: String,
        difficulty: Difficulty,
        won: Boolean,
        elapsedSeconds: Long,
        hintsUsed: Int,
    )

    /** 데일리 챌린지 클리어 기록 (날짜순). */
    val dailyRecords: Flow<List<DailyRecord>>

    /** 데일리 챌린지 클리어를 남긴다. 그 날짜에 이미 기록이 있으면 무시한다. */
    suspend fun recordDailyClear(record: DailyRecord)
}
