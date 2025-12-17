package dev.hossain.mathtutor.di

import androidx.room.Room
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
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
            ).addMigrations(MathDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @SingleIn(AppScope::class)
    fun provideSessionDao(database: MathDatabase): SessionDao = database.sessionDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideBadgeDao(database: MathDatabase): BadgeDao = database.badgeDao()
}
