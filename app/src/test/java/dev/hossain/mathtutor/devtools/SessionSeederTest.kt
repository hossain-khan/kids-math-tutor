package dev.hossain.mathtutor.devtools

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.repository.SessionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Instant

class SessionSeederTest {
    @Test
    fun `seedSampleSessions saves specified number of sessions`() =
        runBlocking {
            val fakeProblemGenerator =
                object : ProblemGenerator {
                    override fun generateProblems(
                        count: Int,
                        operation: MathOperation,
                        gradeLevel: dev.hossain.mathtutor.domain.model.GradeLevel,
                    ): List<MathProblem> =
                        (1..count).map { idx ->
                            MathProblem(
                                num1 = idx,
                                num2 = idx + 1,
                                operation = operation,
                                correctAnswer =
                                    idx + (idx + 1),
                            )
                        }
                }

            val fakeRepo =
                object : SessionRepository {
                    var calls = 0

                    override suspend fun saveSession(
                        session: PracticeSession,
                        operation: MathOperation,
                        durationSeconds: Long,
                        gradeLevel: Int?,
                    ): Long {
                        calls++
                        return calls.toLong()
                    }

                    override fun getAllSessions() = throw UnsupportedOperationException()

                    override fun getRecentSessions(limit: Int) = throw UnsupportedOperationException()

                    override fun getSessionsByOperation(operation: MathOperation) = throw UnsupportedOperationException()

                    override fun getOverallStats() = throw UnsupportedOperationException()

                    override fun getStatsByOperation(operation: MathOperation) = throw UnsupportedOperationException()

                    override suspend fun clearAllSessions() { /* no-op */ }
                }

            val seeder = SessionSeederImpl(fakeProblemGenerator, fakeRepo)

            val seeded =
                seeder.seedSampleSessions(
                    count = 5,
                    operation = MathOperation.ADDITION,
                    grade = GradeLevel.GRADE_1,
                    avgAccuracy = 1.0f,
                )

            assertThat(seeded).isEqualTo(5)
            assertThat(fakeRepo.calls).isEqualTo(5)
        }
}
