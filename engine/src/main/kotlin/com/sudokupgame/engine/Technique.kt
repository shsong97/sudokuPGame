package com.sudokupgame.engine

/**
 * 사람이 쓰는 풀이 기법. 선언 순서 = 시도 순서(쉬운 것부터).
 * [weight]는 같은 난이도 안에서 퍼즐을 쉬운 순으로 정렬할 때 쓰는 점수.
 */
enum class Technique(val difficulty: Difficulty, val weight: Int) {
    NAKED_SINGLE(Difficulty.EASY, 1),
    HIDDEN_SINGLE(Difficulty.EASY, 2),
    NAKED_PAIR(Difficulty.MEDIUM, 10),
    HIDDEN_PAIR(Difficulty.MEDIUM, 15),
    POINTING(Difficulty.MEDIUM, 12),
    NAKED_TRIPLE(Difficulty.HARD, 25),
    HIDDEN_TRIPLE(Difficulty.HARD, 30),
    BOX_LINE_REDUCTION(Difficulty.HARD, 25),
    X_WING(Difficulty.HARD, 40),
    XY_WING(Difficulty.EXPERT, 60),
    SWORDFISH(Difficulty.EXPERT, 70),
}

data class Placement(val index: Int, val digit: Int)

data class Elimination(val index: Int, val digit: Int)

/**
 * 풀이 한 단계. 싱글 기법은 [placements]로 숫자를 확정하고,
 * 나머지 기법은 [eliminations]로 후보를 지운다. [focusCells]는 기법을 이루는 칸(힌트 강조용).
 */
data class Step(
    val technique: Technique,
    val placements: List<Placement> = emptyList(),
    val eliminations: List<Elimination> = emptyList(),
    val focusCells: List<Int> = emptyList(),
)
