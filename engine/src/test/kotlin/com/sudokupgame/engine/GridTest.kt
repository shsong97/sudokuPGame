package com.sudokupgame.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `유닛은 27개이고 각 칸은 유닛 3개에 속한다`() {
        assertEquals(27, Grid.units.size)
        for (i in 0 until Grid.CELLS) {
            assertEquals(3, Grid.units.count { i in it })
        }
        assertTrue(Grid.boxes.all { box -> box.all { Grid.box(it) == Grid.box(box[0]) } })
    }

    @Test
    fun `각 칸의 peer는 20개다`() {
        assertTrue(Grid.peers.all { it.size == 20 })
    }
}
