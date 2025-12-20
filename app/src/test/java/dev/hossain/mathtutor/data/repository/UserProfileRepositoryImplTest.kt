package dev.hossain.mathtutor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UserProfileRepositoryImplTest {
    private lateinit var testContext: Context
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    private var testCounter = 0

    @Before
    fun setup() {
        testContext = ApplicationProvider.getApplicationContext()
        testCounter++

        // Clean up all DataStore files before each test
        cleanupAllDataStoreFiles()
    }

    @After
    fun cleanup() {
        // Clean up test DataStore files after each test
        cleanupAllDataStoreFiles()
    }

    private fun cleanupAllDataStoreFiles() {
        // Delete the datastore directory
        val dataStoreDir = File(testContext.filesDir, "datastore")
        if (dataStoreDir.exists()) {
            dataStoreDir.deleteRecursively()
        }

        // Delete any preferences files
        testContext.filesDir
            .listFiles { file ->
                file.name.contains("preferences") ||
                    file.name.startsWith("test_") ||
                    file.name.contains("user_preferences")
            }?.forEach {
                if (it.isDirectory) {
                    it.deleteRecursively()
                } else {
                    it.delete()
                }
            }
    }

    @Test
    fun `a_getProfile returns null when no profile exists`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val profile = repository.getProfile().first()
            assertThat(profile).isNull()
        }

    @Test
    fun `saveProfile stores profile correctly`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Test User",
                    gradeLevel = GradeLevel.GRADE_1,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                )

            repository.saveProfile(testProfile)

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile).isNotNull()
            assertThat(retrievedProfile?.name).isEqualTo("Test User")
            assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
            assertThat(retrievedProfile?.createdAt?.toEpochMilli().isEqualTo(now.toEpochMilli()))
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: false).isTrue()
        }

    @Test
    fun `saveProfile with null name stores empty string and retrieves as empty`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = null,
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    createdAt = now,
                    adaptiveDifficultyEnabled = false,
                )

            repository.saveProfile(testProfile)

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile).isNotNull()
            assertThat(retrievedProfile?.name).isEqualTo("")
            assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.KINDERGARTEN)
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: true).isFalse()
        }

    @Test
    fun `updateGradeLevel changes only grade level`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Test User",
                    gradeLevel = GradeLevel.GRADE_1,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                )

            repository.saveProfile(testProfile)
            repository.updateGradeLevel(GradeLevel.GRADE_2)

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile).isNotNull()
            assertThat(retrievedProfile?.name).isEqualTo("Test User")
            assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
            assertThat(retrievedProfile?.createdAt?.toEpochMilli().isEqualTo(now.toEpochMilli()))
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: false).isTrue()
        }

    @Test
    fun `updateName changes only name`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Old Name",
                    gradeLevel = GradeLevel.GRADE_1,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                )

            repository.saveProfile(testProfile)
            repository.updateName("New Name")

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile).isNotNull()
            assertThat(retrievedProfile?.name).isEqualTo("New Name")
            assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
            assertThat(retrievedProfile?.createdAt?.toEpochMilli().isEqualTo(now.toEpochMilli()))
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: false).isTrue()
        }

    @Test
    fun `updateName with null clears name`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Test User",
                    gradeLevel = GradeLevel.GRADE_2,
                    createdAt = now,
                    adaptiveDifficultyEnabled = false,
                )

            repository.saveProfile(testProfile)
            repository.updateName(null)

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile).isNotNull()
            assertThat(retrievedProfile?.name).isEqualTo("")
            assertThat(retrievedProfile?.gradeLevel).isEqualTo(GradeLevel.GRADE_2)
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: true).isFalse()
        }

    @Test
    fun `profile persists across multiple reads`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Persistent User",
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                )

            repository.saveProfile(testProfile)

            // Read multiple times
            val firstRead = repository.getProfile().first()
            val secondRead = repository.getProfile().first()
            val thirdRead = repository.getProfile().first()

            assertThat(secondRead?.name).isEqualTo(firstRead?.name)
            assertThat(thirdRead?.name).isEqualTo(secondRead?.name)
            assertThat(thirdRead?.name).isEqualTo("Persistent User")
        }

    @Test
    fun `adaptive difficulty defaults to true when not set`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()
            val testProfile =
                UserProfile(
                    name = "Test User",
                    gradeLevel = GradeLevel.GRADE_1,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                )

            repository.saveProfile(testProfile)

            val retrievedProfile = repository.getProfile().first()
            assertThat(retrievedProfile?.adaptiveDifficultyEnabled ?: false).isTrue()
        }

    @Test
    fun `all three grade levels can be saved and retrieved`() =
        testScope.runTest {
            val repository = UserProfileRepositoryImpl(testContext)
            val now = Instant.now()

            // Test Kindergarten
            repository.saveProfile(
                UserProfile(
                    name = "K Student",
                    gradeLevel = GradeLevel.KINDERGARTEN,
                    createdAt = now,
                    adaptiveDifficultyEnabled = true,
                ),
            )
            assertThat(
                repository
                    .getProfile()
                    .isEqualTo(GradeLevel.KINDERGARTEN)
                    .first()
                    ?.gradeLevel,
            )

            // Test Grade 1
            repository.updateGradeLevel(GradeLevel.GRADE_1)
            assertThat(
                repository
                    .getProfile()
                    .isEqualTo(GradeLevel.GRADE_1)
                    .first()
                    ?.gradeLevel,
            )

            // Test Grade 2
            repository.updateGradeLevel(GradeLevel.GRADE_2)
            assertThat(
                repository
                    .getProfile()
                    .isEqualTo(GradeLevel.GRADE_2)
                    .first()
                    ?.gradeLevel,
            )
        }
}
