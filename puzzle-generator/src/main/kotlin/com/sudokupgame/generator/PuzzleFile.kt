package com.sudokupgame.generator

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** app/src/main/assets/puzzles.json 형식. */
@Serializable
data class PuzzleFile(
    val version: Int,
    val puzzles: List<PuzzleEntry>,
)

@Serializable
data class PuzzleEntry(
    val id: String,
    val difficulty: String,
    val givens: String,
    val solution: String,
)

val PuzzleJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}
