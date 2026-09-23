package com.sudokupgame.engine

/** 사람의 풀이 기법만으로 푸는 솔버. 난이도 평가와 힌트에 쓴다. */
object LogicalSolver {

    fun solve(board: Board): SolveResult {
        val grid = CandidateGrid(board)
        val steps = mutableListOf<Step>()
        while (grid.values.any { it == 0 } && !grid.hasContradiction()) {
            val step = findStep(grid) ?: break
            grid.apply(step)
            steps += step
        }
        return SolveResult(steps, grid.toBoard())
    }

    /** 현재 보드에서 적용할 수 있는 가장 쉬운 다음 단계. 없으면 null. */
    fun nextStep(board: Board): Step? {
        val grid = CandidateGrid(board)
        if (grid.hasContradiction()) return null
        return findStep(grid)
    }

    /** 기법만으로 풀리면 그 난이도, 아니면 null. */
    fun grade(board: Board): Difficulty? = solve(board).difficulty

    private fun findStep(grid: CandidateGrid): Step? =
        Techniques.finders.firstNotNullOfOrNull { it(grid) }
}

class SolveResult(val steps: List<Step>, val board: Board) {
    val isSolved: Boolean get() = board.isFilled

    val hardestTechnique: Technique? get() = steps.maxOfOrNull { it.technique }

    /** 끝까지 풀었을 때만 난이도가 있다. */
    val difficulty: Difficulty?
        get() = if (isSolved) hardestTechnique?.difficulty ?: Difficulty.EASY else null

    val score: Int get() = steps.sumOf { it.technique.weight }
}
