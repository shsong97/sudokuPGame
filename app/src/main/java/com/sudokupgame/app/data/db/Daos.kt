package com.sudokupgame.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedGameDao {
    @Query("SELECT * FROM saved_game WHERE id = 0")
    fun observe(): Flow<SavedGameEntity?>

    @Query("SELECT * FROM saved_game WHERE id = 0")
    suspend fun get(): SavedGameEntity?

    @Upsert
    suspend fun upsert(entity: SavedGameEntity)

    @Query("DELETE FROM saved_game")
    suspend fun clear()
}

@Dao
interface PuzzleRecordDao {
    @Query("SELECT * FROM puzzle_record")
    fun observeAll(): Flow<List<PuzzleRecordEntity>>

    @Query("SELECT * FROM puzzle_record WHERE puzzleId = :puzzleId")
    suspend fun get(puzzleId: String): PuzzleRecordEntity?

    @Upsert
    suspend fun upsert(entity: PuzzleRecordEntity)
}

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_record ORDER BY date")
    fun observeAll(): Flow<List<DailyRecordEntity>>

    /** 이미 클리어한 날짜면 처음 기록을 그대로 둔다. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: DailyRecordEntity)
}
