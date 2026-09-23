package com.sudokupgame.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class, PuzzleRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao

    abstract fun puzzleRecordDao(): PuzzleRecordDao
}
