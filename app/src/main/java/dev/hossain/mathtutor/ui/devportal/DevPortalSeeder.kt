package dev.hossain.mathtutor.ui.devportal

import dev.hossain.mathtutor.devtools.SessionSeeder
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation

/**
 * Small helper to coordinate seeding operations for the Developer Portal.
 * Extracted to make seeding logic easier to unit test outside of Compose.
 */
class DevPortalSeeder(
    private val sessionSeeder: SessionSeeder,
) {
    suspend fun seed(
        count: Int,
        operation: MathOperation,
        grade: GradeLevel,
        avgAccuracy: Float = 0.8f,
    ): String =
        try {
            val seeded = sessionSeeder.seedSampleSessions(count, operation, grade, avgAccuracy)
            "Seeded $seeded sessions"
        } catch (e: Exception) {
            "Seed failed: ${e.message}"
        }
}
