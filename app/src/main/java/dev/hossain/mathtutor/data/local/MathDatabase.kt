package dev.hossain.mathtutor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.dao.CustomChallengeDao
import dev.hossain.mathtutor.data.local.dao.GameSessionDao
import dev.hossain.mathtutor.data.local.dao.PerformanceDao
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.data.local.entity.ChallengePracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.ChallengeProblemsEntity
import dev.hossain.mathtutor.data.local.entity.CustomChallengeEntity
import dev.hossain.mathtutor.data.local.entity.GameSessionEntity
import dev.hossain.mathtutor.data.local.entity.PerformanceEntity
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.data.local.entity.StreakEntity

/**
 * Room database for Kids Math Tutor app.
 * Stores practice session history, statistics, badge achievements, daily streaks,
 * performance records, game session data, and custom challenges.
 *
 * Database name: kids_math_tutor.db
 * Version: 1 (initial release version with all features)
 *
 * Entities:
 * - [PracticeSessionEntity]: Completed practice sessions with statistics
 * - [BadgeEntity]: Badge achievements and unlock status
 * - [StreakEntity]: Daily practice streak tracking
 * - [PerformanceEntity]: Individual problem performance records for adaptive difficulty
 * - [GameSessionEntity]: Mini-game session data and scores
 * - [CustomChallengeEntity]: Parent-created custom challenges
 * - [ChallengeProblemsEntity]: Math problems within custom challenges
 * - [ChallengePracticeSessionEntity]: Practice sessions for custom challenges
 *
 * Type Converters:
 * - [Converters]: Handles MathOperation, BadgeCategory, ChallengeType, GradeLevel enums, Instant timestamp, and LocalDate conversions
 */
@Database(
    entities = [
        PracticeSessionEntity::class,
        BadgeEntity::class,
        StreakEntity::class,
        PerformanceEntity::class,
        GameSessionEntity::class,
        CustomChallengeEntity::class,
        ChallengeProblemsEntity::class,
        ChallengePracticeSessionEntity::class,
    ],
    version = 1,
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

    /**
     * Provides access to custom challenge data operations.
     *
     * @return CustomChallengeDao instance
     */
    abstract fun customChallengeDao(): CustomChallengeDao

    companion object {
        /**
         * Database file name used for Room database creation.
         */
        const val DATABASE_NAME = "kids_math_tutor.db"
    }
}
