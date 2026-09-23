package com.sudokupgame.app.data

import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty

data class Puzzle(
    val id: String,
    val difficulty: Difficulty,
    val givens: Board,
    val solution: Board,
)
