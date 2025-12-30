package dev.hossain.mathtutor.data.local.entity.goals

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity that links practice sessions to goal components.
 * Created when a practice session is completed as part of an active goal,
 * allowing the system to track which sessions contribute to goal progress.
 *
 * @property id Unique identifier for this link
 * @property sessionId Reference to the practice session (from practice_sessions table)
 * @property activeGoalId Reference to the active goal (from active_goals table)
 * @property componentIndex Which component of the goal this session satisfies
 */
@Entity(
    tableName = "practice_session_to_goal",
    indices = [
        Index("sessionId"),
        Index("activeGoalId"),
    ],
)
data class PracticeSessionToGoalEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val activeGoalId: String,
    val componentIndex: Int, // Which component this session fulfills
)
