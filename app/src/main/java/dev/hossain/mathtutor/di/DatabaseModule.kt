package dev.hossain.mathtutor.di

import androidx.room.Room
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.GameSessionDao
import dev.hossain.mathtutor.data.local.dao.PerformanceDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Provides Room database and DAOs for the application.
 * Uses Metro DI to provide database and DAOs as singletons scoped to the application lifecycle.
 *
 * This interface uses [ContributesTo] which automatically contributes bindings to the [AppScope].
 */
@ContributesTo(AppScope::class)
interface DatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideMathDatabase(
        @ApplicationContext context: android.content.Context,
    ): MathDatabase =
        Room
            .databaseBuilder(
                context,
                MathDatabase::class.java,
                MathDatabase.DATABASE_NAME,
            ).addMigrations(
                MathDatabase.MIGRATION_1_2,
                MathDatabase.MIGRATION_2_3,
                MathDatabase.MIGRATION_3_4,
                MathDatabase.MIGRATION_4_5,
                MathDatabase.MIGRATION_5_6,
            ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @SingleIn(AppScope::class)
    fun provideSessionDao(database: MathDatabase): SessionDao = database.sessionDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideBadgeDao(database: MathDatabase): BadgeDao = database.badgeDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideStreakDao(database: MathDatabase): StreakDao = database.streakDao()

    @Provides
    @SingleIn(AppScope::class)
    fun providePerformanceDao(database: MathDatabase): PerformanceDao = database.performanceDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideGameSessionDao(database: MathDatabase): GameSessionDao = database.gameSessionDao()
}
