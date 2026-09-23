package com.sudokupgame.engine

/** 스도쿠 규칙 검사. */
object Rules {
    /** 다른 칸과 충돌 없이 [digit]을 [index]에 둘 수 있는지. 해당 칸 자신의 값은 무시한다. */
    fun canPlace(board: Board, index: Int, digit: Int): Boolean =
        Grid.peers[index].none { board[it] == digit }

    /** 채워진 칸끼리 충돌이 없는지. */
    fun isConsistent(board: Board): Boolean =
        (0 until Grid.CELLS).all { i -> board[i] == 0 || canPlace(board, i, board[i]) }

    fun isSolved(board: Board): Boolean = board.isFilled && isConsistent(board)

    /** [solution]이 완성된 정답이고 [puzzle]의 주어진 숫자와 모두 일치하는지. */
    fun isSolutionOf(solution: Board, puzzle: Board): Boolean =
        isSolved(solution) && (0 until Grid.CELLS).all { puzzle[it] == 0 || puzzle[it] == solution[it] }
}
