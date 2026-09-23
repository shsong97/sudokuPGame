package com.sudokupgame.app.feature.stats

import com.sudokupgame.app.data.Puzzle
import com.sudokupgame.app.data.PuzzleRecord
import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DifficultyStatsTest {
    private val puzzles = listOf("E001", "E002", "E003", "M001").map { id ->
        Puzzle(id, if (id.startsWith("E")) Difficulty.EASY else Difficulty.MEDIUM, Board.EMPTY, Board.EMPTY)
    }

    @Test
    fun `난이도별로 기록을 모은다`() {
        val records = listOf(
            PuzzleRecord("E001", Difficulty.EASY, wins = 2, losses = 1, bestTimeSeconds = 100, totalWinTimeSeconds = 300, hintsUsed = 3),
            PuzzleRecord("E002", Difficulty.EASY, wins = 0, losses = 1, bestTimeSeconds = null, totalWinTimeSeconds = 0, hintsUsed = 1),
            PuzzleRecord("E003", Difficulty.EASY, wins = 1, losses = 0, bestTimeSeconds = 90, totalWinTimeSeconds = 90),
        )
        val easy = computeStats(puzzles, records).first { it.difficulty == Difficulty.EASY }
        assertEquals(3, easy.totalPuzzles)
        assertEquals(2, easy.clearedPuzzles)
        assertEquals(5, easy.played)
        assertEquals(3, easy.wins)
        assertEquals(60, easy.winRatePercent)
        assertEquals(90L, easy.bestTimeSeconds)
        assertEquals(130L, easy.averageTimeSeconds)
        assertEquals(4, easy.hintsUsed)
    }

    @Test
    fun `기록이 없는 난이도도 0으로 포함한다`() {
        val stats = computeStats(puzzles, emptyList())
        assertEquals(Difficulty.entries, stats.map { it.difficulty })
        val medium = stats.first { it.difficulty == Difficulty.MEDIUM }
        assertEquals(1, medium.totalPuzzles)
        assertEquals(0, medium.played)
        assertNull(medium.winRatePercent)
        assertNull(medium.averageTimeSeconds)
    }
}
