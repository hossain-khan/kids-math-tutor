package dev.hossain.mathtutor.devtools

import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * Helper service to generate and persist sample practice sessions for testing.
 */
interface SessionSeeder {
    /**
     * Seeds the specified number of sessions. Returns number of sessions successfully seeded.
     */
    suspend fun seedSampleSessions(
        count: Int = 10,
        operation: MathOperation = MathOperation.MIXED,
        grade: GradeLevel = GradeLevel.GRADE_1,
        avgAccuracy: Float = 0.8f,
    ): Int
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SessionSeederImpl
    @Inject
    constructor(
        private val problemGenerator: ProblemGenerator,
        private val sessionRepository: SessionRepository,
    ) : SessionSeeder {
        override suspend fun seedSampleSessions(
            count: Int,
            operation: MathOperation,
            grade: GradeLevel,
            avgAccuracy: Float,
        ): Int =
            withContext(Dispatchers.IO) {
                var seeded = 0
                val rnd = Random(System.currentTimeMillis())

                // Distribute sessions across 5 days: 2 per day
                // Day 0 (today), Day 1 (yesterday), Day 2 (2 days ago), etc.
                val daysToDistribute = 5
                val sessionsPerDay = 2

                repeat(count.coerceAtLeast(0)) { index ->
                    try {
                        val dayOffset = (index / sessionsPerDay) % daysToDistribute
                        val sessionTimestamp = Instant.now().minus(dayOffset.toLong(), ChronoUnit.DAYS)

                        val op =
                            if (operation == MathOperation.MIXED) {
                                // pick a random non-mixed operation
                                listOf(
                                    MathOperation.ADDITION,
                                    MathOperation.SUBTRACTION,
                                    MathOperation.MULTIPLICATION,
                                    MathOperation.DIVISION,
                                ).random(rnd)
                            } else {
                                operation
                            }

                        val totalProblems = 10
                        val problems = problemGenerator.generateProblems(totalProblems, op, grade)
                        val answers = mutableMapOf<String, SessionAnswer>()

                        problems.forEach { p ->
                            val isCorrect = rnd.nextFloat() <= avgAccuracy
                            val userAnswer =
                                if (isCorrect) {
                                    p.correctAnswer
                                } else {
                                    p.correctAnswer +
                                        rnd.nextInt(1, 5) * if (rnd.nextBoolean()) 1 else -1
                                }
                            val answer =
                                SessionAnswer(
                                    problemId = p.id,
                                    userAnswer = userAnswer,
                                    isCorrect = p.checkAnswer(userAnswer),
                                    attemptCount = 1,
                                    timeSpentSeconds = (1 + rnd.nextInt(5)).toLong(),
                                )
                            answers[p.id] = answer
                        }

                        val durationSec = (problems.size * (5 + rnd.nextInt(10))).toLong()
                        val session =
                            PracticeSession(
                                totalProblems = problems.size,
                                problems = problems,
                                answers = answers,
                                operation = if (operation == MathOperation.MIXED) null else operation,
                                durationSeconds = durationSec,
                                completedAt = sessionTimestamp,
                            )

                        sessionRepository.saveSession(
                            session,
                            op,
                            durationSec, // gradeLevel
                            when (grade) {
                                GradeLevel.KINDERGARTEN -> 0
                                GradeLevel.GRADE_1 -> 1
                                GradeLevel.GRADE_2 -> 2
                            },
                        )

                        seeded++
                    } catch (e: Exception) {
                        // continue seeding other sessions
                    }
                }

                seeded
            }
    }
