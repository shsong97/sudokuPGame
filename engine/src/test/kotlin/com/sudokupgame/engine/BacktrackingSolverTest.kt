package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BacktrackingSolverTest {
    @Test
    fun `쉬운 퍼즐을 정답으로 푼다`() {
        val solved = BacktrackingSolver.solve(Board.parse(Fixtures.EASY_PUZZLE))
        assertEquals(Board.parse(Fixtures.EASY_SOLUTION), solved)
    }

    @Test
    fun `매우 어려운 퍼즐도 유일해로 푼다`() {
        val puzzle = Board.parse(Fixtures.HARDEST_PUZZLE)
        assertEquals(1, BacktrackingSolver.countSolutions(puzzle))
        assertTrue(Rules.isSolutionOf(BacktrackingSolver.solve(puzzle)!!, puzzle))
    }

    @Test
    fun `해가 여러 개인 보드는 limit까지만 센다`() {
        assertEquals(2, BacktrackingSolver.countSolutions(Board.EMPTY, limit = 2))
        assertEquals(5, BacktrackingSolver.countSolutions(Board.EMPTY, limit = 5))
    }

    @Test
    fun `충돌이 있는 보드는 해가 없다`() {
        val broken = Board.parse(Fixtures.EASY_PUZZLE).with(Grid.index(0, 2), 5)
        assertEquals(0, BacktrackingSolver.countSolutions(broken))
        assertNull(BacktrackingSolver.solve(broken))
    }

    @Test
    fun `무작위 완성 보드는 규칙을 만족하고 시드마다 다르다`() {
        val a = BacktrackingSolver.randomSolution(Random(1))
        val b = BacktrackingSolver.randomSolution(Random(2))
        assertTrue(Rules.isSolved(a))
        assertTrue(Rules.isSolved(b))
        assertTrue(a != b)
        assertEquals(a, BacktrackingSolver.randomSolution(Random(1)))
    }
}
