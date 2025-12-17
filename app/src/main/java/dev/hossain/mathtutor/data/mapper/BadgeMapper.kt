package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import org.json.JSONObject

/**
 * Mapper for converting between Badge domain model and BadgeEntity database model.
 * Handles serialization and deserialization of badge requirements.
 */
object BadgeMapper {
    /**
     * Converts a database [BadgeEntity] to a domain [Badge].
     *
     * @param entity The badge entity from the database
     * @return Domain badge model with deserialized requirement
     */
    fun toDomain(entity: BadgeEntity): Badge {
        val requirement = deserializeRequirement(entity.requirementType, entity.requirementData)
        return Badge(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            icon = entity.icon,
            category = entity.category,
            requirement = requirement,
            unlockedAt = entity.unlockedAt,
        )
    }

    /**
     * Converts a domain [Badge] to a database [BadgeEntity].
     *
     * @param badge The domain badge to convert
     * @return Database entity with serialized requirement
     */
    fun toEntity(badge: Badge): BadgeEntity {
        val (requirementType, requirementData) = serializeRequirement(badge.requirement)
        return BadgeEntity(
            id = badge.id,
            name = badge.name,
            description = badge.description,
            icon = badge.icon,
            category = badge.category,
            requirementType = requirementType,
            requirementData = requirementData,
            unlockedAt = badge.unlockedAt,
        )
    }

    /**
     * Serializes a badge requirement to type and JSON data strings.
     *
     * @param requirement The badge requirement to serialize
     * @return Pair of requirement type name and JSON data string
     */
    private fun serializeRequirement(requirement: BadgeRequirement): Pair<String, String> {
        val type = requirement::class.simpleName ?: "Unknown"
        val data =
            when (requirement) {
                is BadgeRequirement.ProblemCount -> {
                    JSONObject()
                        .apply {
                            put("count", requirement.count)
                        }.toString()
                }

                is BadgeRequirement.OperationCount -> {
                    JSONObject()
                        .apply {
                            put("operation", requirement.operation.name)
                            put("count", requirement.count)
                        }.toString()
                }

                is BadgeRequirement.ConsecutiveCorrect -> {
                    JSONObject()
                        .apply {
                            put("count", requirement.count)
                        }.toString()
                }

                is BadgeRequirement.SessionAccuracy -> {
                    JSONObject()
                        .apply {
                            put("percentage", requirement.percentage)
                            put("sessionCount", requirement.sessionCount)
                        }.toString()
                }

                is BadgeRequirement.DailyStreak -> {
                    JSONObject()
                        .apply {
                            put("days", requirement.days)
                        }.toString()
                }

                is BadgeRequirement.ProblemSpeed -> {
                    JSONObject()
                        .apply {
                            put("maxSeconds", requirement.maxSeconds)
                        }.toString()
                }

                is BadgeRequirement.MixedSessions -> {
                    JSONObject()
                        .apply {
                            put("count", requirement.count)
                        }.toString()
                }
            }
        return Pair(type, data)
    }

    /**
     * Deserializes a badge requirement from type and JSON data strings.
     *
     * @param type The requirement type name
     * @param data JSON string containing requirement parameters
     * @return Deserialized badge requirement
     * @throws IllegalArgumentException if the requirement type is unknown
     */
    private fun deserializeRequirement(
        type: String,
        data: String,
    ): BadgeRequirement {
        val json = JSONObject(data)
        return when (type) {
            "ProblemCount" -> {
                BadgeRequirement.ProblemCount(count = json.getInt("count"))
            }

            "OperationCount" -> {
                BadgeRequirement.OperationCount(
                    operation = MathOperation.valueOf(json.getString("operation")),
                    count = json.getInt("count"),
                )
            }

            "ConsecutiveCorrect" -> {
                BadgeRequirement.ConsecutiveCorrect(count = json.getInt("count"))
            }

            "SessionAccuracy" -> {
                BadgeRequirement.SessionAccuracy(
                    percentage = json.getDouble("percentage").toFloat(),
                    sessionCount = json.getInt("sessionCount"),
                )
            }

            "DailyStreak" -> {
                BadgeRequirement.DailyStreak(days = json.getInt("days"))
            }

            "ProblemSpeed" -> {
                BadgeRequirement.ProblemSpeed(maxSeconds = json.getInt("maxSeconds"))
            }

            "MixedSessions" -> {
                BadgeRequirement.MixedSessions(count = json.getInt("count"))
            }

            else -> {
                throw IllegalArgumentException("Unknown requirement type: $type")
            }
        }
    }
}
