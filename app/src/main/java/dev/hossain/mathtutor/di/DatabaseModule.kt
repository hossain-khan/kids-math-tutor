package dev.hossain.mathtutor.di

import android.content.Context
import androidx.room.Room
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Provides Room database instance for the application.
 * Uses Metro DI to provide database as a singleton scoped to the application lifecycle.
 */
interface DatabaseProvider {
    fun provideDatabase(): MathDatabase
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DatabaseProviderImpl
    constructor(
        @ApplicationContext private val context: Context,
    ) : DatabaseProvider {
        private val database: MathDatabase by lazy {
            Room
                .databaseBuilder(
                    context,
                    MathDatabase::class.java,
                    MathDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration() // For Phase 2: OK to lose data during development
                .build()
        }

        override fun provideDatabase(): MathDatabase = database
    }

/**
 * Provides SessionDao for database operations on practice sessions.
 * Depends on DatabaseProvider to get the database instance.
 */
interface SessionDaoProvider {
    fun provideSessionDao(): SessionDao
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SessionDaoProviderImpl
    constructor(
        private val databaseProvider: DatabaseProvider,
    ) : SessionDaoProvider {
        override fun provideSessionDao(): SessionDao = databaseProvider.provideDatabase().sessionDao()
    }
