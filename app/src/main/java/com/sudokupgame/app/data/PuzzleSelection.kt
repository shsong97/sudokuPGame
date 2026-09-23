package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty

/** "새 게임"으로 시작할 퍼즐: 해당 난이도에서 아직 클리어하지 않은 첫 번째. 모두 클리어했으면 첫 번째. */
fun nextPuzzleToPlay(puzzles: List<Puzzle>, records: List<PuzzleRecord>, difficulty: Difficulty): String? {
    val cleared = records.filter { it.isCleared }.mapTo(mutableSetOf()) { it.puzzleId }
    val candidates = puzzles.filter { it.difficulty == difficulty }
    return (candidates.firstOrNull { it.id !in cleared } ?: candidates.firstOrNull())?.id
}
