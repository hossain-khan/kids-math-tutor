package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation

/**
 * Mapper for converting between Badge domain model and BadgeEntity database model.
 * Handles serialization and deserialization of badge requirements using simple string formatting.
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
     * Serializes a badge requirement to type and data strings using simple key=value format.
     *
     * @param requirement The badge requirement to serialize
     * @return Pair of requirement type name and data string
     */
    private fun serializeRequirement(requirement: BadgeRequirement): Pair<String, String> {
        val type = requirement::class.simpleName ?: "Unknown"
        val data =
            when (requirement) {
                is BadgeRequirement.ProblemCount -> "count=${requirement.count}"
                is BadgeRequirement.OperationCount -> "operation=${requirement.operation.name},count=${requirement.count}"
                is BadgeRequirement.ConsecutiveCorrect -> "count=${requirement.count}"
                is BadgeRequirement.SessionAccuracy -> "percentage=${requirement.percentage},sessionCount=${requirement.sessionCount}"
                is BadgeRequirement.DailyStreak -> "days=${requirement.days}"
                is BadgeRequirement.ProblemSpeed -> "maxSeconds=${requirement.maxSeconds}"
                is BadgeRequirement.MixedSessions -> "count=${requirement.count}"
            }
        return Pair(type, data)
    }

    /**
     * Deserializes a badge requirement from type and data strings.
     *
     * @param type The requirement type name
     * @param data String containing requirement parameters in key=value format
     * @return Deserialized badge requirement
     * @throws IllegalArgumentException if the requirement type is unknown or data is malformed
     */
    private fun deserializeRequirement(
        type: String,
        data: String,
    ): BadgeRequirement {
        val params =
            data.split(",").associate {
                val parts = it.split("=")
                if (parts.size != 2) {
                    throw IllegalArgumentException("Malformed requirement data: $it")
                }
                parts[0] to parts[1]
            }

        return when (type) {
            "ProblemCount" -> {
                BadgeRequirement.ProblemCount(count = params["count"]!!.toInt())
            }

            "OperationCount" -> {
                BadgeRequirement.OperationCount(
                    operation = MathOperation.valueOf(params["operation"]!!),
                    count = params["count"]!!.toInt(),
                )
            }

            "ConsecutiveCorrect" -> {
                BadgeRequirement.ConsecutiveCorrect(count = params["count"]!!.toInt())
            }

            "SessionAccuracy" -> {
                BadgeRequirement.SessionAccuracy(
                    percentage = params["percentage"]!!.toFloat(),
                    sessionCount = params["sessionCount"]!!.toInt(),
                )
            }

            "DailyStreak" -> {
                BadgeRequirement.DailyStreak(days = params["days"]!!.toInt())
            }

            "ProblemSpeed" -> {
                BadgeRequirement.ProblemSpeed(maxSeconds = params["maxSeconds"]!!.toInt())
            }

            "MixedSessions" -> {
                BadgeRequirement.MixedSessions(count = params["count"]!!.toInt())
            }

            else -> {
                throw IllegalArgumentException("Unknown requirement type: $type")
            }
        }
    }
}
