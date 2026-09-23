package com.sudokupgame.app.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.sudokupgame.app.data.Puzzle
import com.sudokupgame.app.data.PuzzleRepository
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val repository = object : PuzzleRepository {
        override suspend fun puzzles(): List<Puzzle> =
            listOf(TestPuzzles.easy, TestPuzzles.easy.copy(id = "E002"))
    }

    private val store = ViewModelStore()

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

    private fun viewModel(id: String): GameViewModel {
        val factory = viewModelFactory {
            initializer { GameViewModel(SavedStateHandle(mapOf(GameViewModel.PUZZLE_ID_KEY to id)), repository) }
        }
        return ViewModelProvider.create(store, factory)[GameViewModel::class]
    }

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
        assertEquals(3, (vm.uiState.value as GameUiState.Ready).game.elapsedSeconds)

        vm.onPause()
        advanceTimeBy(5_000)
        val game = (vm.uiState.value as GameUiState.Ready).game
        assertEquals(3, game.elapsedSeconds)
        assertTrue(game.status == GameStatus.PAUSED)
    }
}
