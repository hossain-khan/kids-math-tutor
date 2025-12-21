package dev.hossain.mathtutor.ui.devportal

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.devtools.SessionSeeder
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DevPortalSeederTest {
    @Test
    fun `seed returns success message when sessionSeeder succeeds`() =
        runBlocking {
            val fakeSeeder =
                object : SessionSeeder {
                    override suspend fun seedSampleSessions(
                        count: Int,
                        operation: MathOperation,
                        grade: GradeLevel,
                        avgAccuracy: Float,
                    ): Int {
                        // simulate seeding
                        return count.coerceAtLeast(0)
                    }
                }

            val helper = DevPortalSeeder(fakeSeeder)
            val msg = helper.seed(count = 5, operation = MathOperation.ADDITION, grade = GradeLevel.GRADE_1)
            assertThat(msg).isEqualTo("Seeded 5 sessions")
        }

    @Test
    fun `seed returns failure message when sessionSeeder throws`() =
        runBlocking {
            val fakeSeeder =
                object : SessionSeeder {
                    override suspend fun seedSampleSessions(
                        count: Int,
                        operation: MathOperation,
                        grade: GradeLevel,
                        avgAccuracy: Float,
                    ): Int = throw IllegalStateException("boom")
                }

            val helper = DevPortalSeeder(fakeSeeder)
            val msg = helper.seed(count = 3, operation = MathOperation.ADDITION, grade = GradeLevel.GRADE_1)
            assertThat(msg).isEqualTo("Seed failed: boom")
        }
}
