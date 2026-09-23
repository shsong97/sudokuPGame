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
