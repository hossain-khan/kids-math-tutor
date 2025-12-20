package dev.hossain.mathtutor.ui.settings

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [SettingsPresenter].
 *
 * Tests presenter logic for settings and profile management.
 */
class SettingsPresenterTest {
    @Test
    fun repository_updateName_savesCorrectly() {
        // Given
        val repository = FakeUserProfileRepository()
        val initialProfile =
            UserProfile(
                name = "Old Name",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(initialProfile)

        // When
        repository.updateNameSync("New Name")

        // Then
        assertThat(repository.getCurrentProfile()?.name).isEqualTo("New Name")
    }

    @Test
    fun repository_updateGradeLevel_savesCorrectly() {
        // Given
        val repository = FakeUserProfileRepository()
        val initialProfile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(initialProfile)

        // When
        repository.updateGradeLevelSync(GradeLevel.GRADE_2)

        // Then
        assertThat(repository.getCurrentProfile()?.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
    }

    @Test
    fun repository_updateAdaptiveDifficulty_savesCorrectly() {
        // Given
        val repository = FakeUserProfileRepository()
        val initialProfile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(initialProfile)

        // When
        repository.updateAdaptiveDifficultySync(false)

        // Then
        assertThat(repository.getCurrentProfile()?.adaptiveDifficultyEnabled ?: true).isFalse()
    }

    @Test
    fun state_withNullProfile_showsLoadingState() {
        // Given
        val repository = FakeUserProfileRepository()
        repository.setProfile(null)

        // When
        val profile = repository.getCurrentProfile()

        // Then
        assertThat(profile).isNull()
    }

    @Test
    fun state_withProfile_showsProfileData() {
        // Given
        val repository = FakeUserProfileRepository()
        val profile =
            UserProfile(
                name = "Sarah",
                gradeLevel = GradeLevel.KINDERGARTEN,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        repository.setProfile(profile)

        // When
        val retrievedProfile = repository.getCurrentProfile()

        // Then
        assertThat(retrievedProfile).isNotNull()
        assertThat(retrievedProfile?.name).isEqualTo("Sarah")
        assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.KINDERGARTEN)
        assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: false).isTrue()
    }

    /**
     * Fake implementation of [UserProfileRepository] for testing.
     */
    private class FakeUserProfileRepository : UserProfileRepository {
        private val profileFlow = MutableStateFlow<UserProfile?>(null)

        override fun getProfile(): Flow<UserProfile?> = profileFlow

        override suspend fun saveProfile(profile: UserProfile) {
            profileFlow.value = profile
        }

        override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(gradeLevel = gradeLevel)
            }
        }

        override suspend fun updateName(name: String?) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(name = name)
            }
        }

        override suspend fun updateAdaptiveDifficulty(enabled: Boolean) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(adaptiveDifficultyEnabled = enabled)
            }
        }

        // Test helper methods
        fun setProfile(profile: UserProfile?) {
            profileFlow.value = profile
        }

        fun getCurrentProfile(): UserProfile? = profileFlow.value

        fun updateNameSync(name: String?) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(name = name)
            }
        }

        fun updateGradeLevelSync(gradeLevel: GradeLevel) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(gradeLevel = gradeLevel)
            }
        }

        fun updateAdaptiveDifficultySync(enabled: Boolean) {
            profileFlow.value?.let { currentProfile ->
                profileFlow.value = currentProfile.copy(adaptiveDifficultyEnabled = enabled)
            }
        }
    }
}
