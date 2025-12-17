package dev.hossain.mathtutor.di

import android.content.Context
import androidx.room.Room
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Provides Room database and DAO for the application.
 * Uses Metro DI to provide database and DAO as singletons scoped to the application lifecycle.
 */
@SingleIn(AppScope::class)
@Inject
class DatabaseModule
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val database: MathDatabase by lazy {
            Room
                .databaseBuilder(
                    context,
                    MathDatabase::class.java,
                    MathDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration() // For Phase 2: OK to lose data during development
                .build()
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideMathDatabase(): MathDatabase = database

        @Provides
        @SingleIn(AppScope::class)
        fun provideSessionDao(): SessionDao = database.sessionDao()
    }
