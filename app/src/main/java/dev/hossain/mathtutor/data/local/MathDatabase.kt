package dev.hossain.mathtutor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.GameSessionDao
import dev.hossain.mathtutor.data.local.dao.PerformanceDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.data.local.entity.GameSessionEntity
import dev.hossain.mathtutor.data.local.entity.PerformanceEntity
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.StreakEntity

/**
 * Room database for Kids Math Tutor app.
 * Stores practice session history, statistics, badge achievements, daily streaks,
 * performance records, and game session data.
 *
 * Database name: kids_math_tutor.db
 * Version: 7 (added 4 new Memory Match badges)
 *
 * Entities:
 * - [PracticeSessionEntity]: Completed practice sessions with statistics
 * - [BadgeEntity]: Badge achievements and unlock status
 * - [StreakEntity]: Daily practice streak tracking
 * - [PerformanceEntity]: Individual problem performance records for adaptive difficulty
 * - [GameSessionEntity]: Mini-game session data and scores
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
        GameSessionEntity::class,
    ],
    version = 7,
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

    /**
     * Provides access to game session data operations.
     *
     * @return GameSessionDao instance
     */
    abstract fun gameSessionDao(): GameSessionDao

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

        /**
         * Migration from database version 4 to version 5.
         * Adds the game_sessions table for mini-game tracking (Math Race, etc.).
         */
        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create game_sessions table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS game_sessions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            gameId TEXT NOT NULL,
                            startTime INTEGER NOT NULL,
                            endTime INTEGER NOT NULL,
                            score INTEGER NOT NULL,
                            correctAnswers INTEGER NOT NULL,
                            totalAttempts INTEGER NOT NULL,
                            durationSeconds INTEGER NOT NULL,
                            gradeLevel TEXT NOT NULL
                        )
                        """.trimIndent(),
                    )
                    // Create index on gameId for faster lookups
                    db.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_sessions_gameId ON game_sessions (gameId)
                        """.trimIndent(),
                    )
                }
            }

        /**
         * Migration from database version 5 to version 6.
         * Updates badge icons from emoji strings to BadgeIcon enum names.
         * This migration clears existing badges as emoji values cannot be mapped to enum.
         * Badges will be repopulated with proper enum values on next app launch.
         */
        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Clear existing badges since we can't migrate emoji strings to enum names
                    db.execSQL("DELETE FROM badges")
                }
            }

        /**
         * Migration from database version 6 to version 7.
         * Adds 4 new Memory Match badges to the database.
         */
        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Insert new Memory Match badges
                    // Memory Master
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO badges (id, name, description, icon, category, requirementType, requirementData, unlockedAt)
                        VALUES ('memory_master', 'Memory Master', 'Complete your first Memory Match', 'MEMORY_MASTER', 'GAMES', 'MemoryMatchCount', 'count=1', NULL)
                        """.trimIndent(),
                    )
                    // Sharp Memory
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO badges (id, name, description, icon, category, requirementType, requirementData, unlockedAt)
                        VALUES ('sharp_memory', 'Sharp Memory', 'Complete Memory Match in 12 or fewer moves', 'SHARP_MEMORY', 'GAMES', 'MemoryMatchMoves', 'maxMoves=12', NULL)
                        """.trimIndent(),
                    )
                    // Lightning Match
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO badges (id, name, description, icon, category, requirementType, requirementData, unlockedAt)
                        VALUES ('lightning_match', 'Lightning Match', 'Complete Memory Match in under 60 seconds', 'LIGHTNING_MATCH', 'GAMES', 'MemoryMatchTime', 'maxSeconds=60', NULL)
                        """.trimIndent(),
                    )
                    // Perfect Memory
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO badges (id, name, description, icon, category, requirementType, requirementData, unlockedAt)
                        VALUES ('perfect_memory', 'Perfect Memory', 'Complete with exactly 8 moves (perfect game)', 'PERFECT_MEMORY', 'GAMES', 'PerfectMemoryMatch', '', NULL)
                        """.trimIndent(),
                    )
                }
            }
    }
}
