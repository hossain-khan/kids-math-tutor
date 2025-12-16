package dev.hossain.mathtutor.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [AnswerField] component.
 *
 * These tests verify the rendering, accessibility, and behavior of the AnswerField component.
 */
class AnswerFieldTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun answerField_emptyAnswer_displaysPlaceholder() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "")
            }
        }

        // Then - placeholder "?" should be displayed
        composeTestRule.onNodeWithText("?").assertIsDisplayed()
    }

    @Test
    fun answerField_hasAccessibilityLabel() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "")
            }
        }

        // Then - label "Your Answer" should be present for screen readers
        composeTestRule.onNodeWithText("Your Answer").assertIsDisplayed()
    }

    @Test
    fun answerField_withAnswer_displaysAnswer() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "42")
            }
        }

        // Then - answer "42" should be displayed
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun answerField_withMultiDigitAnswer_displaysFullAnswer() {
        // Given
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "123")
            }
        }

        // Then - full answer "123" should be displayed
        composeTestRule.onNodeWithText("123").assertIsDisplayed()
    }

    @Test
    fun answerField_updatingAnswer_displaysNewAnswer() {
        // Given - initial answer
        var answer = "1"
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = answer)
            }
        }

        // Verify initial state
        composeTestRule.onNodeWithText("1").assertIsDisplayed()

        // When - updating answer
        answer = "12"
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = answer)
            }
        }

        // Then - new answer should be displayed
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun answerField_clearingAnswer_displaysPlaceholder() {
        // Given - initial answer
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "42")
            }
        }

        // Verify initial state
        composeTestRule.onNodeWithText("42").assertIsDisplayed()

        // When - clearing answer
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "")
            }
        }

        // Then - placeholder should be displayed again
        composeTestRule.onNodeWithText("?").assertIsDisplayed()
    }

    @Test
    fun answerField_labelPersists_withAndWithoutAnswer() {
        // Given - empty field
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "")
            }
        }

        // Then - label exists
        composeTestRule.onNodeWithText("Your Answer").assertIsDisplayed()

        // When - adding answer
        composeTestRule.setContent {
            KidsMathTutorAppTheme {
                AnswerField(answer = "42")
            }
        }

        // Then - label still exists
        composeTestRule.onNodeWithText("Your Answer").assertIsDisplayed()
    }
}
