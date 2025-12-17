package dev.hossain.mathtutor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity

/**
 * Room database for Kids Math Tutor app.
 * Stores practice session history and statistics.
 *
 * Database name: kids_math_tutor.db
 * Version: 1 (initial release)
 *
 * Entities:
 * - [PracticeSessionEntity]: Completed practice sessions with statistics
 *
 * Type Converters:
 * - [Converters]: Handles MathOperation enum and Instant timestamp conversions
 */
@Database(
    entities = [PracticeSessionEntity::class],
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

    companion object {
        /**
         * Database file name used for Room database creation.
         */
        const val DATABASE_NAME = "kids_math_tutor.db"
    }
}
