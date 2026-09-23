package com.sudokupgame.generator

import com.sudokupgame.engine.BacktrackingSolver
import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.LogicalSolver
import com.sudokupgame.engine.PuzzleGenerator
import com.sudokupgame.engine.Rules
import com.sudokupgame.engine.Symmetry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/** 앱에 내장된 puzzles.json 전체 검증. CI에서 매번 실행한다. */
class PuzzlesJsonTest {
    private val file: PuzzleFile = PuzzleJson.decodeFromString(
        PuzzleFile.serializer(),
        File(System.getProperty("puzzlesJson")).readText(),
    )

    @Test
    fun `난이도별 25개씩 총 100개이고 id가 유일하다`() {
        assertEquals(1, file.version)
        assertEquals(100, file.puzzles.size)
        assertEquals(file.puzzles.size, file.puzzles.map { it.id }.toSet().size)
        for (difficulty in Difficulty.entries) {
            val ids = file.puzzles.filter { it.difficulty == difficulty.name }.map { it.id }
            assertEquals((1..25).map { idPrefix(difficulty) + it.toString().padStart(3, '0') }, ids)
        }
    }

    @Test
    fun `모든 퍼즐은 유일해이고 정답과 난이도가 맞다`() {
        for (entry in file.puzzles) {
            val puzzle = Board.parse(entry.givens)
            val solution = Board.parse(entry.solution)
            val difficulty = Difficulty.valueOf(entry.difficulty)
            assertTrue(Rules.isSolutionOf(solution, puzzle), "${entry.id}: 정답 불일치")
            assertTrue(BacktrackingSolver.hasUniqueSolution(puzzle), "${entry.id}: 유일해 아님")
            assertEquals(difficulty, LogicalSolver.grade(puzzle), "${entry.id}: 난이도 불일치")
            assertTrue(puzzle.filledCount in PuzzleGenerator.givenRange(difficulty), "${entry.id}: 힌트 수 범위 밖")
        }
    }

    @Test
    fun `회전·숫자 치환을 포함해 중복 퍼즐이 없다`() {
        val canonical = file.puzzles.map { Symmetry.canonical(Board.parse(it.givens)) }
        assertEquals(canonical.size, canonical.toSet().size)
    }
}
