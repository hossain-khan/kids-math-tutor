package dev.hossain.mathtutor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity

/**
 * Room database for Kids Math Tutor app.
 * Stores practice session history, statistics, and badge achievements.
 *
 * Database name: kids_math_tutor.db
 * Version: 2 (added badge system)
 *
 * Entities:
 * - [PracticeSessionEntity]: Completed practice sessions with statistics
 * - [BadgeEntity]: Badge achievements and unlock status
 *
 * Type Converters:
 * - [Converters]: Handles MathOperation, BadgeCategory enums and Instant timestamp conversions
 */
@Database(
    entities = [
        PracticeSessionEntity::class,
        BadgeEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MathDatabase : RoomDatabase() {
    /**
     * Provides access to session data operations.
     *
     * @return SessionDao instance
     */
    abstract fun sessionDao(): SessionDao

    /**
     * Provides access to badge data operations.
     *
     * @return BadgeDao instance
     */
    abstract fun badgeDao(): BadgeDao

    companion object {
        /**
         * Database file name used for Room database creation.
         */
        const val DATABASE_NAME = "kids_math_tutor.db"

        /**
         * Migration from database version 1 to version 2.
         * Adds the badges table for the badge achievement system.
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create badges table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS badges (
                            id TEXT PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT NOT NULL,
                            icon TEXT NOT NULL,
                            category TEXT NOT NULL,
                            requirementType TEXT NOT NULL,
                            requirementData TEXT NOT NULL,
                            unlockedAt INTEGER
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
