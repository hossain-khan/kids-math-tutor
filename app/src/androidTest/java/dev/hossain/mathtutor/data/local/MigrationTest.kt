package dev.hossain.mathtutor.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for Room database migrations.
 * Ensures that database schema changes preserve existing data and maintain integrity.
 *
 * These tests verify:
 * 1. Database can be created from scratch (version 1)
 * 2. Future migrations preserve data (when new versions are added)
 * 3. All tables and indices are created correctly
 *
 * When adding new database versions:
 * 1. Increment version number in MathDatabase
 * 2. Create a Migration object in DatabaseModule
 * 3. Add migration test here following the pattern below
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    /**
     * MigrationTestHelper provided by Room for testing database migrations.
     * Helps verify that migrations correctly transform the database schema.
     */
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MathDatabase::class.java,
            emptyList(), // No migrations yet since we're at version 1
            FrameworkSQLiteOpenHelperFactory(),
        )

    /**
     * Tests that database version 1 can be created from scratch.
     * Verifies all tables exist with correct schema.
     */
    @Test
    @Throws(IOException::class)
    fun testDatabaseCreation_version1() {
        // Create database at version 1
        val db = helper.createDatabase(TEST_DB, 1)

        // Verify all tables exist
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")

        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val tableName = cursor.getString(0)
            // Exclude internal SQLite tables
            if (!tableName.startsWith("sqlite_") && !tableName.startsWith("android_") && !tableName.startsWith("room_")) {
                tables.add(tableName)
            }
        }
        cursor.close()

        // Verify all expected tables exist
        val expectedTables =
            listOf(
                "active_goals",
                "badges",
                "challenge_practice_sessions",
                "challenge_problems",
                "custom_challenges",
                "game_sessions",
                "goal_history",
                "goals_catalog",
                "performance_records",
                "practice_session_to_goal",
                "practice_sessions",
                "streaks",
            )

        assertEquals("All tables should be created", expectedTables.size, tables.size)
        expectedTables.forEach { expectedTable ->
            assert(tables.contains(expectedTable)) {
                "Table $expectedTable should exist in database. Found tables: $tables"
            }
        }

        db.close()
    }

    /**
     * Tests that goals_catalog table has correct schema.
     * Verifies columns, types, and constraints.
     */
    @Test
    @Throws(IOException::class)
    fun testGoalsCatalogTable_schema() {
        val db = helper.createDatabase(TEST_DB, 1)

        // Query table info
        val cursor = db.query("PRAGMA table_info(goals_catalog)")

        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        cursor.close()

        // Verify columns
        assertEquals("TEXT", columns["id"])
        assertEquals("TEXT", columns["title"])
        assertEquals("TEXT", columns["description"])
        assertEquals("TEXT", columns["components"])
        assertEquals("INTEGER", columns["createdAt"])
        assertEquals("INTEGER", columns["isArchived"])

        db.close()
    }

    /**
     * Tests that active_goals table has correct schema.
     */
    @Test
    @Throws(IOException::class)
    fun testActiveGoalsTable_schema() {
        val db = helper.createDatabase(TEST_DB, 1)

        val cursor = db.query("PRAGMA table_info(active_goals)")

        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        cursor.close()

        // Verify columns
        assertEquals("TEXT", columns["id"])
        assertEquals("TEXT", columns["goalId"])
        assertEquals("INTEGER", columns["activatedAt"])
        assertEquals("INTEGER", columns["currentComponentIndex"])
        assertEquals("TEXT", columns["componentProgress"])

        db.close()
    }

    /**
     * Tests that goal_history table has correct schema with indices.
     */
    @Test
    @Throws(IOException::class)
    fun testGoalHistoryTable_schema() {
        val db = helper.createDatabase(TEST_DB, 1)

        val cursor = db.query("PRAGMA table_info(goal_history)")

        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        cursor.close()

        // Verify columns
        assertEquals("TEXT", columns["id"])
        assertEquals("TEXT", columns["goalId"])
        assertEquals("TEXT", columns["goalTitle"])
        assertEquals("INTEGER", columns["completedAt"])
        assertEquals("INTEGER", columns["totalTimeSeconds"])
        assertEquals("REAL", columns["overallAccuracy"])
        assertEquals("TEXT", columns["componentResults"])

        // Verify indices exist
        val indexCursor = db.query("PRAGMA index_list(goal_history)")
        val indices = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indices.add(indexCursor.getString(indexCursor.getColumnIndexOrThrow("name")))
        }
        indexCursor.close()

        // Should have 3 indices (on goalId, completedAt, and composite goalId+completedAt)
        assertEquals("Should have 3 indices on goal_history", 3, indices.size)

        db.close()
    }

    /**
     * Tests that practice_session_to_goal table has correct schema.
     */
    @Test
    @Throws(IOException::class)
    fun testPracticeSessionToGoalTable_schema() {
        val db = helper.createDatabase(TEST_DB, 1)

        val cursor = db.query("PRAGMA table_info(practice_session_to_goal)")

        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        cursor.close()

        // Verify columns
        assertEquals("TEXT", columns["id"])
        assertEquals("TEXT", columns["sessionId"])
        assertEquals("TEXT", columns["activeGoalId"])
        assertEquals("INTEGER", columns["componentIndex"])

        db.close()
    }

    /**
     * Tests that database can be opened and closed without errors.
     * Verifies DAOs can be accessed after database creation.
     */
    @Test
    fun testDatabase_canBeOpenedAndUsed() {
        // Create database using Room builder (not helper)
        val database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MathDatabase::class.java,
                ).build()

        // Verify all DAOs can be accessed
        assertNotNull(database.sessionDao())
        assertNotNull(database.badgeDao())
        assertNotNull(database.streakDao())
        assertNotNull(database.performanceDao())
        assertNotNull(database.gameSessionDao())
        assertNotNull(database.customChallengeDao())
        assertNotNull(database.goalsDao())
        assertNotNull(database.activeGoalDao())
        assertNotNull(database.goalHistoryDao())
        assertNotNull(database.practiceSessionToGoalDao())

        database.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
