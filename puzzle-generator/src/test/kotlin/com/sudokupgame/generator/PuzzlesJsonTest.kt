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
import java.security.MessageDigest

/** 앱에 내장된 puzzles.json 전체 검증. CI에서 매번 실행한다. */
class PuzzlesJsonTest {
    private val file: PuzzleFile = PuzzleJson.decodeFromString(
        PuzzleFile.serializer(),
        File(System.getProperty("puzzlesJson")).readText(),
    )

    @Test
    fun `난이도별 100개씩 총 400개이고 id가 유일하다`() {
        assertEquals(1, file.version)
        assertEquals(400, file.puzzles.size)
        assertEquals(file.puzzles.size, file.puzzles.map { it.id }.toSet().size)
        for (difficulty in Difficulty.entries) {
            val ids = file.puzzles.filter { it.difficulty == difficulty.name }.map { it.id }
            assertEquals((1..100).map { idPrefix(difficulty) + it.toString().padStart(3, '0') }, ids)
        }
    }

    /**
     * 1.0.0에 들어간 난이도별 첫 25개(E001~X025)는 id·내용이 절대 바뀌면 안 된다.
     * 기기에 저장된 기록과 진행 중인 게임이 id로 이 퍼즐들을 가리키기 때문이다.
     */
    @Test
    fun `처음 출시한 100개 퍼즐은 바뀌지 않는다`() {
        val firstRelease = Difficulty.entries.flatMap { difficulty ->
            file.puzzles.filter { it.difficulty == difficulty.name }.take(25)
        }
        val fingerprint = MessageDigest.getInstance("SHA-256")
            .digest(firstRelease.joinToString("") { it.id + it.givens + it.solution }.toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertEquals("87bcd3589b732d30d9e5c4abc9a3e7e4b82c8dbb63b8a13a97988ba9665726cb", fingerprint)
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
