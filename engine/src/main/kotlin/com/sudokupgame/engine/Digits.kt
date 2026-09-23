package com.sudokupgame.engine

/** 후보 숫자 비트마스크 도우미. 숫자 d(1~9)는 비트 (d - 1)에 대응한다. */
internal object Digits {
    const val ALL = 0x1FF

    fun bit(digit: Int): Int = 1 shl (digit - 1)

    fun count(mask: Int): Int = Integer.bitCount(mask)

    fun contains(mask: Int, digit: Int): Boolean = mask and bit(digit) != 0

    /** 비트가 하나만 켜진 마스크의 숫자. */
    fun single(mask: Int): Int = Integer.numberOfTrailingZeros(mask) + 1

    fun toList(mask: Int): List<Int> = (1..9).filter { contains(mask, it) }
}
