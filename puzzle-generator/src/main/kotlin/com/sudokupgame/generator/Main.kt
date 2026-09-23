package com.sudokupgame.generator

import com.sudokupgame.engine.Difficulty
import com.sudokupgame.engine.GeneratedPuzzle
import com.sudokupgame.engine.PuzzleGenerator
import com.sudokupgame.engine.Symmetry
import java.io.File
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * 개발용 퍼즐 생성 CLI. 난이도별로 퍼즐을 생성해 JSON으로 저장한다.
 * 같은 시드면 항상 같은 결과가 나온다.
 *
 * 실행: ./gradlew :puzzle-generator:run --args="--seed=2026 --count=25"
 */
fun main(args: Array<String>) {
    val options = args.associate { arg ->
        val (key, value) = arg.removePrefix("--").split("=", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        key to value
    }
    val seed = options["seed"]?.toLong() ?: DEFAULT_SEED
    val count = options["count"]?.toInt() ?: DEFAULT_COUNT
    val out = File(options["out"] ?: DEFAULT_OUT)

    println("시드 $seed, 난이도별 ${count}개 생성 → ${out.path}")
    lateinit var file: PuzzleFile
    val millis = measureTimeMillis {
        val executor = Executors.newFixedThreadPool(Difficulty.entries.size)
        val futures = Difficulty.entries.map { difficulty ->
            executor.submit<List<GeneratedPuzzle>> {
                generateSet(difficulty, count, Random(seed * 31 + difficulty.ordinal))
            }
        }
        val sets = futures.map { it.get() }
        executor.shutdown()
        file = PuzzleFile(version = 1, puzzles = sets.flatMap(::toEntries))
    }

    out.parentFile?.mkdirs()
    out.writeText(PuzzleJson.encodeToString(PuzzleFile.serializer(), file) + "\n")
    println("완료: ${file.puzzles.size}개, ${millis / 1000.0}초")
}

/** 중복(회전·숫자 치환 포함) 없이 [count]개를 만들고 쉬운 순으로 정렬한다. */
private fun generateSet(difficulty: Difficulty, count: Int, random: Random): List<GeneratedPuzzle> {
    val generator = PuzzleGenerator(random)
    val seen = mutableSetOf<String>()
    val result = mutableListOf<GeneratedPuzzle>()
    while (result.size < count) {
        val puzzle = checkNotNull(generator.generate(difficulty)) { "$difficulty 퍼즐 생성 실패" }
        if (seen.add(Symmetry.canonical(puzzle.puzzle))) {
            result += puzzle
            println("  $difficulty ${result.size}/$count (힌트 ${puzzle.puzzle.filledCount}, 점수 ${puzzle.score})")
        }
    }
    return result.sortedWith(compareBy({ it.score }, { -it.puzzle.filledCount }))
}

private fun toEntries(puzzles: List<GeneratedPuzzle>): List<PuzzleEntry> =
    puzzles.mapIndexed { i, p ->
        PuzzleEntry(
            id = idPrefix(p.difficulty) + (i + 1).toString().padStart(3, '0'),
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
private const val DEFAULT_COUNT = 25
private const val DEFAULT_OUT = "app/src/main/assets/puzzles.json"
