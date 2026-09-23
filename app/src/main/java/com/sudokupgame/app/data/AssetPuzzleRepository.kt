package com.sudokupgame.app.data

import android.content.Context
import com.sudokupgame.engine.Board
import com.sudokupgame.engine.Difficulty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** assets/puzzles.json 에서 내장 퍼즐을 읽는다. 처음 한 번만 읽고 메모리에 둔다. */
@Singleton
class AssetPuzzleRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PuzzleRepository {

    private val mutex = Mutex()
    private var cache: List<Puzzle>? = null

    override suspend fun puzzles(): List<Puzzle> = mutex.withLock {
        cache ?: load().also { cache = it }
    }

    private suspend fun load(): List<Puzzle> = withContext(Dispatchers.IO) {
        val text = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        json.decodeFromString(PuzzleFileDto.serializer(), text).puzzles.map { dto ->
            Puzzle(
                id = dto.id,
                difficulty = Difficulty.valueOf(dto.difficulty),
                givens = Board.parse(dto.givens),
                solution = Board.parse(dto.solution),
            )
        }
    }

    @Serializable
    private data class PuzzleFileDto(val version: Int, val puzzles: List<PuzzleDto>)

    @Serializable
    private data class PuzzleDto(val id: String, val difficulty: String, val givens: String, val solution: String)

    private companion object {
        const val FILE_NAME = "puzzles.json"
        val json = Json { ignoreUnknownKeys = true }
    }
}
