package com.sudokupgame.app.feature.game

import com.sudokupgame.app.data.Puzzle
import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.Grid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GameStateTest {
    private val game = GameState.new(TestPuzzles.easy)
    private val solution = TestPuzzles.easy.solution

    /** 첫 번째 빈칸 (0행 2열), 정답 4. */
    private val empty = Grid.index(0, 2)
    private val wrong = if (solution[empty] == 1) 2 else 1

    @Test
    fun `주어진 숫자는 잠겨 있다`() {
        val selected = game.select(0)
        assertSame(selected, selected.input(9))
        assertSame(selected, selected.erase())
    }

    @Test
    fun `정답을 넣으면 확정되고 실수가 늘지 않는다`() {
        val state = game.select(empty).input(solution[empty])
        assertEquals(solution[empty], state.cells[empty].value)
        assertFalse(state.cells[empty].isError)
        assertTrue(state.cells[empty].isLocked)
        assertEquals(0, state.mistakes)
    }

    @Test
    fun `오답을 넣으면 오류로 표시되고 실수가 는다`() {
        val state = game.select(empty).input(wrong)
        assertTrue(state.cells[empty].isError)
        assertEquals(1, state.mistakes)
        assertEquals(GameStatus.PLAYING, state.status)
    }

    @Test
    fun `같은 오답을 다시 눌러도 실수는 한 번만 센다`() {
        val state = game.select(empty).input(wrong).input(wrong)
        assertEquals(1, state.mistakes)
    }

    @Test
    fun `실수 3번이면 게임 오버`() {
        val wrongDigits = (1..9).filter { it != solution[empty] }.take(3)
        var state = game.select(empty)
        wrongDigits.forEach { state = state.input(it) }
        assertEquals(3, state.mistakes)
        assertEquals(GameStatus.LOST, state.status)
        // 게임 오버 뒤에는 입력이 무시된다.
        assertSame(state, state.input(solution[empty]))
    }

    @Test
    fun `오답은 지울 수 있고 정답은 지울 수 없다`() {
        val erased = game.select(empty).input(wrong).erase()
        assertEquals(0, erased.cells[empty].value)
        assertEquals(1, erased.mistakes)

        val correct = game.select(empty).input(solution[empty])
        assertSame(correct, correct.erase())
    }

    @Test
    fun `실행 취소는 칸만 되돌리고 실수는 유지한다`() {
        val state = game.select(empty).input(wrong).undo()
        assertEquals(0, state.cells[empty].value)
        assertEquals(1, state.mistakes)
        assertFalse(state.canUndo)
    }

    @Test
    fun `메모 모드에서는 메모를 켜고 끈다`() {
        val notes = game.select(empty).toggleNotesMode().input(1).input(2)
        assertEquals(setOf(1, 2), notes.cells[empty].notes)
        assertEquals(0, notes.cells[empty].value)
        assertEquals(setOf(2), notes.input(1).cells[empty].notes)
        assertEquals(0, notes.mistakes)
    }

    @Test
    fun `정답을 넣으면 같은 줄의 메모에서 그 숫자가 지워진다`() {
        val peer = Grid.index(0, 3)
        val digit = solution[empty]
        val state = game
            .toggleNotesMode()
            .select(peer).input(digit)
            .toggleNotesMode()
            .select(empty).input(digit)
        assertFalse(digit in state.cells[peer].notes)
    }

    @Test
    fun `모든 칸을 채우면 클리어`() {
        var state = game
        for (i in 0 until Grid.CELLS) {
            if (state.cells[i].isEmpty) state = state.select(i).input(solution[i])
        }
        assertEquals(GameStatus.WON, state.status)
        assertNull(state.selected)
        assertTrue(state.completedDigits.containsAll((1..9).toList()))
    }

    @Test
    fun `일시정지 중에는 시간이 흐르지 않는다`() {
        assertEquals(1, game.tick().elapsedSeconds)
        assertEquals(0, game.pause().tick().elapsedSeconds)
        assertEquals(1, game.pause().resume().tick().elapsedSeconds)
    }

    @Test
    fun `다시 시작하면 처음 상태로 돌아간다`() {
        val state = game.select(empty).input(wrong).tick().restart()
        assertEquals(game, state)
    }
}

object TestPuzzles {
    val easy = Puzzle(
        id = "E001",
        difficulty = Difficulty.EASY,
        givens = Board.parse("530070000600195000098000060800060003400803001700020006060000280000419005000080079"),
        solution = Board.parse("534678912672195348198342567859761423426853791713924856961537284287419635345286179"),
    )
}
