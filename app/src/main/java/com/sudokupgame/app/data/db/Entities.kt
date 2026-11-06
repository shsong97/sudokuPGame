package com.sudokupgame.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_game")
data class SavedGameEntity(
    /** 진행 중인 게임은 하나뿐이라 항상 0. */
    @PrimaryKey val id: Int = 0,
    val puzzleId: String,
    val difficulty: String,
    val cells: String,
    val mistakes: Int,
    val elapsedSeconds: Long,
    val notesMode: Boolean,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val hintsUsed: Int = 0,
    @ColumnInfo(defaultValue = "NUMBER") val mode: String = "NUMBER",
    /** 데일리 챌린지로 시작한 게임이면 그 날짜(LocalDate.toEpochDay), 아니면 null. */
    val dailyDate: Long? = null,
)

@Entity(tableName = "puzzle_record")
data class PuzzleRecordEntity(
    @PrimaryKey val puzzleId: String,
    val difficulty: String,
    val wins: Int,
    val losses: Int,
    val bestTimeSeconds: Long?,
    val totalWinTimeSeconds: Long,
    val lastPlayedAt: Long,
    /** 끝낸 게임들에서 쓴 힌트 합계. */
    @ColumnInfo(defaultValue = "0") val hintsUsed: Int = 0,
)

/** 데일리 챌린지 클리어 기록. 날짜마다 하나 (처음 클리어한 것). */
@Entity(tableName = "daily_record")
data class DailyRecordEntity(
    /** LocalDate.toEpochDay */
    @PrimaryKey val date: Long,
    val puzzleId: String,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
    val clearedAt: Long,
)
