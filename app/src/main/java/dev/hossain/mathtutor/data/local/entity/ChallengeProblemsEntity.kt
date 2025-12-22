package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.MathOperation

/**
 * Room entity representing a math problem within a custom challenge.
 * Stores individual problem data with a foreign key relationship to the parent challenge.
 *
 * @property id Unique identifier for this problem
 * @property challengeId Reference to the parent custom challenge
 * @property operand1 First operand in the math problem
 * @property operand2 Second operand in the math problem
 * @property operation The mathematical operation to perform
 * @property answer The correct answer to the problem
 * @property orderIndex The order position of this problem in the challenge (0-based)
 */
@Entity(
    tableName = "challenge_problems",
    foreignKeys = [
        ForeignKey(
            entity = CustomChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["challengeId"])],
)
data class ChallengeProblemsEntity(
    @PrimaryKey
    val id: String,
    val challengeId: String,
    val operand1: Int,
    val operand2: Int,
    val operation: MathOperation,
    val answer: Int,
    val orderIndex: Int,
)
