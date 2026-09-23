package com.sudokupgame.engine

import kotlin.random.Random

/** 비트마스크 백트래킹 솔버. 해의 개수 세기(유일해 검사)와 완성 보드 생성에 쓴다. */
object BacktrackingSolver {

    /** 해의 개수를 [limit]개까지만 센다. 충돌이 있는 보드는 0. */
    fun countSolutions(board: Board, limit: Int = 2): Int {
        val search = Search.from(board) ?: return 0
        return search.count(limit)
    }

    fun hasUniqueSolution(board: Board): Boolean = countSolutions(board, limit = 2) == 1

    fun solve(board: Board): Board? {
        val search = Search.from(board) ?: return null
        return if (search.findFirst(random = null)) Board.of(search.cells) else null
    }

    /** 무작위 완성 보드. */
    fun randomSolution(random: Random): Board {
        val search = checkNotNull(Search.from(Board.EMPTY))
        check(search.findFirst(random))
        return Board.of(search.cells)
    }

    private class Search(val cells: IntArray) {
        private val rowUsed = IntArray(Grid.SIZE)
        private val colUsed = IntArray(Grid.SIZE)
        private val boxUsed = IntArray(Grid.SIZE)

        fun candidates(i: Int): Int =
            Digits.ALL and (rowUsed[Grid.row(i)] or colUsed[Grid.col(i)] or boxUsed[Grid.box(i)]).inv()

        fun place(i: Int, digit: Int) {
            val bit = Digits.bit(digit)
            cells[i] = digit
            rowUsed[Grid.row(i)] = rowUsed[Grid.row(i)] or bit
            colUsed[Grid.col(i)] = colUsed[Grid.col(i)] or bit
            boxUsed[Grid.box(i)] = boxUsed[Grid.box(i)] or bit
        }

        fun unplace(i: Int, digit: Int) {
            val mask = Digits.bit(digit).inv()
            cells[i] = 0
            rowUsed[Grid.row(i)] = rowUsed[Grid.row(i)] and mask
            colUsed[Grid.col(i)] = colUsed[Grid.col(i)] and mask
            boxUsed[Grid.box(i)] = boxUsed[Grid.box(i)] and mask
        }

        /** 후보가 가장 적은 빈칸. 빈칸이 없으면 -1. */
        fun pickCell(): Int {
            var best = -1
            var bestCount = 10
            for (i in 0 until Grid.CELLS) {
                if (cells[i] != 0) continue
                val count = Digits.count(candidates(i))
                if (count < bestCount) {
                    best = i
                    bestCount = count
                    if (count <= 1) break
                }
            }
            return best
        }

        fun count(limit: Int): Int {
            val i = pickCell()
            if (i < 0) return 1
            var mask = candidates(i)
            var found = 0
            while (mask != 0 && found < limit) {
                val bit = mask and -mask
                mask = mask xor bit
                val digit = Digits.single(bit)
                place(i, digit)
                found += count(limit - found)
                unplace(i, digit)
            }
            return found
        }

        fun findFirst(random: Random?): Boolean {
            val i = pickCell()
            if (i < 0) return true
            val digits = Digits.toList(candidates(i)).let { if (random != null) it.shuffled(random) else it }
            for (digit in digits) {
                place(i, digit)
                if (findFirst(random)) return true
                unplace(i, digit)
            }
            return false
        }

        companion object {
            fun from(board: Board): Search? {
                if (!Rules.isConsistent(board)) return null
                val search = Search(IntArray(Grid.CELLS))
                for (i in 0 until Grid.CELLS) {
                    if (board[i] != 0) search.place(i, board[i])
                }
                return search
            }
        }
    }
}
