package dev.hossain.mathtutor.data.local

import androidx.room.TypeConverter
import dev.hossain.mathtutor.domain.model.MathOperation
import java.time.Instant

/**
 * Room type converters for custom types that need to be stored in the database.
 * Converts between Room-supported types (String, Long) and domain types (MathOperation, Instant).
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
}
