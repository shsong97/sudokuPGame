package com.sudokupgame.app.data

import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PuzzleSelectionTest {
    private fun puzzle(id: String, difficulty: Difficulty) = Puzzle(id, difficulty, Board.EMPTY, Board.EMPTY)

    private fun cleared(id: String) = PuzzleRecord(id, Difficulty.EASY, wins = 1, losses = 0, bestTimeSeconds = 60, totalWinTimeSeconds = 60)

    private val puzzles = listOf(
        puzzle("E001", Difficulty.EASY),
        puzzle("E002", Difficulty.EASY),
        puzzle("M001", Difficulty.MEDIUM),
    )

    @Test
    fun `아직 클리어하지 않은 첫 퍼즐을 고른다`() {
        assertEquals("E001", nextPuzzleToPlay(puzzles, emptyList(), Difficulty.EASY))
        assertEquals("E002", nextPuzzleToPlay(puzzles, listOf(cleared("E001")), Difficulty.EASY))
    }

    @Test
    fun `게임 오버만 한 퍼즐은 아직 안 푼 것으로 본다`() {
        val lost = PuzzleRecord("E001", Difficulty.EASY, wins = 0, losses = 2, bestTimeSeconds = null, totalWinTimeSeconds = 0)
        assertEquals("E001", nextPuzzleToPlay(puzzles, listOf(lost), Difficulty.EASY))
    }

    @Test
    fun `모두 클리어했으면 첫 퍼즐부터 다시`() {
        assertEquals("E001", nextPuzzleToPlay(puzzles, listOf(cleared("E001"), cleared("E002")), Difficulty.EASY))
    }

    @Test
    fun `해당 난이도 퍼즐이 없으면 null`() {
        assertNull(nextPuzzleToPlay(puzzles, emptyList(), Difficulty.EXPERT))
    }
}
