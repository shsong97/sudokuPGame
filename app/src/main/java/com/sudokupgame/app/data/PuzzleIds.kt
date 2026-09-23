package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty

/** puzzles.json 의 id 규칙: 난이도 접두어 + 3자리 번호 (E001, M001, H001, X001). */
object PuzzleIds {
    fun prefix(difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EASY -> "E"
        Difficulty.MEDIUM -> "M"
        Difficulty.HARD -> "H"
        Difficulty.EXPERT -> "X"
    }

    fun first(difficulty: Difficulty): String = prefix(difficulty) + "001"
}
