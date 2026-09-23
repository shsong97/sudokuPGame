package com.sudokupgame.app.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.sudokupgame.app.data.FakeGameRepository
import com.sudokupgame.app.data.FakePuzzleRepository
import com.sudokupgame.app.data.FakeSettingsRepository
import com.sudokupgame.app.data.Settings
import com.sudokupgame.engine.Grid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val puzzles = FakePuzzleRepository(listOf(TestPuzzles.easy, TestPuzzles.easy.copy(id = "E002")))
    private val games = FakeGameRepository()
    private val settings = FakeSettingsRepository()
    private val solution = TestPuzzles.easy.solution
    private val empty = Grid.index(0, 2)
    private val wrong = if (solution[empty] == 1) 2 else 1

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    /**
     * 본문이 끝나면 ViewModel을 정리해 viewModelScope의 무한 타이머를 취소한다.
     * runTest는 본문 뒤에 남은 작업이 끝나길 기다리므로 @AfterEach 전에 취소해야 한다.
     */
    private fun runVmTest(block: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            block()
        } finally {
            store.clear()
        }
    }

    /** 앱 스코프 대신 테스트 스코프의 backgroundScope를 넘긴다. */
    private fun TestScope.viewModel(id: String): GameViewModel {
        val factory = viewModelFactory {
            initializer {
                GameViewModel(
                    SavedStateHandle(mapOf(GameViewModel.PUZZLE_ID_KEY to id)),
                    puzzles,
                    games,
                    settings,
                    backgroundScope,
                )
            }
        }
        return ViewModelProvider.create(store, factory)[GameViewModel::class]
    }

    private fun GameViewModel.game() = (uiState.value as GameUiState.Ready).game

    @Test
    fun `퍼즐을 불러오고 다음 퍼즐 id를 알려준다`() = runVmTest {
        val vm = viewModel("E001")
        vm.uiState.test {
            assertEquals(GameUiState.Loading, awaitItem())
            val ready = awaitItem() as GameUiState.Ready
            assertEquals("E001", ready.game.puzzleId)
            assertEquals("E002", ready.nextPuzzleId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `없는 퍼즐이면 NotFound`() = runVmTest {
        val vm = viewModel("Z999")
        runCurrent()
        assertEquals(GameUiState.NotFound, vm.uiState.value)
    }

    @Test
    fun `타이머는 1초마다 흐르고 일시정지하면 멈춘다`() = runVmTest {
        val vm = viewModel("E001")
        runCurrent()
        advanceTimeBy(3_001)
        assertEquals(3, vm.game().elapsedSeconds)

        vm.onPause()
        advanceTimeBy(5_000)
        assertEquals(3, vm.game().elapsedSeconds)
        assertEquals(GameStatus.PAUSED, vm.game().status)
    }

    @Test
    fun `입력하면 잠시 뒤 자동 저장된다`() = runVmTest {
        val vm = viewModel("E001")
        runCurrent()
        vm.onCellClick(empty)
        vm.onDigit(solution[empty])
        advanceTimeBy(600)
        val saved = games.saved.value
        assertNotNull(saved)
        assertEquals("E001", saved!!.puzzleId)
        assertEquals(solution[empty], saved.cells[empty].value)
    }

    @Test
    fun `같은 퍼즐의 저장된 게임이 있으면 이어서 한다`() = runVmTest {
        val first = viewModel("E001")
        runCurrent()
        first.onCellClick(empty)
        first.onDigit(wrong)
        advanceTimeBy(2_600)
        // 화면을 떠나면(ViewModel 정리) 마지막 경과 시간까지 저장된다.
        store.clear()
        runCurrent()

        val resumed = viewModel("E001")
        runCurrent()
        assertEquals(1, resumed.game().mistakes)
        assertEquals(wrong, resumed.game().cells[empty].value)
        assertEquals(2, resumed.game().elapsedSeconds)
    }

    @Test
    fun `다른 퍼즐의 저장된 게임은 무시하고 새로 시작한다`() = runVmTest {
        val first = viewModel("E001")
        runCurrent()
        first.onCellClick(empty)
        first.onDigit(wrong)
        advanceTimeBy(600)
        store.clear()

        val other = viewModel("E002")
        runCurrent()
        assertEquals(0, other.game().mistakes)
        advanceTimeBy(600)
        assertEquals("E002", games.saved.value?.puzzleId)
    }

    @Test
    fun `게임 오버되면 기록을 남기고 저장된 게임을 지운다`() = runVmTest {
        val vm = viewModel("E001")
        runCurrent()
        vm.onCellClick(empty)
        (1..9).filter { it != solution[empty] }.take(3).forEach { vm.onDigit(it) }
        advanceTimeBy(600)
        assertEquals(GameStatus.LOST, vm.game().status)
        assertNull(games.saved.value)
        assertEquals(1, games.recordList.value.single().losses)
    }

    @Test
    fun `클리어하면 승리와 최고 기록을 남긴다`() = runVmTest {
        val vm = viewModel("E001")
        runCurrent()
        advanceTimeBy(5_001)
        for (i in 0 until Grid.CELLS) {
            if (vm.game().cells[i].isEmpty) {
                vm.onCellClick(i)
                vm.onDigit(solution[i])
            }
        }
        advanceTimeBy(600)
        val record = games.recordList.value.single()
        assertEquals(1, record.wins)
        assertEquals(5L, record.bestTimeSeconds)
        assertNull(games.saved.value)
    }

    @Test
    fun `힌트 사용 횟수가 저장되고 기록에 더해진다`() = runVmTest {
        val vm = viewModel("E001")
        runCurrent()
        vm.onHint()
        vm.onRevealHint()
        vm.onHint()
        vm.onDismissHint()
        advanceTimeBy(600)
        assertEquals(2, games.saved.value?.hintsUsed)

        for (i in 0 until Grid.CELLS) {
            if (vm.game().cells[i].isEmpty) {
                vm.onCellClick(i)
                vm.onDigit(solution[i])
            }
        }
        advanceTimeBy(600)
        assertEquals(2, games.recordList.value.single().hintsUsed)
    }

    @Test
    fun `메모 자동 정리 설정을 따른다`() = runVmTest {
        settings.update { it.copy(autoRemoveNotes = false) }
        val vm = viewModel("E001")
        runCurrent()
        val peer = Grid.index(0, 3)
        val digit = solution[empty]
        vm.onToggleNotes()
        vm.onCellClick(peer)
        vm.onDigit(digit)
        vm.onToggleNotes()
        vm.onCellClick(empty)
        vm.onDigit(digit)
        assertTrue(digit in vm.game().cells[peer].notes)
        assertFalse(Settings().autoRemoveNotes == vm.settings.value.autoRemoveNotes)
    }
}
