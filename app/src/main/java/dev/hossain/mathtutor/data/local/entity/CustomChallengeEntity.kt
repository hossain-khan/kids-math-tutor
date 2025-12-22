package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.ChallengeType
import java.time.Instant

/**
 * Room entity representing a custom challenge created by parents.
 * Stores the challenge metadata including title, type, and archival status.
 *
 * @property id Unique identifier for this challenge
 * @property title The title of the challenge
 * @property subtitle Optional subtitle or description
 * @property type The type of challenge (GENERATED or EXPLICIT)
 * @property createdAt When the challenge was created (stored as epoch milliseconds)
 * @property isArchived Whether the challenge is archived
 */
@Entity(tableName = "custom_challenges")
data class CustomChallengeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String?,
    val type: ChallengeType,
    val createdAt: Instant,
    val isArchived: Boolean,
)
