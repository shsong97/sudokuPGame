package com.sudokupgame.app.feature.game

import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.data.Puzzle
import com.sudokupgame.app.data.SavedGame
import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.Grid
import com.sudokupgame.engine.LogicalSolver
import com.sudokupgame.engine.Technique

const val MAX_MISTAKES = 3

enum class GameStatus { PLAYING, PAUSED, WON, LOST }

/** 화면에 떠 있는 힌트. 1단계는 칸과 이유를 보여주고, 2단계(reveal)에서 정답을 넣거나 오답을 지운다. */
sealed interface Hint {
    val target: Int

    /** 틀린 숫자가 있으면 먼저 그것부터 알려준다. */
    data class WrongValue(override val target: Int) : Hint

    data class Placement(
        override val target: Int,
        val digit: Int,
        val technique: Technique,
        val focusCells: Set<Int>,
    ) : Hint
}

data class CellState(
    val value: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false,
) {
    val isEmpty: Boolean get() = value == 0

    /** 처음부터 주어졌거나 정답으로 채워진 칸은 더 이상 바꿀 수 없다. */
    val isLocked: Boolean get() = isGiven || (value != 0 && !isError)
}

/**
 * 게임 한 판의 상태와 규칙. 모든 동작은 새 상태를 돌려준다.
 * 정답 입력은 확정(잠금)되고, 오답 입력은 실수로 기록되며 지우거나 덮어쓸 수 있다.
 */
data class GameState(
    val puzzleId: String,
    val difficulty: Difficulty,
    val cells: List<CellState>,
    val solution: Board,
    val selected: Int? = null,
    val notesMode: Boolean = false,
    val mistakes: Int = 0,
    val elapsedSeconds: Long = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val history: List<List<CellState>> = emptyList(),
    val hintsUsed: Int = 0,
    val hint: Hint? = null,
    /** 표시 방식. 규칙에는 영향이 없다. */
    val mode: GameMode = GameMode.NUMBER,
) {
    val canUndo: Boolean get() = status == GameStatus.PLAYING && history.isNotEmpty()

    /** 정답으로 9번 다 채운 숫자 (숫자 패드 비활성용). */
    val completedDigits: Set<Int>
        get() = (1..9).filterTo(mutableSetOf()) { d -> cells.count { it.value == d && !it.isError } == 9 }

    fun remainingCount(digit: Int): Int = 9 - cells.count { it.value == digit && !it.isError }

    fun select(index: Int): GameState =
        if (status == GameStatus.PLAYING) copy(selected = index, hint = null) else this

    fun toggleNotesMode(): GameState = copy(notesMode = !notesMode)

    /** [autoRemoveNotes]가 켜져 있으면 정답을 넣을 때 같은 행·열·박스 메모에서 그 숫자를 지운다. */
    fun input(digit: Int, autoRemoveNotes: Boolean = true): GameState {
        require(digit in 1..9)
        val index = selected ?: return this
        val cell = cells[index]
        if (status != GameStatus.PLAYING || cell.isLocked) return this
        return if (notesMode) toggleNote(index, cell, digit) else placeDigit(index, cell, digit, autoRemoveNotes)
    }

    private fun toggleNote(index: Int, cell: CellState, digit: Int): GameState {
        if (!cell.isEmpty) return this
        val notes = if (digit in cell.notes) cell.notes - digit else cell.notes + digit
        return withCells(cells.replace(index, cell.copy(notes = notes)))
    }

    private fun placeDigit(index: Int, cell: CellState, digit: Int, autoRemoveNotes: Boolean): GameState {
        if (cell.value == digit) return this
        if (solution[index] != digit) {
            val mistakes = mistakes + 1
            return withCells(cells.replace(index, CellState(value = digit, isError = true))).copy(
                mistakes = mistakes,
                status = if (mistakes >= MAX_MISTAKES) GameStatus.LOST else status,
            )
        }
        val updated = cells.toMutableList()
        updated[index] = CellState(value = digit)
        if (autoRemoveNotes) {
            for (peer in Grid.peers[index]) {
                val p = updated[peer]
                if (digit in p.notes) updated[peer] = p.copy(notes = p.notes - digit)
            }
        }
        val next = withCells(updated)
        val solved = next.cells.withIndex().all { (i, c) -> c.value == solution[i] }
        return if (solved) next.copy(status = GameStatus.WON, selected = null) else next
    }

    fun erase(): GameState {
        val index = selected ?: return this
        val cell = cells[index]
        if (status != GameStatus.PLAYING || cell.isLocked) return this
        if (cell.isEmpty && cell.notes.isEmpty()) return this
        return withCells(cells.replace(index, CellState()))
    }

    /** 칸 상태만 되돌린다. 실수 횟수는 줄지 않는다. */
    fun undo(): GameState {
        if (!canUndo) return this
        return copy(cells = history.last(), history = history.dropLast(1), hint = null)
    }

    fun pause(): GameState = if (status == GameStatus.PLAYING) copy(status = GameStatus.PAUSED, hint = null) else this

    fun resume(): GameState = if (status == GameStatus.PAUSED) copy(status = GameStatus.PLAYING) else this

    fun tick(): GameState = if (status == GameStatus.PLAYING) copy(elapsedSeconds = elapsedSeconds + 1) else this

    /** 같은 퍼즐을 처음부터. */
    fun restart(): GameState = GameState(
        puzzleId = puzzleId,
        difficulty = difficulty,
        cells = cells.map { if (it.isGiven) it else CellState() },
        solution = solution,
        mode = mode,
    )

    /** 1단계 힌트: 틀린 칸이 있으면 그 칸을, 없으면 다음에 확정할 수 있는 칸과 기법을 보여준다. */
    fun requestHint(): GameState {
        if (status != GameStatus.PLAYING || hint != null) return this
        val wrong = cells.indexOfFirst { it.isError }
        val hint = if (wrong >= 0) Hint.WrongValue(wrong) else findPlacementHint() ?: return this
        return copy(hint = hint, selected = hint.target, hintsUsed = hintsUsed + 1)
    }

    /** 2단계 힌트: 정답을 넣거나(실수로 세지 않음) 틀린 숫자를 지운다. */
    fun revealHint(autoRemoveNotes: Boolean = true): GameState = when (val h = hint) {
        null -> this
        is Hint.WrongValue -> copy(selected = h.target, hint = null).erase()
        is Hint.Placement -> copy(selected = h.target, notesMode = false, hint = null)
            .input(h.digit, autoRemoveNotes)
            .copy(notesMode = notesMode)
    }

    fun dismissHint(): GameState = if (hint != null) copy(hint = null) else this

    private fun findPlacementHint(): Hint.Placement? {
        val board = Board.of(IntArray(Grid.CELLS) { cells[it].value })
        val next = LogicalSolver.nextPlacement(board)
        if (next != null) {
            return Hint.Placement(
                target = next.placement.index,
                digit = next.placement.digit,
                technique = next.technique,
                focusCells = next.focusCells.toSet() - next.placement.index,
            )
        }
        // 기법으로 진행할 수 없는 경우(내장 퍼즐에서는 일어나지 않음): 빈칸 하나의 정답을 알려준다.
        val empty = cells.indexOfFirst { it.isEmpty }.takeIf { it >= 0 } ?: return null
        return Hint.Placement(empty, solution[empty], Technique.NAKED_SINGLE, emptySet())
    }

    private fun withCells(newCells: List<CellState>): GameState =
        copy(cells = newCells, history = (history + listOf(cells)).takeLast(MAX_HISTORY), hint = null)

    companion object {
        private const val MAX_HISTORY = 200

        fun new(puzzle: Puzzle, mode: GameMode = GameMode.NUMBER): GameState = GameState(
            puzzleId = puzzle.id,
            difficulty = puzzle.difficulty,
            cells = List(Grid.CELLS) { i ->
                val value = puzzle.givens[i]
                CellState(value = value, isGiven = value != 0)
            },
            solution = puzzle.solution,
            mode = mode,
        )

        /** 저장된 게임을 이어서. 실행 취소 기록은 비어 있다. [mode]로 표시 방식을 바꿔 이어갈 수 있다. */
        fun restore(puzzle: Puzzle, saved: SavedGame, mode: GameMode = saved.mode): GameState = GameState(
            puzzleId = puzzle.id,
            difficulty = puzzle.difficulty,
            cells = saved.cells,
            solution = puzzle.solution,
            mistakes = saved.mistakes,
            elapsedSeconds = saved.elapsedSeconds,
            notesMode = saved.notesMode,
            hintsUsed = saved.hintsUsed,
            mode = mode,
        )
    }

    fun toSavedGame(): SavedGame = SavedGame(
        puzzleId = puzzleId,
        difficulty = difficulty,
        cells = cells,
        mistakes = mistakes,
        elapsedSeconds = elapsedSeconds,
        notesMode = notesMode,
        hintsUsed = hintsUsed,
        mode = mode,
    )
}

private fun <T> List<T>.replace(index: Int, value: T): List<T> =
    toMutableList().also { it[index] = value }
