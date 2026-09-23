package com.sudokupgame.engine

import kotlin.random.Random

data class GeneratedPuzzle(
    val puzzle: Board,
    val solution: Board,
    val difficulty: Difficulty,
    val score: Int,
)

/**
 * 목표 난이도의 유일해 퍼즐 생성기.
 * 무작위 완성 보드에서 180° 회전 대칭 쌍으로 칸을 비우며, 매번 유일해이고
 * 목표 난이도를 넘지 않을 때만 비운 상태를 유지한다.
 */
class PuzzleGenerator(private val random: Random) {

    fun generate(target: Difficulty, maxAttempts: Int = 100_000): GeneratedPuzzle? {
        repeat(maxAttempts) {
            tryGenerate(target)?.let { return it }
        }
        return null
    }

    private fun tryGenerate(target: Difficulty): GeneratedPuzzle? {
        val solution = BacktrackingSolver.randomSolution(random)
        val range = givenRange(target)
        val cells = solution.toIntArray()
        var given = Grid.CELLS

        for (pair in SYMMETRIC_PAIRS.shuffled(random)) {
            if (given - pair.size < range.first) continue
            pair.forEach { cells[it] = 0 }
            val candidate = Board.of(cells)
            val grade = if (BacktrackingSolver.hasUniqueSolution(candidate)) LogicalSolver.grade(candidate) else null
            if (grade != null && grade <= target) {
                given -= pair.size
            } else {
                pair.forEach { cells[it] = solution[it] }
            }
        }

        val puzzle = Board.of(cells)
        val result = LogicalSolver.solve(puzzle)
        if (result.difficulty != target || given !in range) return null
        return GeneratedPuzzle(puzzle, solution, target, result.score)
    }

    /** 유일해만 유지하며 더 비울 칸이 없을 때까지 비운 퍼즐 (난이도 무관). 테스트용. */
    internal fun minimalPuzzle(): Pair<Board, Board> {
        val solution = BacktrackingSolver.randomSolution(random)
        val cells = solution.toIntArray()
        for (i in (0 until Grid.CELLS).shuffled(random)) {
            cells[i] = 0
            if (!BacktrackingSolver.hasUniqueSolution(Board.of(cells))) cells[i] = solution[i]
        }
        return Board.of(cells) to solution
    }

    companion object {
        /** 난이도별 주어진 숫자 개수 범위. */
        fun givenRange(difficulty: Difficulty): IntRange = when (difficulty) {
            Difficulty.EASY -> 36..45
            Difficulty.MEDIUM -> 30..35
            Difficulty.HARD -> 26..30
            Difficulty.EXPERT -> 22..26
        }

        /** 180° 회전 대칭 칸 쌍. 가운데 칸(40)은 혼자. */
        private val SYMMETRIC_PAIRS: List<List<Int>> =
            (0..Grid.CELLS / 2).map { i -> listOf(i, Grid.CELLS - 1 - i).distinct() }
    }
}
