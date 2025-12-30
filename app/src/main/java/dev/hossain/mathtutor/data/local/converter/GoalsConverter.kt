package dev.hossain.mathtutor.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.ComponentResult
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.SessionMetadata
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Room type converters for Goals feature models.
 * Handles serialization/deserialization of complex goal-related types to/from JSON.
 *
 * Uses kotlinx.serialization for safe JSON conversion.
 */
@ProvidedTypeConverter
class GoalsConverter {
    /**
     * Converts a list of GoalComponent objects to JSON string.
     *
     * @param components The list of goal components
     * @return JSON string representation
     */
    @TypeConverter
    fun fromGoalComponentList(components: List<GoalComponent>): String =
        Json.encodeToString(ListSerializer(GoalComponent.serializer()), components)

    /**
     * Converts JSON string to a list of GoalComponent objects.
     *
     * @param json JSON string representation
     * @return Deserialized list of goal components
     */
    @TypeConverter
    fun toGoalComponentList(json: String): List<GoalComponent> = Json.decodeFromString(ListSerializer(GoalComponent.serializer()), json)

    /**
     * Converts a list of ComponentProgress objects to JSON string.
     *
     * @param progress The list of component progress objects
     * @return JSON string representation
     */
    @TypeConverter
    fun fromComponentProgressList(progress: List<ComponentProgress>): String =
        Json.encodeToString(ListSerializer(ComponentProgress.serializer()), progress)

    /**
     * Converts JSON string to a list of ComponentProgress objects.
     *
     * @param json JSON string representation
     * @return Deserialized list of component progress objects
     */
    @TypeConverter
    fun toComponentProgressList(json: String): List<ComponentProgress> =
        Json.decodeFromString(ListSerializer(ComponentProgress.serializer()), json)

    /**
     * Converts a list of ComponentResult objects to JSON string.
     *
     * @param results The list of component result objects
     * @return JSON string representation
     */
    @TypeConverter
    fun fromComponentResultList(results: List<ComponentResult>): String =
        Json.encodeToString(ListSerializer(ComponentResult.serializer()), results)

    /**
     * Converts JSON string to a list of ComponentResult objects.
     *
     * @param json JSON string representation
     * @return Deserialized list of component result objects
     */
    @TypeConverter
    fun toComponentResultList(json: String): List<ComponentResult> =
        Json.decodeFromString(ListSerializer(ComponentResult.serializer()), json)

    /**
     * Converts a list of SessionMetadata objects to JSON string.
     *
     * @param sessions The list of session metadata objects
     * @return JSON string representation
     */
    @TypeConverter
    fun fromSessionMetadataList(sessions: List<SessionMetadata>): String =
        Json.encodeToString(ListSerializer(SessionMetadata.serializer()), sessions)

    /**
     * Converts JSON string to a list of SessionMetadata objects.
     *
     * @param json JSON string representation
     * @return Deserialized list of session metadata objects
     */
    @TypeConverter
    fun toSessionMetadataList(json: String): List<SessionMetadata> =
        Json.decodeFromString(ListSerializer(SessionMetadata.serializer()), json)
}
