package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GridTest {
    @Test
    fun `index와 row, col 변환이 서로 일치한다`() {
        for (i in 0 until Grid.CELLS) {
            assertEquals(i, Grid.index(Grid.row(i), Grid.col(i)))
        }
    }

    @Test
    fun `box 번호는 좌상단 0부터 우하단 8까지다`() {
        assertEquals(0, Grid.box(Grid.index(0, 0)))
        assertEquals(4, Grid.box(Grid.index(4, 4)))
        assertEquals(8, Grid.box(Grid.index(8, 8)))
        assertEquals(2, Grid.box(Grid.index(1, 7)))
    }
}
