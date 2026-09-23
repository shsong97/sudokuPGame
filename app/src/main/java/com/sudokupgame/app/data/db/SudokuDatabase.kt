package com.sudokupgame.app.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class, PuzzleRecordEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // 2: 힌트 사용 횟수 열 추가
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao

    abstract fun puzzleRecordDao(): PuzzleRecordDao
}
