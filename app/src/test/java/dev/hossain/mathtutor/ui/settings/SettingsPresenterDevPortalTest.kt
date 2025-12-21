package dev.hossain.mathtutor.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsPresenterDevPortalTest {
    @Test
    fun devPortal_visibility_matchesBuildConfig() {
        val expected = dev.hossain.mathtutor.BuildConfig.DEBUG
        assertThat(SettingsPresenter.isDevPortalVisible()).isEqualTo(expected)
    }
}
