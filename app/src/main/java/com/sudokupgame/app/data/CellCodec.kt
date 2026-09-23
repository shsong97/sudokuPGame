package com.sudokupgame.app.data

import com.sudokupgame.app.feature.game.CellState
import com.sudokupgame.engine.Grid

/**
 * 81칸 상태를 DB 저장용 문자열로 바꾼다. 칸마다 5글자:
 * 값(0~9) 1자리 + 플래그(0 보통, 1 주어진 숫자, 2 오답) 1자리 + 메모 비트마스크 16진수 3자리.
 */
internal object CellCodec {
    private const val CELL_LENGTH = 5

    fun encode(cells: List<CellState>): String {
        require(cells.size == Grid.CELLS)
        return buildString(Grid.CELLS * CELL_LENGTH) {
            for (cell in cells) {
                append(cell.value)
                append(
                    when {
                        cell.isGiven -> '1'
                        cell.isError -> '2'
                        else -> '0'
                    },
                )
                val mask = cell.notes.fold(0) { acc, d -> acc or (1 shl (d - 1)) }
                append(mask.toString(16).padStart(3, '0'))
            }
        }
    }

    fun decode(text: String): List<CellState> {
        require(text.length == Grid.CELLS * CELL_LENGTH) { "invalid cells length: ${text.length}" }
        return List(Grid.CELLS) { i ->
            val chunk = text.substring(i * CELL_LENGTH, (i + 1) * CELL_LENGTH)
            val mask = chunk.substring(2).toInt(16)
            CellState(
                value = chunk[0].digitToInt(),
                isGiven = chunk[1] == '1',
                isError = chunk[1] == '2',
                notes = (1..9).filterTo(mutableSetOf()) { mask and (1 shl (it - 1)) != 0 },
            )
        }
    }
}
