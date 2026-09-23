package com.sudokupgame.app.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** 기존 사용자의 진행 상황과 기록이 DB 버전 업그레이드 후에도 남는지 확인한다. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SudokuDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsDataAndDefaultsHintsToZero() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                "INSERT INTO saved_game (id, puzzleId, difficulty, cells, mistakes, elapsedSeconds, notesMode, updatedAt) " +
                    "VALUES (0, 'E003', 'EASY', '${"00000".repeat(81)}', 1, 42, 0, 0)",
            )
            db.execSQL(
                "INSERT INTO puzzle_record (puzzleId, difficulty, wins, losses, bestTimeSeconds, totalWinTimeSeconds, lastPlayedAt) " +
                    "VALUES ('E001', 'EASY', 2, 1, 100, 250, 0)",
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true).use { db ->
            db.query("SELECT puzzleId, elapsedSeconds, hintsUsed FROM saved_game").use { c ->
                c.moveToFirst()
                assertEquals("E003", c.getString(0))
                assertEquals(42L, c.getLong(1))
                assertEquals(0, c.getInt(2))
            }
            db.query("SELECT wins, bestTimeSeconds, hintsUsed FROM puzzle_record WHERE puzzleId = 'E001'").use { c ->
                c.moveToFirst()
                assertEquals(2, c.getInt(0))
                assertEquals(100L, c.getLong(1))
                assertEquals(0, c.getInt(2))
            }
        }
    }

    @Test
    fun migrate2To3_defaultsModeToNumber() {
        helper.createDatabase(DB_NAME, 2).use { db ->
            db.execSQL(
                "INSERT INTO saved_game (id, puzzleId, difficulty, cells, mistakes, elapsedSeconds, notesMode, updatedAt, hintsUsed) " +
                    "VALUES (0, 'M002', 'MEDIUM', '${"00000".repeat(81)}', 0, 10, 1, 0, 2)",
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 3, true).use { db ->
            db.query("SELECT puzzleId, hintsUsed, mode FROM saved_game").use { c ->
                c.moveToFirst()
                assertEquals("M002", c.getString(0))
                assertEquals(2, c.getInt(1))
                assertEquals("NUMBER", c.getString(2))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
