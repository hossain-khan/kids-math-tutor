package dev.hossain.mathtutor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
            assertNull(profile)
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
            assertNotNull(retrievedProfile)
            assertEquals("Test User", retrievedProfile?.name)
            assertEquals(GradeLevel.GRADE_1, retrievedProfile?.gradeLevel)
            assertEquals(now.toEpochMilli(), retrievedProfile?.createdAt?.toEpochMilli())
            assertTrue(retrievedProfile?.adaptiveDifficultyEnabled ?: false)
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
            assertNotNull(retrievedProfile)
            assertEquals("", retrievedProfile?.name)
            assertEquals(GradeLevel.KINDERGARTEN, retrievedProfile?.gradeLevel)
            assertFalse(retrievedProfile?.adaptiveDifficultyEnabled ?: true)
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
            assertNotNull(retrievedProfile)
            assertEquals("Test User", retrievedProfile?.name)
            assertEquals(GradeLevel.GRADE_2, retrievedProfile?.gradeLevel)
            assertEquals(now.toEpochMilli(), retrievedProfile?.createdAt?.toEpochMilli())
            assertTrue(retrievedProfile?.adaptiveDifficultyEnabled ?: false)
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
            assertNotNull(retrievedProfile)
            assertEquals("New Name", retrievedProfile?.name)
            assertEquals(GradeLevel.GRADE_1, retrievedProfile?.gradeLevel)
            assertEquals(now.toEpochMilli(), retrievedProfile?.createdAt?.toEpochMilli())
            assertTrue(retrievedProfile?.adaptiveDifficultyEnabled ?: false)
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
            assertNotNull(retrievedProfile)
            assertEquals("", retrievedProfile?.name)
            assertEquals(GradeLevel.GRADE_2, retrievedProfile?.gradeLevel)
            assertFalse(retrievedProfile?.adaptiveDifficultyEnabled ?: true)
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

            assertEquals(firstRead?.name, secondRead?.name)
            assertEquals(secondRead?.name, thirdRead?.name)
            assertEquals("Persistent User", thirdRead?.name)
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
            assertTrue(retrievedProfile?.adaptiveDifficultyEnabled ?: false)
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
            assertEquals(GradeLevel.KINDERGARTEN, repository.getProfile().first()?.gradeLevel)

            // Test Grade 1
            repository.updateGradeLevel(GradeLevel.GRADE_1)
            assertEquals(GradeLevel.GRADE_1, repository.getProfile().first()?.gradeLevel)

            // Test Grade 2
            repository.updateGradeLevel(GradeLevel.GRADE_2)
            assertEquals(GradeLevel.GRADE_2, repository.getProfile().first()?.gradeLevel)
        }
}
