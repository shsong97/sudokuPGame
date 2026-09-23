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
}
