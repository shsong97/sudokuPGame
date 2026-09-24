package com.sudokupgame.app.data

import com.sudokupgame.app.feature.game.CellState
import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.Grid

/** 진행 중인 게임 (한 번에 1개). 실행 취소 기록은 저장하지 않는다. */
data class SavedGame(
    val puzzleId: String,
    val difficulty: Difficulty,
    val cells: List<CellState>,
    val mistakes: Int,
    val elapsedSeconds: Long,
    val notesMode: Boolean,
    val hintsUsed: Int = 0,
    val mode: GameMode = GameMode.NUMBER,
) {
    /** 저장된 칸의 주어진 숫자가 [puzzle]과 같은지. 퍼즐 데이터가 바뀐 경우를 걸러낸다. */
    fun matches(puzzle: Puzzle): Boolean =
        cells.size == Grid.CELLS && cells.indices.all { i ->
            val given = puzzle.givens[i]
            cells[i].isGiven == (given != 0) && (!cells[i].isGiven || cells[i].value == given)
        }
}

/** 퍼즐별 누적 기록. 끝낸 게임(클리어·게임 오버)만 센다. */
data class PuzzleRecord(
    val puzzleId: String,
    val difficulty: Difficulty,
    val wins: Int,
    val losses: Int,
    val bestTimeSeconds: Long?,
    val totalWinTimeSeconds: Long,
    val hintsUsed: Int = 0,
) {
    val isCleared: Boolean get() = wins > 0
}
