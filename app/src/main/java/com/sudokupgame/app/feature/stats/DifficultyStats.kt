package com.sudokupgame.app.feature.stats

import com.sudokupgame.app.data.Puzzle
import com.sudokupgame.app.data.PuzzleRecord
import com.sudokupgame.engine.Difficulty

data class DifficultyStats(
    val difficulty: Difficulty,
    val totalPuzzles: Int,
    val clearedPuzzles: Int,
    val played: Int,
    val wins: Int,
    val bestTimeSeconds: Long?,
    val averageTimeSeconds: Long?,
    val hintsUsed: Int,
) {
    /** 0~100. 끝낸 게임이 없으면 null. */
    val winRatePercent: Int? get() = if (played == 0) null else wins * 100 / played
}

/** 퍼즐별 기록을 난이도별로 모은다. 기록이 없는 난이도도 0으로 포함한다. */
fun computeStats(puzzles: List<Puzzle>, records: List<PuzzleRecord>): List<DifficultyStats> =
    Difficulty.entries.map { difficulty ->
        val list = records.filter { it.difficulty == difficulty }
        val wins = list.sumOf { it.wins }
        DifficultyStats(
            difficulty = difficulty,
            totalPuzzles = puzzles.count { it.difficulty == difficulty },
            clearedPuzzles = list.count { it.isCleared },
            played = wins + list.sumOf { it.losses },
            wins = wins,
            bestTimeSeconds = list.mapNotNull { it.bestTimeSeconds }.minOrNull(),
            averageTimeSeconds = if (wins == 0) null else list.sumOf { it.totalWinTimeSeconds } / wins,
            hintsUsed = list.sumOf { it.hintsUsed },
        )
    }
