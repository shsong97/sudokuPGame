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

    /**
     * 다음으로 숫자를 확정할 수 있는 칸과, 거기까지 필요한 풀이 단계들.
     * 후보만 지우는 단계(페어·X-Wing 등)가 먼저 필요하면 그 단계들도 함께 돌려준다.
     * 기법으로 더 진행할 수 없으면 null.
     */
    fun nextPlacement(board: Board): PlacementHint? {
        val grid = CandidateGrid(board)
        val steps = mutableListOf<Step>()
        while (!grid.hasContradiction()) {
            val step = findStep(grid) ?: return null
            steps += step
            val placement = step.placements.firstOrNull()
            if (placement != null) return PlacementHint(placement, steps)
            grid.apply(step)
        }
        return null
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

/** [placement]를 찾기까지의 단계들. 마지막 단계가 숫자를 확정한다. */
class PlacementHint(val placement: Placement, val steps: List<Step>) {
    /** 설명에 쓸 대표 기법: 과정 중 가장 어려운 기법. */
    val technique: Technique get() = steps.maxOf { it.technique }

    /** 대표 기법을 이루는 칸들. */
    val focusCells: List<Int> get() = steps.last { it.technique == technique }.focusCells
}
