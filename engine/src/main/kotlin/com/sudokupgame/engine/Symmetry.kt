package com.sudokupgame.engine

/** 회전·뒤집기(8가지)와 숫자 치환에 대해 같은 퍼즐인지 판별하기 위한 정규형. */
object Symmetry {

    private val transforms: List<(Int, Int) -> Int> = listOf(
        { r, c -> Grid.index(r, c) },
        { r, c -> Grid.index(c, 8 - r) },
        { r, c -> Grid.index(8 - r, 8 - c) },
        { r, c -> Grid.index(8 - c, r) },
        { r, c -> Grid.index(r, 8 - c) },
        { r, c -> Grid.index(8 - r, c) },
        { r, c -> Grid.index(c, r) },
        { r, c -> Grid.index(8 - c, 8 - r) },
    )

    /** 두 퍼즐이 같은 정규형이면 회전·뒤집기·숫자 치환으로 서로 바뀌는 같은 퍼즐이다. */
    fun canonical(board: Board): String = transforms.minOf { transform ->
        val moved = IntArray(Grid.CELLS)
        for (i in 0 until Grid.CELLS) moved[transform(Grid.row(i), Grid.col(i))] = board[i]
        relabel(moved)
    }

    /** 처음 등장하는 순서대로 숫자를 1, 2, 3...으로 바꾼다. */
    private fun relabel(cells: IntArray): String {
        val mapping = IntArray(10)
        var next = 1
        val sb = StringBuilder(Grid.CELLS)
        for (v in cells) {
            if (v == 0) {
                sb.append('0')
                continue
            }
            if (mapping[v] == 0) mapping[v] = next++
            sb.append(mapping[v])
        }
        return sb.toString()
    }
}
