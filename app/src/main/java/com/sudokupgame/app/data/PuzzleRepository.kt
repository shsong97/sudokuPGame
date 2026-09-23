package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty

interface PuzzleRepository {
    suspend fun puzzles(): List<Puzzle>

    suspend fun puzzle(id: String): Puzzle? = puzzles().find { it.id == id }

    suspend fun firstPuzzleId(difficulty: Difficulty): String? =
        puzzles().firstOrNull { it.difficulty == difficulty }?.id

    /** 같은 난이도의 다음 퍼즐. 마지막이면 null. */
    suspend fun nextPuzzleId(id: String): String? {
        val all = puzzles()
        val current = all.find { it.id == id } ?: return null
        val sameDifficulty = all.filter { it.difficulty == current.difficulty }
        return sameDifficulty.getOrNull(sameDifficulty.indexOf(current) + 1)?.id
    }
}
