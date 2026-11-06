package com.sudokupgame.app.data

import com.sudokupgame.engine.Difficulty
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 데일리 챌린지: 날짜마다 내장 퍼즐 중 하나를 정해 모두가 같은 퍼즐을 푼다 (오프라인, 서버 없음).
 *
 * 난이도는 요일로 정한다: 월·화 쉬움, 수·목 보통, 금·토 어려움, 일 전문가.
 * 같은 난이도 안에서는 주 단위로 순서를 섞어 고르므로, 그 난이도의 퍼즐을 한 바퀴 다 돌기 전에는 겹치지 않는다
 * (쉬움 100개 → 50주, 전문가 100개 → 100주).
 */
fun dailyDifficulty(date: LocalDate): Difficulty = dailyDifficulty(date.dayOfWeek)

private fun dailyDifficulty(day: DayOfWeek): Difficulty = when (day) {
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY -> Difficulty.EASY
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY -> Difficulty.MEDIUM
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY -> Difficulty.HARD
    DayOfWeek.SUNDAY -> Difficulty.EXPERT
}

/** [date]의 데일리 퍼즐 id. 해당 난이도 퍼즐이 없으면 null. */
fun dailyPuzzleId(puzzles: List<Puzzle>, date: LocalDate): String? {
    val difficulty = dailyDifficulty(date)
    val candidates = puzzles.filter { it.difficulty == difficulty }
    if (candidates.isEmpty()) return null

    val sameDifficultyDays = DayOfWeek.entries.filter { dailyDifficulty(it) == difficulty }
    val slot = sameDifficultyDays.indexOf(date.dayOfWeek)
    // 1970-01-01은 목요일. +3 하면 월요일마다 주 번호가 바뀐다.
    val week = Math.floorDiv(date.toEpochDay() + 3, 7L)
    val sequence = week * sameDifficultyDays.size + slot
    // 서로소인 수를 곱해 순서를 섞는다 (퍼즐 수와 서로소면 한 바퀴 동안 겹치지 않는다).
    val index = Math.floorMod(sequence * SHUFFLE_MULTIPLIER + SHUFFLE_OFFSET, candidates.size.toLong()).toInt()
    return candidates[index].id
}

private const val SHUFFLE_MULTIPLIER = 37L
private const val SHUFFLE_OFFSET = 11L

/** 데일리 챌린지를 클리어한 기록. 날짜마다 처음 클리어한 것만 남는다. */
data class DailyRecord(
    val date: LocalDate,
    val puzzleId: String,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
)

data class DailyStats(
    val totalCleared: Int,
    val currentStreak: Int,
    val bestStreak: Int,
)

/**
 * 오늘까지 이어진 연속 클리어 일수. 오늘을 아직 안 풀었어도 어제까지 이어졌으면 그 기록을 보여준다
 * (오늘 풀면 이어지므로).
 */
fun currentStreak(clearedDates: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in clearedDates) today else today.minusDays(1)
    var count = 0
    while (day in clearedDates) {
        count++
        day = day.minusDays(1)
    }
    return count
}

fun bestStreak(clearedDates: Set<LocalDate>): Int {
    var best = 0
    var run = 0
    var previous: LocalDate? = null
    for (date in clearedDates.sorted()) {
        run = if (previous != null && previous.plusDays(1) == date) run + 1 else 1
        best = maxOf(best, run)
        previous = date
    }
    return best
}

fun computeDailyStats(records: List<DailyRecord>, today: LocalDate): DailyStats {
    val dates = records.mapTo(mutableSetOf()) { it.date }
    return DailyStats(
        totalCleared = dates.size,
        currentStreak = currentStreak(dates, today),
        bestStreak = bestStreak(dates),
    )
}
