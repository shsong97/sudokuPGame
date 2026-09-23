package com.sudokupgame.engine

/** 불변 9×9 보드. 각 칸은 0(빈칸) 또는 1~9. */
class Board private constructor(private val cells: IntArray) {

    operator fun get(index: Int): Int = cells[index]

    operator fun get(row: Int, col: Int): Int = cells[Grid.index(row, col)]

    val filledCount: Int get() = cells.count { it != 0 }

    val isFilled: Boolean get() = cells.none { it == 0 }

    fun with(index: Int, value: Int): Board {
        require(value in 0..9) { "value must be 0..9: $value" }
        return Board(cells.copyOf().also { it[index] = value })
    }

    fun toIntArray(): IntArray = cells.copyOf()

    /** 81자 문자열 (빈칸은 '0'). puzzles.json 저장 형식. */
    fun toCompactString(): String = cells.joinToString("")

    override fun equals(other: Any?): Boolean = other is Board && cells.contentEquals(other.cells)

    override fun hashCode(): Int = cells.contentHashCode()

    override fun toString(): String = Grid.rows.joinToString("\n") { row ->
        row.joinToString(" ") { if (cells[it] == 0) "." else cells[it].toString() }
    }

    companion object {
        val EMPTY = Board(IntArray(Grid.CELLS))

        fun of(values: IntArray): Board {
            require(values.size == Grid.CELLS) { "board must have ${Grid.CELLS} cells: ${values.size}" }
            require(values.all { it in 0..9 }) { "cell values must be 0..9" }
            return Board(values.copyOf())
        }

        /** 81자 문자열을 읽는다. 빈칸은 '0' 또는 '.'. */
        fun parse(text: String): Board {
            require(text.length == Grid.CELLS) { "board string must have ${Grid.CELLS} chars: ${text.length}" }
            return Board(
                IntArray(Grid.CELLS) { i ->
                    when (val ch = text[i]) {
                        '.', '0' -> 0
                        in '1'..'9' -> ch - '0'
                        else -> throw IllegalArgumentException("invalid char '$ch' at $i")
                    }
                },
            )
        }
    }
}
