package com.sudokupgame.engine

/** 각 기법의 탐색기. 적용 가능한 첫 번째 단계를 돌려주고, 없으면 null. */
internal object Techniques {

    val finders: List<(CandidateGrid) -> Step?> = listOf(
        ::nakedSingle,
        ::hiddenSingle,
        { g -> nakedSubset(g, 2, Technique.NAKED_PAIR) },
        { g -> hiddenSubset(g, 2, Technique.HIDDEN_PAIR) },
        ::pointing,
        { g -> nakedSubset(g, 3, Technique.NAKED_TRIPLE) },
        { g -> hiddenSubset(g, 3, Technique.HIDDEN_TRIPLE) },
        ::boxLineReduction,
        { g -> fish(g, 2, Technique.X_WING) },
        ::xyWing,
        { g -> fish(g, 3, Technique.SWORDFISH) },
    )

    fun nakedSingle(g: CandidateGrid): Step? {
        for (i in 0 until Grid.CELLS) {
            if (g.isEmpty(i) && Digits.count(g.candidates[i]) == 1) {
                return Step(
                    Technique.NAKED_SINGLE,
                    placements = listOf(Placement(i, Digits.single(g.candidates[i]))),
                    focusCells = listOf(i),
                )
            }
        }
        return null
    }

    fun hiddenSingle(g: CandidateGrid): Step? {
        for (unit in Grid.units) {
            for (digit in 1..9) {
                val cells = unit.filter { g.has(it, digit) }
                if (cells.size == 1) {
                    return Step(
                        Technique.HIDDEN_SINGLE,
                        placements = listOf(Placement(cells[0], digit)),
                        focusCells = unit.toList(),
                    )
                }
            }
        }
        return null
    }

    /** 유닛 안의 n칸이 합쳐서 n개의 후보만 가지면, 그 숫자들을 유닛의 다른 칸에서 지운다. */
    fun nakedSubset(g: CandidateGrid, size: Int, technique: Technique): Step? {
        for (unit in Grid.units) {
            val cells = unit.filter { g.isEmpty(it) && Digits.count(g.candidates[it]) in 2..size }
            for (combo in combinations(cells, size)) {
                val union = combo.fold(0) { acc, i -> acc or g.candidates[i] }
                if (Digits.count(union) != size) continue
                val eliminations = unit
                    .filter { g.isEmpty(it) && it !in combo }
                    .flatMap { i -> Digits.toList(g.candidates[i] and union).map { Elimination(i, it) } }
                if (eliminations.isNotEmpty()) {
                    return Step(technique, eliminations = eliminations, focusCells = combo)
                }
            }
        }
        return null
    }

    /** 유닛 안에서 n개의 숫자가 같은 n칸에만 들어갈 수 있으면, 그 칸의 다른 후보를 지운다. */
    fun hiddenSubset(g: CandidateGrid, size: Int, technique: Technique): Step? {
        for (unit in Grid.units) {
            val positions = (1..9).associateWith { d -> unit.filter { g.has(it, d) } }
                .filterValues { it.size in 1..size }
            for (digits in combinations(positions.keys.toList(), size)) {
                val cells = digits.flatMap { positions.getValue(it) }.distinct()
                if (cells.size != size) continue
                val keep = digits.fold(0) { acc, d -> acc or Digits.bit(d) }
                val eliminations = cells.flatMap { i ->
                    Digits.toList(g.candidates[i] and keep.inv()).map { Elimination(i, it) }
                }
                if (eliminations.isNotEmpty()) {
                    return Step(technique, eliminations = eliminations, focusCells = cells)
                }
            }
        }
        return null
    }

    /** 박스 안에서 숫자 후보가 한 행(열)에만 있으면, 그 행(열)의 박스 밖 칸에서 지운다. */
    fun pointing(g: CandidateGrid): Step? {
        for ((b, box) in Grid.boxes.withIndex()) {
            for (digit in 1..9) {
                val cells = box.filter { g.has(it, digit) }
                if (cells.size < 2) continue
                for (line in lineContaining(cells)) {
                    val eliminations = line
                        .filter { Grid.box(it) != b && g.has(it, digit) }
                        .map { Elimination(it, digit) }
                    if (eliminations.isNotEmpty()) {
                        return Step(Technique.POINTING, eliminations = eliminations, focusCells = cells)
                    }
                }
            }
        }
        return null
    }

    /** 행(열) 안에서 숫자 후보가 한 박스에만 있으면, 그 박스의 행(열) 밖 칸에서 지운다. */
    fun boxLineReduction(g: CandidateGrid): Step? {
        for (line in Grid.rows + Grid.cols) {
            for (digit in 1..9) {
                val cells = line.filter { g.has(it, digit) }
                if (cells.size < 2) continue
                val box = Grid.box(cells[0])
                if (cells.any { Grid.box(it) != box }) continue
                val eliminations = Grid.boxes[box]
                    .filter { it !in line && g.has(it, digit) }
                    .map { Elimination(it, digit) }
                if (eliminations.isNotEmpty()) {
                    return Step(Technique.BOX_LINE_REDUCTION, eliminations = eliminations, focusCells = cells)
                }
            }
        }
        return null
    }

    /**
     * X-Wing(n=2) / Swordfish(n=3). n개의 기준 행에서 숫자 후보가 합쳐서 n개의 열에만 있으면,
     * 그 열들의 다른 행에서 지운다. 행과 열을 바꿔서도 검사한다.
     */
    fun fish(g: CandidateGrid, size: Int, technique: Technique): Step? {
        for (digit in 1..9) {
            for ((baseLines, coverLines, coverOf) in listOf(
                Triple(Grid.rows, Grid.cols, Grid::col),
                Triple(Grid.cols, Grid.rows, Grid::row),
            )) {
                val bases = baseLines.indices.mapNotNull { line ->
                    val cells = baseLines[line].filter { g.has(it, digit) }
                    if (cells.size in 2..size) line to cells else null
                }
                for (combo in combinations(bases, size)) {
                    val covers = combo.flatMap { (_, cells) -> cells.map(coverOf) }.toSet()
                    if (covers.size != size) continue
                    val baseCells = combo.flatMap { it.second }
                    val eliminations = covers
                        .flatMap { coverLines[it].toList() }
                        .filter { it !in baseCells && g.has(it, digit) }
                        .map { Elimination(it, digit) }
                    if (eliminations.isNotEmpty()) {
                        return Step(technique, eliminations = eliminations, focusCells = baseCells)
                    }
                }
            }
        }
        return null
    }

    /**
     * XY-Wing. 후보가 {x,y}인 중심 칸과, 중심을 보는 {x,z}·{y,z} 두 날개 칸이 있으면
     * 두 날개를 모두 보는 칸에서 z를 지운다.
     */
    fun xyWing(g: CandidateGrid): Step? {
        for (pivot in 0 until Grid.CELLS) {
            val pc = g.candidates[pivot]
            if (!g.isEmpty(pivot) || Digits.count(pc) != 2) continue
            val wings = Grid.peers[pivot].filter {
                g.isEmpty(it) && Digits.count(g.candidates[it]) == 2 && Digits.count(g.candidates[it] and pc) == 1
            }
            for ((a, b) in combinations(wings, 2).map { it[0] to it[1] }) {
                val ca = g.candidates[a]
                val cb = g.candidates[b]
                if (ca and pc == cb and pc) continue
                val z = ca and pc.inv()
                if (z != cb and pc.inv()) continue
                val digit = Digits.single(z)
                val eliminations = Grid.peers[a]
                    .filter { it != b && it != pivot && Grid.arePeers(it, b) && g.has(it, digit) }
                    .map { Elimination(it, digit) }
                if (eliminations.isNotEmpty()) {
                    return Step(Technique.XY_WING, eliminations = eliminations, focusCells = listOf(pivot, a, b))
                }
            }
        }
        return null
    }

    /** 박스 안의 칸들이 모두 같은 행 또는 같은 열에 있을 때 그 줄. */
    private fun lineContaining(cells: List<Int>): List<IntArray> = buildList {
        if (cells.all { Grid.row(it) == Grid.row(cells[0]) }) add(Grid.rows[Grid.row(cells[0])])
        if (cells.all { Grid.col(it) == Grid.col(cells[0]) }) add(Grid.cols[Grid.col(cells[0])])
    }

    fun <T> combinations(items: List<T>, size: Int): Sequence<List<T>> = sequence {
        if (size == 0) {
            yield(emptyList())
            return@sequence
        }
        for (i in 0..items.size - size) {
            for (rest in combinations(items.subList(i + 1, items.size), size - 1)) {
                yield(listOf(items[i]) + rest)
            }
        }
    }
}
