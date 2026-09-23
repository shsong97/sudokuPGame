package com.sudokupgame.app.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class, PuzzleRecordEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // 2: 힌트 사용 횟수 열 추가
        AutoMigration(from = 1, to = 2),
        // 3: 진행 중인 게임의 표시 모드(숫자/동물) 열 추가
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao

    abstract fun puzzleRecordDao(): PuzzleRecordDao
}
