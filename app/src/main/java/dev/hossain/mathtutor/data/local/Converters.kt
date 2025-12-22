package dev.hossain.mathtutor.data.local

import androidx.room.TypeConverter
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import java.time.Instant
import java.time.LocalDate

/**
 * Room type converters for custom types that need to be stored in the database.
 * Converts between Room-supported types (String, Long) and domain types (MathOperation, BadgeCategory, ChallengeType, Instant, LocalDate).
 */
class Converters {
    /**
     * Converts MathOperation enum to String for database storage.
     *
     * @param operation The MathOperation enum value
     * @return String representation of the operation name
     */
    @TypeConverter
    fun fromMathOperation(operation: MathOperation): String = operation.name

    /**
     * Converts String from database back to MathOperation enum.
     *
     * @param value String representation of the operation name
     * @return MathOperation enum value
     */
    @TypeConverter
    fun toMathOperation(value: String): MathOperation = MathOperation.valueOf(value)

    /**
     * Converts BadgeCategory enum to String for database storage.
     *
     * @param category The BadgeCategory enum value
     * @return String representation of the category name
     */
    @TypeConverter
    fun fromBadgeCategory(category: BadgeCategory): String = category.name

    /**
     * Converts String from database back to BadgeCategory enum.
     *
     * @param value String representation of the category name
     * @return BadgeCategory enum value
     */
    @TypeConverter
    fun toBadgeCategory(value: String): BadgeCategory = BadgeCategory.valueOf(value)

    /**
     * Converts Instant timestamp to Long (epoch milliseconds) for database storage.
     *
     * @param instant The Instant timestamp
     * @return Long representation as epoch milliseconds, or null if input is null
     */
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    /**
     * Converts Long (epoch milliseconds) from database back to Instant.
     *
     * @param value Long representation as epoch milliseconds
     * @return Instant timestamp, or null if input is null
     */
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    /**
     * Converts LocalDate to Long (epoch days) for database storage.
     *
     * @param date The LocalDate to convert
     * @return Long representation as epoch days, or null if input is null
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    /**
     * Converts Long (epoch days) from database back to LocalDate.
     *
     * @param value Long representation as epoch days
     * @return LocalDate, or null if input is null
     */
    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    /**
     * Converts GradeLevel enum to String for database storage.
     *
     * @param gradeLevel The GradeLevel enum value
     * @return String representation of the grade level name
     */
    @TypeConverter
    fun fromGradeLevel(gradeLevel: GradeLevel): String = gradeLevel.name

    /**
     * Converts String from database back to GradeLevel enum.
     *
     * @param value String representation of the grade level name
     * @return GradeLevel enum value
     */
    @TypeConverter
    fun toGradeLevel(value: String): GradeLevel = GradeLevel.valueOf(value)

    /**
     * Converts ChallengeType enum to String for database storage.
     *
     * @param type The ChallengeType enum value
     * @return String representation of the challenge type name
     */
    @TypeConverter
    fun fromChallengeType(type: ChallengeType): String = type.name

    /**
     * Converts String from database back to ChallengeType enum.
     *
     * @param value String representation of the challenge type name
     * @return ChallengeType enum value
     */
    @TypeConverter
    fun toChallengeType(value: String): ChallengeType = ChallengeType.valueOf(value)
}
