package dev.hossain.mathtutor.data.local.converter

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.ComponentResult
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.SessionMetadata
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import java.time.Instant

class GoalsConverterTest {
    private val converter = GoalsConverter()

    @Test
    fun `fromGoalComponentList and toGoalComponentList roundtrip correctly`() {
        val components =
            listOf(
                GoalComponent.OperationBased(MathOperation.ADDITION, 2),
                GoalComponent.OperationBased(MathOperation.SUBTRACTION, 3),
                GoalComponent.CustomChallengeBased("challenge1", "Worksheet 1", 1),
            )

        val json = converter.fromGoalComponentList(components)
        val deserialized = converter.toGoalComponentList(json)

        assertEquals(3, deserialized.size)
        assertEquals(components[0], deserialized[0])
        assertEquals(components[1], deserialized[1])
        assertEquals(components[2], deserialized[2])
    }

    @Test
    fun `fromGoalComponentList handles empty list`() {
        val components = emptyList<GoalComponent>()
        val json = converter.fromGoalComponentList(components)
        val deserialized = converter.toGoalComponentList(json)

        assertEquals(0, deserialized.size)
    }

    @Test
    fun `fromComponentProgressList and toComponentProgressList roundtrip correctly`() {
        val progress =
            listOf(
                ComponentProgress(
                    componentIndex = 0,
                    completedSessions = 1,
                    totalSessions = 2,
                    accuracy = 90f,
                    totalTimeSeconds = 600L,
                    sessionResults = emptyList(),
                ),
                ComponentProgress(
                    componentIndex = 1,
                    completedSessions = 0,
                    totalSessions = 3,
                    accuracy = 0f,
                    totalTimeSeconds = 0L,
                    sessionResults = emptyList(),
                ),
            )

        val json = converter.fromComponentProgressList(progress)
        val deserialized = converter.toComponentProgressList(json)

        assertEquals(2, deserialized.size)
        assertEquals(progress[0], deserialized[0])
        assertEquals(progress[1], deserialized[1])
    }

    @Test
    fun `fromComponentResultList and toComponentResultList roundtrip correctly`() {
        val results =
            listOf(
                ComponentResult(
                    0,
                    GoalComponent.OperationBased(MathOperation.ADDITION, 2),
                    2,
                    2,
                    95f,
                    500L,
                ),
                ComponentResult(
                    1,
                    GoalComponent.OperationBased(MathOperation.SUBTRACTION, 1),
                    1,
                    1,
                    85f,
                    300L,
                ),
            )

        val json = converter.fromComponentResultList(results)
        val deserialized = converter.toComponentResultList(json)

        assertEquals(2, deserialized.size)
        assertEquals(results[0], deserialized[0])
        assertEquals(results[1], deserialized[1])
    }

    @Test
    fun `handles nested structures with custom challenges`() {
        val component = GoalComponent.CustomChallengeBased("challenge-id", "Custom Worksheet", 5)
        val components = listOf(component)

        val json = converter.fromGoalComponentList(components)
        val deserialized = converter.toGoalComponentList(json)

        assertNotNull(deserialized[0])
        val customComponent = deserialized[0] as GoalComponent.CustomChallengeBased
        assertEquals("challenge-id", customComponent.challengeId)
        assertEquals("Custom Worksheet", customComponent.challengeTitle)
        assertEquals(5, customComponent.sessionCount)
    }
}
