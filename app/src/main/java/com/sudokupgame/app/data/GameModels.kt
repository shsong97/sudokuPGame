package com.sudokupgame.app.data

import com.sudokupgame.app.feature.game.CellState
import com.sudokupgame.engine.Difficulty

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
)

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
