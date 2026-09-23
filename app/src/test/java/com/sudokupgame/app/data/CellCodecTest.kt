package com.sudokupgame.app.data

import com.sudokupgame.app.feature.game.CellState
import com.sudokupgame.engine.Grid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CellCodecTest {
    @Test
    fun `칸 상태를 저장하고 그대로 복원한다`() {
        val cells = List(Grid.CELLS) { i ->
            when (i % 4) {
                0 -> CellState(value = 5, isGiven = true)
                1 -> CellState(value = 3, isError = true)
                2 -> CellState(notes = setOf(1, 5, 9))
                else -> CellState(notes = (1..9).toSet())
            }
        }
        val encoded = CellCodec.encode(cells)
        assertEquals(Grid.CELLS * 5, encoded.length)
        assertEquals(cells, CellCodec.decode(encoded))
    }
}
