package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.PracticeSession
import java.time.Instant

/**
 * Mapper for converting between domain and data layer session objects.
 */
object SessionMapper {
    /**
     * Converts a domain [PracticeSession] to a [PracticeSessionEntity] for database storage.
     *
     * @param session The practice session to convert
     * @param operation The math operation practiced
     * @param durationSeconds Time spent completing the session in seconds
     * @param gradeLevel Optional grade level (K=0, 1st=1, 2nd=2)
     * @return Database entity ready for insertion
     */
    fun toEntity(
        session: PracticeSession,
        operation: MathOperation,
        durationSeconds: Long,
        gradeLevel: Int? = null,
    ): PracticeSessionEntity {
        val correctAnswers = session.getCorrectCount()
        val incorrectAnswers = session.getIncorrectCount()
        val accuracy = session.getAccuracy()

        return PracticeSessionEntity(
            operation = operation,
            totalProblems = session.totalProblems,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            accuracy = accuracy,
            durationSeconds = durationSeconds,
            timestamp = Instant.now(),
            gradeLevel = gradeLevel,
        )
    }
}
