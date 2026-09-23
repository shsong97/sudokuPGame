package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class SymmetryTest {
    private val puzzle = Board.parse(Fixtures.EASY_PUZZLE)

    @Test
    fun `회전하고 숫자를 바꾼 퍼즐은 같은 정규형이다`() {
        val swap = intArrayOf(0, 9, 8, 7, 6, 5, 4, 3, 2, 1)
        val rotated = IntArray(Grid.CELLS)
        for (i in 0 until Grid.CELLS) {
            rotated[Grid.index(Grid.col(i), 8 - Grid.row(i))] = swap[puzzle[i]]
        }
        assertEquals(Symmetry.canonical(puzzle), Symmetry.canonical(Board.of(rotated)))
    }

    @Test
    fun `다른 퍼즐은 다른 정규형이다`() {
        assertNotEquals(Symmetry.canonical(puzzle), Symmetry.canonical(Board.parse(Fixtures.HARDEST_PUZZLE)))
    }
}
