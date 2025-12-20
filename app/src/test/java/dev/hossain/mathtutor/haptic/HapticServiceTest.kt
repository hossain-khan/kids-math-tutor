package dev.hossain.mathtutor.haptic

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HapticService behavior.
 *
 * These tests use a fake implementation of HapticService to verify
 * the expected behavior without requiring actual vibration hardware.
 */
class HapticServiceTest {
    private lateinit var hapticService: FakeHapticService

    @Before
    fun setup() {
        hapticService = FakeHapticService()
    }

    // ==================== Success Vibration Tests ====================

    @Test
    fun `triggerSuccess triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerSuccess()

        assertThat(hapticService.successTriggered).isEqualTo(1)
    }

    @Test
    fun `triggerSuccess does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerSuccess()

        assertThat(hapticService.successTriggered).isEqualTo(0)
    }

    // ==================== Error Vibration Tests ====================

    @Test
    fun `triggerError triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerError()

        assertThat(hapticService.errorTriggered).isEqualTo(1)
    }

    @Test
    fun `triggerError does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerError()

        assertThat(hapticService.errorTriggered).isEqualTo(0)
    }

    // ==================== Badge Unlock Vibration Tests ====================

    @Test
    fun `triggerBadgeUnlock triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerBadgeUnlock()

        assertThat(hapticService.badgeUnlockTriggered).isEqualTo(1)
    }

    @Test
    fun `triggerBadgeUnlock does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerBadgeUnlock()

        assertThat(hapticService.badgeUnlockTriggered).isEqualTo(0)
    }

    // ==================== Button Click Vibration Tests ====================

    @Test
    fun `triggerButtonClick triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerButtonClick()

        assertThat(hapticService.buttonClickTriggered).isEqualTo(1)
    }

    @Test
    fun `triggerButtonClick does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerButtonClick()

        assertThat(hapticService.buttonClickTriggered).isEqualTo(0)
    }

    // ==================== Long Press Vibration Tests ====================

    @Test
    fun `triggerLongPress triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerLongPress()

        assertThat(hapticService.longPressTriggered).isEqualTo(1)
    }

    @Test
    fun `triggerLongPress does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerLongPress()

        assertThat(hapticService.longPressTriggered).isEqualTo(0)
    }

    // ==================== Settings Tests ====================

    @Test
    fun `haptics are enabled by default`() {
        val newService = FakeHapticService()

        assertThat(newService.isHapticsEnabled).isTrue()
    }

    @Test
    fun `setHapticsEnabled updates haptics state`() {
        hapticService.setHapticsEnabled(false)

        assertThat(hapticService.isHapticsEnabled).isFalse()
    }

    @Test
    fun `setHapticsEnabled to true enables haptics`() {
        hapticService.setHapticsEnabled(false)
        hapticService.setHapticsEnabled(true)

        assertThat(hapticService.isHapticsEnabled).isTrue()
    }

    // ==================== Multiple Calls Tests ====================

    @Test
    fun `multiple triggerSuccess calls increment count`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerSuccess()
        hapticService.triggerSuccess()
        hapticService.triggerSuccess()

        assertThat(hapticService.successTriggered).isEqualTo(3)
    }

    @Test
    fun `different vibration types increment separate counts`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerSuccess()
        hapticService.triggerError()
        hapticService.triggerBadgeUnlock()
        hapticService.triggerButtonClick()
        hapticService.triggerLongPress()

        assertThat(hapticService.successTriggered).isEqualTo(1)
        assertThat(hapticService.errorTriggered).isEqualTo(1)
        assertThat(hapticService.badgeUnlockTriggered).isEqualTo(1)
        assertThat(hapticService.buttonClickTriggered).isEqualTo(1)
        assertThat(hapticService.longPressTriggered).isEqualTo(1)
    }

    @Test
    fun `enabling haptics after disabling allows vibrations`() {
        hapticService.setHapticsEnabled(false)
        hapticService.triggerSuccess()
        assertThat(hapticService.successTriggered).isEqualTo(0)

        hapticService.setHapticsEnabled(true)
        hapticService.triggerSuccess()
        assertThat(hapticService.successTriggered).isEqualTo(1)
    }

    /**
     * Fake implementation of HapticService for testing.
     * Tracks all calls and state without actual vibration.
     */
    private class FakeHapticService : HapticService {
        // Trigger counters
        var successTriggered = 0
        var errorTriggered = 0
        var badgeUnlockTriggered = 0
        var buttonClickTriggered = 0
        var longPressTriggered = 0

        // State
        private var hapticsEnabledState = true

        val isHapticsEnabled: Boolean get() = hapticsEnabledState

        override fun triggerSuccess() {
            if (hapticsEnabledState) successTriggered++
        }

        override fun triggerError() {
            if (hapticsEnabledState) errorTriggered++
        }

        override fun triggerBadgeUnlock() {
            if (hapticsEnabledState) badgeUnlockTriggered++
        }

        override fun triggerButtonClick() {
            if (hapticsEnabledState) buttonClickTriggered++
        }

        override fun triggerLongPress() {
            if (hapticsEnabledState) longPressTriggered++
        }

        override fun setHapticsEnabled(enabled: Boolean) {
            hapticsEnabledState = enabled
        }
    }
}
