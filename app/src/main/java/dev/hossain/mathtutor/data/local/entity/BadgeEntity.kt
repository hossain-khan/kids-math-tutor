package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.BadgeCategory
import java.time.Instant

/**
 * Room entity representing a badge in the database.
 * Stores badge information and unlock status.
 *
 * @property id Unique identifier for the badge (primary key)
 * @property name Display name of the badge
 * @property description Detailed description of what the badge represents
 * @property icon Emoji or icon representation of the badge
 * @property category The category this badge belongs to
 * @property requirementType String representing the type of requirement (e.g., "ProblemCount")
 * @property requirementData JSON string containing requirement parameters
 * @property unlockedAt Timestamp when the badge was unlocked, null if still locked
 */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: BadgeCategory,
    val requirementType: String,
    val requirementData: String,
    val unlockedAt: Instant? = null,
)
