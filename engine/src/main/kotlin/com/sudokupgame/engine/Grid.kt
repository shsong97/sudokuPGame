package com.sudokupgame.engine

/** 스도쿠 격자 상수와 인덱스 변환. 칸 인덱스는 row * 9 + col. */
object Grid {
    const val SIZE = 9
    const val BOX = 3
    const val CELLS = SIZE * SIZE

    fun index(row: Int, col: Int): Int = row * SIZE + col

    fun row(index: Int): Int = index / SIZE

    fun col(index: Int): Int = index % SIZE

    fun box(index: Int): Int = (row(index) / BOX) * BOX + col(index) / BOX

    val rows: List<IntArray> = List(SIZE) { r -> IntArray(SIZE) { c -> index(r, c) } }

    val cols: List<IntArray> = List(SIZE) { c -> IntArray(SIZE) { r -> index(r, c) } }

    val boxes: List<IntArray> = List(SIZE) { b ->
        IntArray(SIZE) { k -> index((b / BOX) * BOX + k / BOX, (b % BOX) * BOX + k % BOX) }
    }

    /** 27개 유닛: 행 9개, 열 9개, 박스 9개 순서. */
    val units: List<IntArray> = rows + cols + boxes

    /** 각 칸과 같은 행·열·박스에 있는 다른 20칸. */
    val peers: List<IntArray> = List(CELLS) { i ->
        (0 until CELLS).filter { j ->
            j != i && (row(j) == row(i) || col(j) == col(i) || box(j) == box(i))
        }.toIntArray()
    }

    fun arePeers(a: Int, b: Int): Boolean =
        a != b && (row(a) == row(b) || col(a) == col(b) || box(a) == box(b))
}
