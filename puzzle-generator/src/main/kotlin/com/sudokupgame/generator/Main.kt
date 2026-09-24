package com.sudokupgame.generator

import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.GeneratedPuzzle
import com.sudokupgame.engine.PuzzleGenerator
import com.sudokupgame.engine.Symmetry
import java.io.File
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * 개발용 퍼즐 생성 CLI. 난이도별로 [count]개가 되도록 퍼즐을 채워 JSON으로 저장한다.
 *
 * 이미 파일이 있으면 기존 퍼즐의 id·내용·순서는 그대로 두고 모자란 만큼만 뒤에 추가한다.
 * (앱에 저장된 기록·진행 중인 게임이 id로 퍼즐을 가리키므로 기존 퍼즐을 바꾸면 안 된다.)
 * 새 퍼즐은 기존 퍼즐과 회전·숫자 치환까지 겹치지 않고, 추가분끼리만 쉬운 순으로 정렬한다.
 * 같은 시드·같은 기존 파일이면 항상 같은 결과가 나온다.
 *
 * 실행: ./gradlew :puzzle-generator:run --args="--count=100"
 *       ./gradlew :puzzle-generator:run --args="--fresh --seed=2026 --count=25"  (전부 새로 생성)
 */
fun main(args: Array<String>) {
    val options = args.associate { arg ->
        val (key, value) = arg.removePrefix("--").split("=", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        key to value
    }
    val seed = options["seed"]?.toLong() ?: DEFAULT_SEED
    val count = options["count"]?.toInt() ?: DEFAULT_COUNT
    val out = File(options["out"] ?: DEFAULT_OUT)
    val fresh = "fresh" in options

    val existing: Map<Difficulty, List<PuzzleEntry>> =
        if (!fresh && out.exists()) {
            PuzzleJson.decodeFromString(PuzzleFile.serializer(), out.readText()).puzzles
                .groupBy { Difficulty.valueOf(it.difficulty) }
        } else {
            emptyMap()
        }

    println("시드 $seed, 난이도별 ${count}개 (기존 ${existing.values.sumOf { it.size }}개 유지) → ${out.path}")
    lateinit var file: PuzzleFile
    val millis = measureTimeMillis {
        val executor = Executors.newFixedThreadPool(Difficulty.entries.size)
        val futures = Difficulty.entries.map { difficulty ->
            executor.submit<List<PuzzleEntry>> {
                val kept = existing[difficulty].orEmpty()
                if (kept.size > count) println("  $difficulty: 기존 ${kept.size}개가 목표보다 많아 그대로 둡니다")
                val added = generateSet(
                    difficulty = difficulty,
                    count = count - kept.size,
                    exclude = kept.map { Symmetry.canonical(Board.parse(it.givens)) }.toSet(),
                    random = Random(seed * 31 + difficulty.ordinal),
                )
                kept + toEntries(added, firstNumber = kept.size + 1)
            }
        }
        val sets = futures.map { it.get() }
        executor.shutdown()
        file = PuzzleFile(version = 1, puzzles = sets.flatten())
    }

    out.parentFile?.mkdirs()
    out.writeText(PuzzleJson.encodeToString(PuzzleFile.serializer(), file) + "\n")
    println("완료: ${file.puzzles.size}개, ${millis / 1000.0}초")
}

/** [exclude]와 서로 겹치지 않게(회전·숫자 치환 포함) [count]개를 만들고 쉬운 순으로 정렬한다. */
private fun generateSet(difficulty: Difficulty, count: Int, exclude: Set<String>, random: Random): List<GeneratedPuzzle> {
    val generator = PuzzleGenerator(random)
    val seen = exclude.toMutableSet()
    val result = mutableListOf<GeneratedPuzzle>()
    while (result.size < count) {
        val puzzle = checkNotNull(generator.generate(difficulty)) { "$difficulty 퍼즐 생성 실패" }
        if (seen.add(Symmetry.canonical(puzzle.puzzle))) {
            result += puzzle
            println("  $difficulty +${result.size}/$count (힌트 ${puzzle.puzzle.filledCount}, 점수 ${puzzle.score})")
        }
    }
    return result.sortedWith(compareBy({ it.score }, { -it.puzzle.filledCount }))
}

private fun toEntries(puzzles: List<GeneratedPuzzle>, firstNumber: Int): List<PuzzleEntry> =
    puzzles.mapIndexed { i, p ->
        PuzzleEntry(
            id = idPrefix(p.difficulty) + (firstNumber + i).toString().padStart(3, '0'),
            difficulty = p.difficulty.name,
            givens = p.puzzle.toCompactString(),
            solution = p.solution.toCompactString(),
        )
    }

fun idPrefix(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "E"
    Difficulty.MEDIUM -> "M"
    Difficulty.HARD -> "H"
    Difficulty.EXPERT -> "X"
}

private const val DEFAULT_SEED = 2026L
private const val DEFAULT_COUNT = 100
private const val DEFAULT_OUT = "app/src/main/assets/puzzles.json"
