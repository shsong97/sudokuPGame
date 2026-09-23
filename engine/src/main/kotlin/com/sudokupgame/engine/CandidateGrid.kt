package com.sudokupgame.engine

/** 값과 후보 마스크를 함께 들고 있는 풀이 상태. */
internal class CandidateGrid(board: Board) {
    val values: IntArray = board.toIntArray()
    val candidates = IntArray(Grid.CELLS)

    init {
        for (i in 0 until Grid.CELLS) {
            if (values[i] != 0) continue
            var used = 0
            for (p in Grid.peers[i]) {
                if (values[p] != 0) used = used or Digits.bit(values[p])
            }
            candidates[i] = Digits.ALL and used.inv()
        }
    }

    fun isEmpty(i: Int): Boolean = values[i] == 0

    fun has(i: Int, digit: Int): Boolean = values[i] == 0 && Digits.contains(candidates[i], digit)

    /** 빈칸인데 후보가 하나도 없으면 모순. */
    fun hasContradiction(): Boolean = (0 until Grid.CELLS).any { values[it] == 0 && candidates[it] == 0 }

    fun apply(step: Step) {
        for ((index, digit) in step.placements) {
            values[index] = digit
            candidates[index] = 0
            val clear = Digits.bit(digit).inv()
            for (p in Grid.peers[index]) candidates[p] = candidates[p] and clear
        }
        for ((index, digit) in step.eliminations) {
            candidates[index] = candidates[index] and Digits.bit(digit).inv()
        }
    }

    fun toBoard(): Board = Board.of(values)
}
