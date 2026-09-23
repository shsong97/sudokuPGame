package com.sudokupgame.app.di

import com.sudokupgame.app.data.AssetPuzzleRepository
import com.sudokupgame.app.data.PuzzleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindPuzzleRepository(impl: AssetPuzzleRepository): PuzzleRepository
}
