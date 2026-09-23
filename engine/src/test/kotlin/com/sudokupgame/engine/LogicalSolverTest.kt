package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class LogicalSolverTest {
    @Test
    fun `쉬운 퍼즐은 싱글 기법만으로 풀린다`() {
        val result = LogicalSolver.solve(Board.parse(Fixtures.EASY_PUZZLE))
        assertTrue(result.isSolved)
        assertEquals(Board.parse(Fixtures.EASY_SOLUTION), result.board)
        assertEquals(Difficulty.EASY, result.difficulty)
    }

    @Test
    fun `기법으로 풀리지 않는 퍼즐은 난이도가 없다`() {
        val result = LogicalSolver.solve(Board.parse(Fixtures.HARDEST_PUZZLE))
        assertFalse(result.isSolved)
        assertNull(result.difficulty)
    }

    @Test
    fun `nextStep은 가장 쉬운 기법을 먼저 준다`() {
        val step = LogicalSolver.nextStep(Board.parse(Fixtures.EASY_PUZZLE))
        assertNotNull(step)
        assertEquals(Difficulty.EASY, step!!.technique.difficulty)
        assertEquals(1, step.placements.size)
    }

    /**
     * 무작위 최소 퍼즐 여러 개에 대해 모든 풀이 단계가 정답과 어긋나지 않는지 확인한다.
     * (확정한 숫자는 정답과 같고, 지운 후보는 정답 숫자가 아니어야 한다)
     * 같은 시드로 모든 기법이 한 번 이상 쓰였는지도 확인한다.
     */
    @Test
    fun `모든 기법의 풀이 단계는 정답과 일치한다`() {
        val generator = PuzzleGenerator(Random(42))
        val used = mutableSetOf<Technique>()
        repeat(300) {
            val (puzzle, solution) = generator.minimalPuzzle()
            val result = LogicalSolver.solve(puzzle)
            for (step in result.steps) {
                used += step.technique
                for ((index, digit) in step.placements) {
                    assertEquals(solution[index], digit, "${step.technique} placed wrong digit\n$puzzle")
                }
                for ((index, digit) in step.eliminations) {
                    assertTrue(solution[index] != digit, "${step.technique} eliminated solution digit\n$puzzle")
                }
            }
        }
        assertEquals(Technique.entries.toSet(), used, "unused: ${Technique.entries - used}")
    }

    @Test
    fun `nextPlacement는 정답과 같은 숫자를 알려준다`() {
        val puzzle = Board.parse(Fixtures.EASY_PUZZLE)
        val hint = LogicalSolver.nextPlacement(puzzle)!!
        assertEquals(Board.parse(Fixtures.EASY_SOLUTION)[hint.placement.index], hint.placement.digit)
        assertEquals(0, puzzle[hint.placement.index])
    }

    @Test
    fun `어려운 퍼즐의 힌트는 필요한 기법까지 포함하고 항상 정답이다`() {
        val generator = PuzzleGenerator(Random(3))
        for (difficulty in listOf(Difficulty.HARD, Difficulty.EXPERT)) {
            val generated = generator.generate(difficulty)!!
            var board = generated.puzzle
            val seen = mutableSetOf<Technique>()
            while (!board.isFilled) {
                val hint = LogicalSolver.nextPlacement(board)!!
                assertEquals(generated.solution[hint.placement.index], hint.placement.digit)
                seen += hint.technique
                board = board.with(hint.placement.index, hint.placement.digit)
            }
            assertTrue(seen.any { it.difficulty == difficulty }, "$difficulty: $seen")
        }
    }

    @Test
    fun `기법으로 진행할 수 없으면 null`() {
        assertNull(LogicalSolver.nextPlacement(Board.parse(Fixtures.HARDEST_PUZZLE)))
    }
}
