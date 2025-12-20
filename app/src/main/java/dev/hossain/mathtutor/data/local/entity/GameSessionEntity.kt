package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GradeLevel
import java.time.Instant

/**
 * Room entity representing a completed game session.
 * Stores all statistics and metadata from a single game play for persistence.
 *
 * The gameId column is indexed to optimize queries that filter by game type,
 * such as getting personal best or stats for a specific game.
 *
 * @property id Unique identifier for this game session (auto-generated)
 * @property gameId The game type identifier (Game enum name, e.g., "MATH_RACE")
 * @property startTime When the game session started (stored as epoch milliseconds)
 * @property endTime When the game session ended (stored as epoch milliseconds)
 * @property score Number of correct answers (points earned) in this session
 * @property correctAnswers Number of problems answered correctly
 * @property totalAttempts Total number of problems attempted (correct + incorrect)
 * @property durationSeconds Actual duration of the game session in seconds
 * @property gradeLevel The grade level at which the game was played (GradeLevel enum name)
 */
@Entity(
    tableName = "game_sessions",
    indices = [
        Index(value = ["gameId"]),
    ],
)
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val startTime: Instant,
    val endTime: Instant,
    val score: Int,
    val correctAnswers: Int,
    val totalAttempts: Int,
    val durationSeconds: Int,
    val gradeLevel: GradeLevel,
) {
    /**
     * Converts this entity to a domain model.
     *
     * @param isNewRecord Whether this session achieved a new personal best
     * @return GameSession domain model
     */
    fun toDomainModel(isNewRecord: Boolean = false): dev.hossain.mathtutor.domain.model.GameSession =
        dev.hossain.mathtutor.domain.model.GameSession(
            id = id,
            game = Game.valueOf(gameId),
            startTime = startTime,
            endTime = endTime,
            score = score,
            correctAnswers = correctAnswers,
            totalAttempts = totalAttempts,
            durationSeconds = durationSeconds,
            gradeLevel = gradeLevel,
            isNewRecord = isNewRecord,
        )

    companion object {
        /**
         * Creates an entity from a domain model.
         *
         * @param session The domain model to convert
         * @return GameSessionEntity for database storage
         */
        fun fromDomainModel(session: dev.hossain.mathtutor.domain.model.GameSession): GameSessionEntity =
            GameSessionEntity(
                id = session.id,
                gameId = session.game.name,
                startTime = session.startTime,
                endTime = session.endTime,
                score = session.score,
                correctAnswers = session.correctAnswers,
                totalAttempts = session.totalAttempts,
                durationSeconds = session.durationSeconds,
                gradeLevel = session.gradeLevel,
            )
    }
}
