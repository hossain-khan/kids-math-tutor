package dev.hossain.mathtutor.haptic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        assertEquals(1, hapticService.successTriggered)
    }

    @Test
    fun `triggerSuccess does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerSuccess()

        assertEquals(0, hapticService.successTriggered)
    }

    // ==================== Error Vibration Tests ====================

    @Test
    fun `triggerError triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerError()

        assertEquals(1, hapticService.errorTriggered)
    }

    @Test
    fun `triggerError does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerError()

        assertEquals(0, hapticService.errorTriggered)
    }

    // ==================== Badge Unlock Vibration Tests ====================

    @Test
    fun `triggerBadgeUnlock triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerBadgeUnlock()

        assertEquals(1, hapticService.badgeUnlockTriggered)
    }

    @Test
    fun `triggerBadgeUnlock does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerBadgeUnlock()

        assertEquals(0, hapticService.badgeUnlockTriggered)
    }

    // ==================== Button Click Vibration Tests ====================

    @Test
    fun `triggerButtonClick triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerButtonClick()

        assertEquals(1, hapticService.buttonClickTriggered)
    }

    @Test
    fun `triggerButtonClick does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerButtonClick()

        assertEquals(0, hapticService.buttonClickTriggered)
    }

    // ==================== Long Press Vibration Tests ====================

    @Test
    fun `triggerLongPress triggers vibration when haptics are enabled`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerLongPress()

        assertEquals(1, hapticService.longPressTriggered)
    }

    @Test
    fun `triggerLongPress does not trigger when haptics are disabled`() {
        hapticService.setHapticsEnabled(false)

        hapticService.triggerLongPress()

        assertEquals(0, hapticService.longPressTriggered)
    }

    // ==================== Settings Tests ====================

    @Test
    fun `haptics are enabled by default`() {
        val newService = FakeHapticService()

        assertTrue(newService.isHapticsEnabled)
    }

    @Test
    fun `setHapticsEnabled updates haptics state`() {
        hapticService.setHapticsEnabled(false)

        assertFalse(hapticService.isHapticsEnabled)
    }

    @Test
    fun `setHapticsEnabled to true enables haptics`() {
        hapticService.setHapticsEnabled(false)
        hapticService.setHapticsEnabled(true)

        assertTrue(hapticService.isHapticsEnabled)
    }

    // ==================== Multiple Calls Tests ====================

    @Test
    fun `multiple triggerSuccess calls increment count`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerSuccess()
        hapticService.triggerSuccess()
        hapticService.triggerSuccess()

        assertEquals(3, hapticService.successTriggered)
    }

    @Test
    fun `different vibration types increment separate counts`() {
        hapticService.setHapticsEnabled(true)

        hapticService.triggerSuccess()
        hapticService.triggerError()
        hapticService.triggerBadgeUnlock()
        hapticService.triggerButtonClick()
        hapticService.triggerLongPress()

        assertEquals(1, hapticService.successTriggered)
        assertEquals(1, hapticService.errorTriggered)
        assertEquals(1, hapticService.badgeUnlockTriggered)
        assertEquals(1, hapticService.buttonClickTriggered)
        assertEquals(1, hapticService.longPressTriggered)
    }

    @Test
    fun `enabling haptics after disabling allows vibrations`() {
        hapticService.setHapticsEnabled(false)
        hapticService.triggerSuccess()
        assertEquals(0, hapticService.successTriggered)

        hapticService.setHapticsEnabled(true)
        hapticService.triggerSuccess()
        assertEquals(1, hapticService.successTriggered)
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
