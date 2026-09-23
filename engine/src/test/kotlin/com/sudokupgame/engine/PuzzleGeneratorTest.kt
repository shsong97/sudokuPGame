package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random

class PuzzleGeneratorTest {
    @ParameterizedTest
    @EnumSource(Difficulty::class)
    fun `목표 난이도의 유일해 퍼즐을 만든다`(difficulty: Difficulty) {
        val generated = PuzzleGenerator(Random(7)).generate(difficulty)
        assertNotNull(generated)
        val (puzzle, solution) = generated!!
        assertTrue(BacktrackingSolver.hasUniqueSolution(puzzle))
        assertTrue(Rules.isSolutionOf(solution, puzzle))
        assertEquals(difficulty, LogicalSolver.grade(puzzle))
        assertTrue(puzzle.filledCount in PuzzleGenerator.givenRange(difficulty))
    }
}
