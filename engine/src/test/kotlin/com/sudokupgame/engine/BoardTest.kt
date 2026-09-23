package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BoardTest {
    @Test
    fun `81자 문자열을 읽고 다시 같은 문자열로 쓴다`() {
        val board = Board.parse(Fixtures.EASY_PUZZLE)
        assertEquals(Fixtures.EASY_PUZZLE, board.toCompactString())
        assertEquals(5, board[0, 0])
        assertEquals(0, board[0, 2])
        assertEquals(30, board.filledCount)
    }

    @Test
    fun `점은 빈칸으로 읽는다`() {
        assertEquals(Board.parse(Fixtures.EASY_PUZZLE), Board.parse(Fixtures.EASY_PUZZLE.replace('0', '.')))
    }

    @Test
    fun `잘못된 길이나 문자는 거부한다`() {
        assertThrows<IllegalArgumentException> { Board.parse("123") }
        assertThrows<IllegalArgumentException> { Board.parse("x" + Fixtures.EASY_PUZZLE.drop(1)) }
    }

    @Test
    fun `with는 원본을 바꾸지 않는다`() {
        val board = Board.EMPTY
        val changed = board.with(10, 7)
        assertEquals(0, board[10])
        assertEquals(7, changed[10])
    }

    @Test
    fun `규칙 검사`() {
        val puzzle = Board.parse(Fixtures.EASY_PUZZLE)
        val solution = Board.parse(Fixtures.EASY_SOLUTION)
        assertTrue(Rules.isConsistent(puzzle))
        assertTrue(Rules.isSolutionOf(solution, puzzle))
        assertFalse(Rules.canPlace(puzzle, Grid.index(0, 2), 5))
        assertTrue(Rules.canPlace(puzzle, Grid.index(0, 2), 4))
        assertFalse(Rules.isConsistent(puzzle.with(Grid.index(0, 2), 3)))
    }
}
