package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeCategoryTest {
    @Test
    fun `BadgeCategory has correct values`() {
        assertThat(BadgeCategory.entries.size).isEqualTo(6)
    }

    @Test
    fun `BadgeCategory values are correctly named`() {
        assertThat(BadgeCategory.valueOf("GETTING_STARTED").isEqualTo(BadgeCategory.GETTING_STARTED))
        assertThat(BadgeCategory.valueOf("VOLUME").isEqualTo(BadgeCategory.VOLUME))
        assertThat(BadgeCategory.valueOf("OPERATION_MASTERY").isEqualTo(BadgeCategory.OPERATION_MASTERY))
        assertThat(BadgeCategory.valueOf("SPEED_ACCURACY").isEqualTo(BadgeCategory.SPEED_ACCURACY))
        assertThat(BadgeCategory.valueOf("STREAK").isEqualTo(BadgeCategory.STREAK))
        assertThat(BadgeCategory.valueOf("GAMES").isEqualTo(BadgeCategory.GAMES))
    }
}
