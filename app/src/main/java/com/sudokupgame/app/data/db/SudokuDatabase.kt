package com.sudokupgame.app.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class, PuzzleRecordEntity::class, DailyRecordEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        // 2: 힌트 사용 횟수 열 추가
        AutoMigration(from = 1, to = 2),
        // 3: 진행 중인 게임의 표시 모드(숫자/동물) 열 추가
        AutoMigration(from = 2, to = 3),
        // 4: 데일리 챌린지 (진행 중인 게임의 날짜 열, 클리어 기록 표)
        AutoMigration(from = 3, to = 4),
    ],
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao

    abstract fun puzzleRecordDao(): PuzzleRecordDao

    abstract fun dailyRecordDao(): DailyRecordDao
}
