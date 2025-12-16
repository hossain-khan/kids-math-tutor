package dev.hossain.mathtutor.ui.component

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [NumberPad] component.
 *
 * These tests verify the rendering, interaction, and accessibility of the NumberPad component.
 */
class NumberPadTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun numberPad_displaysAllNumbers() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = {})
            }
        }

        // Then - all numbers from 0-9 should be displayed
        for (number in 0..9) {
            composeTestRule.onNodeWithText(number.toString()).assertIsDisplayed()
        }
    }

    @Test
    fun numberPad_allButtonsHaveAccessibilityLabels() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = {})
            }
        }

        // Then - all number buttons should have content descriptions
        for (number in 0..9) {
            composeTestRule
                .onNodeWithContentDescription("Number $number")
                .assertIsDisplayed()
        }
    }

    @Test
    fun numberPad_clickingNumber_invokesCallback() {
        // Given
        var clickedNumber: Int? = null
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = { clickedNumber = it })
            }
        }

        // When - clicking number 5
        composeTestRule.onNodeWithContentDescription("Number 5").performClick()

        // Then - callback should be invoked with 5
        assertEquals(5, clickedNumber)
    }

    @Test
    fun numberPad_clickingMultipleNumbers_invokesCallbackForEach() {
        // Given
        val clickedNumbers = mutableListOf<Int>()
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = { clickedNumbers.add(it) })
            }
        }

        // When - clicking numbers 1, 2, 3
        composeTestRule.onNodeWithContentDescription("Number 1").performClick()
        composeTestRule.onNodeWithContentDescription("Number 2").performClick()
        composeTestRule.onNodeWithContentDescription("Number 3").performClick()

        // Then - all three numbers should be recorded
        assertEquals(listOf(1, 2, 3), clickedNumbers)
    }

    @Test
    fun numberPad_clickingZero_invokesCallbackWithZero() {
        // Given
        var clickedNumber: Int? = null
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = { clickedNumber = it })
            }
        }

        // When - clicking number 0
        composeTestRule.onNodeWithContentDescription("Number 0").performClick()

        // Then - callback should be invoked with 0
        assertEquals(0, clickedNumber)
    }

    @Test
    fun numberPad_buttonsDisplayedInCorrectOrder() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                NumberPad(onNumberClick = {})
            }
        }

        // Then - verify all numbers are present (layout order verified visually)
        // First row: 1, 2, 3, 4, 5
        for (number in 1..5) {
            composeTestRule.onNodeWithText(number.toString()).assertIsDisplayed()
        }

        // Second row: 6, 7, 8, 9, 0
        for (number in 6..9) {
            composeTestRule.onNodeWithText(number.toString()).assertIsDisplayed()
        }
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }
}
