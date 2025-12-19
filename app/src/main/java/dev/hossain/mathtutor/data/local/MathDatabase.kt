package dev.hossain.mathtutor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.PerformanceDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.data.local.entity.PerformanceEntity
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.StreakEntity

/**
 * Room database for Kids Math Tutor app.
 * Stores practice session history, statistics, badge achievements, daily streaks, and performance records.
 *
 * Database name: kids_math_tutor.db
 * Version: 4 (added performance tracking system for adaptive difficulty)
 *
 * Entities:
 * - [PracticeSessionEntity]: Completed practice sessions with statistics
 * - [BadgeEntity]: Badge achievements and unlock status
 * - [StreakEntity]: Daily practice streak tracking
 * - [PerformanceEntity]: Individual problem performance records for adaptive difficulty
 *
 * Type Converters:
 * - [Converters]: Handles MathOperation, BadgeCategory, GradeLevel enums, Instant timestamp, and LocalDate conversions
 */
@Database(
    entities = [
        PracticeSessionEntity::class,
        BadgeEntity::class,
        StreakEntity::class,
        PerformanceEntity::class,
    ],
    version = 4,
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

    /**
     * Provides access to streak data operations.
     *
     * @return StreakDao instance
     */
    abstract fun streakDao(): StreakDao

    /**
     * Provides access to performance data operations.
     *
     * @return PerformanceDao instance
     */
    abstract fun performanceDao(): PerformanceDao

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

        /**
         * Migration from database version 2 to version 3.
         * Adds the streak table for daily practice streak tracking.
         */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create streak table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS streak (
                            id INTEGER PRIMARY KEY NOT NULL,
                            currentStreak INTEGER NOT NULL,
                            longestStreak INTEGER NOT NULL,
                            lastPracticeDate INTEGER,
                            totalDaysPracticed INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }

        /**
         * Migration from database version 3 to version 4.
         * Adds the performance_records table for adaptive difficulty tracking.
         */
        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create performance_records table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS performance_records (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            operation TEXT NOT NULL,
                            gradeLevel TEXT NOT NULL,
                            problemId TEXT NOT NULL,
                            isCorrect INTEGER NOT NULL,
                            attemptCount INTEGER NOT NULL,
                            timeSpentSeconds INTEGER NOT NULL,
                            timestamp INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
