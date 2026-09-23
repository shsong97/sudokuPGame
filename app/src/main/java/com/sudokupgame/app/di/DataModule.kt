package com.sudokupgame.app.di

import android.content.Context
import androidx.room.Room
import com.sudokupgame.app.data.AssetPuzzleRepository
import com.sudokupgame.app.data.DataStoreSettingsRepository
import com.sudokupgame.app.data.GameRepository
import com.sudokupgame.app.data.PuzzleRepository
import com.sudokupgame.app.data.RoomGameRepository
import com.sudokupgame.app.data.SettingsRepository
import com.sudokupgame.app.data.db.SudokuDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindPuzzleRepository(impl: AssetPuzzleRepository): PuzzleRepository

    @Binds
    abstract fun bindGameRepository(impl: RoomGameRepository): GameRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): SudokuDatabase =
            Room.databaseBuilder(context, SudokuDatabase::class.java, "sudoku.db").build()
    }
}
