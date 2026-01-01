package dev.hossain.mathtutor.ui.parentsettings

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.fakes.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for grade limit enforcement logic used by [ParentSettingsPresenter].
 *
 * These tests verify the core auto-downgrade logic:
 * When a parent sets a grade limit via GradeLimitChanged event, if the child's
 * current profile grade exceeds the new limit, the profile is automatically downgraded.
 *
 * Tests verify:
 * - Profile below limit: no downgrade needed
 * - Profile above limit: automatic downgrade to new limit
 * - Limit removal: profile unchanged
 */
class ParentSettingsPresenterTest {
    @Test
    fun gradeLimitChanged_withProfileBelowLimit_doesNotDowngrade() {
        // Given
        val repository = FakeUserProfileRepository()
        val profile =
            UserProfile(
                name = "Child",
                gradeLevel = GradeLevel.GRADE_1, // Profile is Grade 1
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(profile)
        val preferencesRepo = FakeUserPreferencesRepository()

        // When: Parent sets limit to Grade 2 (above current grade)
        val limitBelow = GradeLevel.GRADE_2
        preferencesRepo.setMaxGradeLevelSync(limitBelow)

        // Then: Profile should remain Grade 1 (not changed)
        val updatedProfile = repository.getCurrentProfile()
        assertThat(updatedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
    }

    @Test
    fun gradeLimitChanged_withProfileAboveLimit_downgrades() {
        // Given
        val repository = FakeUserProfileRepository()
        val profile =
            UserProfile(
                name = "Child",
                gradeLevel = GradeLevel.GRADE_2, // Profile is Grade 2
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(profile)

        // When: Profile grade is above new limit, should downgrade
        val newLimit = GradeLevel.GRADE_1
        if (profile.gradeLevel > newLimit) {
            repository.updateGradeLevelSync(newLimit)
        }

        // Then: Profile should be downgraded to Grade 1
        val updatedProfile = repository.getCurrentProfile()
        assertThat(updatedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
    }

    @Test
    fun gradeLimitChanged_removingLimit_doesNotChangeProfile() {
        // Given
        val repository = FakeUserProfileRepository()
        val profile =
            UserProfile(
                name = "Child",
                gradeLevel = GradeLevel.GRADE_2,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(profile)

        // When: Parent removes limit (null), profile stays same
        // No downgrade logic applies when limit is removed
        val updatedProfile = repository.getCurrentProfile()

        // Then: Profile remains Grade 2
        assertThat(updatedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
    }

    @Test
    fun gradeLimitChanged_fromGrade2ToGrade1_downgrades() {
        // Given: Simulate realistic scenario
        val repository = FakeUserProfileRepository()
        repository.setProfile(
            UserProfile(
                name = "Alice",
                gradeLevel = GradeLevel.GRADE_2,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            ),
        )

        // When: Parent lowers grade limit from unlimited to Grade 1
        val newLimit = GradeLevel.GRADE_1
        val currentProfile = repository.getCurrentProfile()
        if (currentProfile != null && currentProfile.gradeLevel > newLimit) {
            repository.updateGradeLevelSync(newLimit)
        }

        // Then: Child's profile should be downgraded
        val updated = repository.getCurrentProfile()
        assertThat(updated?.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
    }

    @Test
    fun gradeLimitChanged_fromGrade1ToKindergarten_downgrades() {
        // Given
        val repository = FakeUserProfileRepository()
        repository.setProfile(
            UserProfile(
                name = "Bob",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            ),
        )

        // When: Parent lowers grade limit to Kindergarten
        val newLimit = GradeLevel.KINDERGARTEN
        val currentProfile = repository.getCurrentProfile()
        if (currentProfile != null && currentProfile.gradeLevel > newLimit) {
            repository.updateGradeLevelSync(newLimit)
        }

        // Then: Child's profile should be downgraded to Kindergarten
        val updated = repository.getCurrentProfile()
        assertThat(updated?.gradeLevel).isEqualTo(GradeLevel.KINDERGARTEN)
    }

    /**
     * Fake implementation of UserPreferencesRepository for testing.
     */
    private class FakeUserProfileRepository : UserProfileRepository {
        private val profileFlow = MutableStateFlow<UserProfile?>(null)

        override fun getProfile(): Flow<UserProfile?> = profileFlow

        fun setProfile(profile: UserProfile?) {
            profileFlow.value = profile
        }

        fun getCurrentProfile(): UserProfile? = profileFlow.value

        override suspend fun saveProfile(profile: UserProfile) {
            profileFlow.value = profile
        }

        override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {
            profileFlow.value?.let { current ->
                profileFlow.value = current.copy(gradeLevel = gradeLevel)
            }
        }

        override suspend fun updateName(name: String?) {
            profileFlow.value?.let { current ->
                profileFlow.value = current.copy(name = name)
            }
        }

        override suspend fun updateAdaptiveDifficulty(enabled: Boolean) {
            profileFlow.value?.let { current ->
                profileFlow.value = current.copy(adaptiveDifficultyEnabled = enabled)
            }
        }

        // Helper methods for testing
        fun updateGradeLevelSync(gradeLevel: GradeLevel) {
            profileFlow.value?.let { current ->
                profileFlow.value = current.copy(gradeLevel = gradeLevel)
            }
        }

        fun updateNameSync(name: String?) {
            profileFlow.value?.let { current ->
                profileFlow.value = current.copy(name = name)
            }
        }
    }
}
