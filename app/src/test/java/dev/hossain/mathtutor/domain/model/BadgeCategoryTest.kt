package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BadgeCategoryTest {
    @Test
    fun `BadgeCategory has correct values`() {
        assertEquals(6, BadgeCategory.entries.size)
    }

    @Test
    fun `BadgeCategory values are correctly named`() {
        assertEquals(BadgeCategory.GETTING_STARTED, BadgeCategory.valueOf("GETTING_STARTED"))
        assertEquals(BadgeCategory.VOLUME, BadgeCategory.valueOf("VOLUME"))
        assertEquals(BadgeCategory.OPERATION_MASTERY, BadgeCategory.valueOf("OPERATION_MASTERY"))
        assertEquals(BadgeCategory.SPEED_ACCURACY, BadgeCategory.valueOf("SPEED_ACCURACY"))
        assertEquals(BadgeCategory.STREAK, BadgeCategory.valueOf("STREAK"))
        assertEquals(BadgeCategory.GAMES, BadgeCategory.valueOf("GAMES"))
    }
}
