package com.sudokupgame.app.data

import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DailyChallengeTest {
    private val puzzles = Difficulty.entries.flatMap { difficulty ->
        (1..100).map { n -> Puzzle("${difficulty.name}-$n", difficulty, Board.EMPTY, Board.EMPTY) }
    }

    /** 2026-11-02는 월요일. */
    private val monday = LocalDate.of(2026, 11, 2)

    @Test
    fun `요일마다 난이도가 정해진다`() {
        val expected = listOf(
            Difficulty.EASY, Difficulty.EASY,
            Difficulty.MEDIUM, Difficulty.MEDIUM,
            Difficulty.HARD, Difficulty.HARD,
            Difficulty.EXPERT,
        )
        assertEquals(expected, (0L..6L).map { dailyDifficulty(monday.plusDays(it)) })
    }

    @Test
    fun `같은 날은 항상 같은 퍼즐이고 난이도도 맞다`() {
        for (offset in 0L..13L) {
            val date = monday.plusDays(offset)
            val id = dailyPuzzleId(puzzles, date)!!
            assertEquals(id, dailyPuzzleId(puzzles, date))
            assertEquals(dailyDifficulty(date), puzzles.single { it.id == id }.difficulty)
        }
    }

    @Test
    fun `같은 난이도의 퍼즐을 한 바퀴 돌기 전에는 겹치지 않는다`() {
        // 쉬움은 주 2일 → 100개를 50주 동안, 전문가는 주 1일 → 100주 동안.
        for (difficulty in Difficulty.entries) {
            val days = (0L until 7L * 100).map { monday.plusDays(it) }.filter { dailyDifficulty(it) == difficulty }.take(100)
            val ids = days.map { dailyPuzzleId(puzzles, it) }
            assertEquals(100, ids.toSet().size, "$difficulty")
        }
    }

    @Test
    fun `1970년 이전 날짜도 고를 수 있다`() {
        assertTrue(dailyPuzzleId(puzzles, LocalDate.of(1960, 3, 1)) != null)
    }

    @Test
    fun `해당 난이도 퍼즐이 없으면 null`() {
        assertNull(dailyPuzzleId(puzzles.filter { it.difficulty != Difficulty.EASY }, monday))
    }

    @Test
    fun `연속 일수는 오늘 또는 어제까지 이어진 날을 센다`() {
        val today = LocalDate.of(2026, 11, 6)
        val days = setOf(today.minusDays(3), today.minusDays(2), today.minusDays(1))
        assertEquals(3, currentStreak(days, today))
        assertEquals(4, currentStreak(days + today, today))
        // 이틀 전까지만 했으면 끊긴 것.
        assertEquals(0, currentStreak(setOf(today.minusDays(2)), today))
        assertEquals(0, currentStreak(emptySet(), today))
    }

    @Test
    fun `통계는 총 클리어 날수와 최고 연속을 계산한다`() {
        val today = LocalDate.of(2026, 11, 6)
        fun record(date: LocalDate) = DailyRecord(date, "E001", 60, 0, 0)
        val records = listOf(
            record(LocalDate.of(2026, 10, 1)),
            record(LocalDate.of(2026, 10, 2)),
            record(LocalDate.of(2026, 10, 3)),
            record(today.minusDays(1)),
            record(today),
        )
        assertEquals(DailyStats(totalCleared = 5, currentStreak = 2, bestStreak = 3), computeDailyStats(records, today))
        assertEquals(DailyStats(0, 0, 0), computeDailyStats(emptyList(), today))
    }
}
