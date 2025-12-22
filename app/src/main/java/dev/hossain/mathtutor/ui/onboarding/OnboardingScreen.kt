package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data object OnboardingScreen : Screen {
    data class State(
        val currentPage: Int,
        val totalPages: Int,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class PageChanged(
            val page: Int,
        ) : Event()

        data object NextClicked : Event()

        data object SkipClicked : Event()

        data object GetStartedClicked : Event()
    }
}

private data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
)

private val onboardingPages =
    listOf(
        OnboardingPage(
            imageRes = R.drawable.onboarding_1_app_name_welcome,
            title = "Welcome to Math Pup Tutor!",
            description = "Let's make learning math fun and exciting together!",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_2_creative_math_red_theme,
            title = "Creative Learning",
            description = "Explore math concepts through interactive and creative exercises.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_3_explore_numbers_green_theme,
            title = "Discover Numbers",
            description = "Build confidence with numbers through engaging practice sessions.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_4_master_math_blue_theme,
            title = "Master Math Skills",
            description = "Track your progress and become a math champion!",
        ),
    )

@AssistedInject
class OnboardingPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<OnboardingScreen.State> {
        @Composable
        override fun present(): OnboardingScreen.State {
            val coroutineScope = rememberCoroutineScope()
            var currentPage = 0

            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Onboarding",
                    screenClass = OnboardingScreen::class.java.name,
                )
                analyticsService.logEvent(AnalyticsEvent.ONBOARDING_STARTED)
            }

            return OnboardingScreen.State(
                currentPage = currentPage,
                totalPages = onboardingPages.size,
            ) { event ->
                when (event) {
                    is OnboardingScreen.Event.PageChanged -> {
                        currentPage = event.page
                        Timber.d("Onboarding: Page changed to ${event.page}")
                    }

                    OnboardingScreen.Event.NextClicked -> {
                        // Handled in UI layer with pager
                    }

                    OnboardingScreen.Event.SkipClicked,
                    OnboardingScreen.Event.GetStartedClicked,
                    -> {
                        Timber.d("Onboarding: Skip/GetStarted clicked - currentPage=$currentPage")
                        coroutineScope.launch {
                            userPreferencesRepository.setOnboardingCompleted(true)
                            // Track onboarding completed
                            analyticsService.logEvent(AnalyticsEvent.ONBOARDING_COMPLETED)
                            navigator.goTo(GradeSelectionScreen())
                        }
                    }
                }
            }
        }

        @CircuitInject(OnboardingScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): OnboardingPresenter
        }
    }

@CircuitInject(OnboardingScreen::class, AppScope::class)
@Composable
fun OnboardingContent(
    state: OnboardingScreen.State,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = onboardingPages[pagerState.currentPage]
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    // Use theme colors for background based on page index
    // Rotate through theme color containers for variety
    val colorScheme = MaterialTheme.colorScheme
    val pageColors = getPageColors(pagerState.currentPage, colorScheme)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(pageColors.backgroundColor),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(systemBarsPadding)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    TextButton(
                        onClick = { state.eventSink(OnboardingScreen.Event.SkipClicked) },
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = pageColors.contentColor,
                            ),
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(
                    page = onboardingPages[page],
                    contentColor = pageColors.contentColor,
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier =
                            Modifier
                                .padding(horizontal = 6.dp)
                                .size(if (isSelected) 14.dp else 10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        pageColors.contentColor
                                    } else {
                                        pageColors.contentColor.copy(alpha = 0.3f)
                                    },
                                ),
                    )
                }
            }

            if (pagerState.currentPage < onboardingPages.size - 1) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else {
                Button(
                    onClick = { state.eventSink(OnboardingScreen.Event.GetStartedClicked) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        text = "Get Started! 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    contentColor: Color,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Data class to hold background and content color pairs for onboarding pages.
 */
private data class PageColors(
    val backgroundColor: Color,
    val contentColor: Color,
)

/**
 * Returns theme-aware colors for onboarding pages based on page index.
 * Rotates through Material 3 color containers for visual variety while maintaining accessibility.
 */
private fun getPageColors(
    pageIndex: Int,
    colorScheme: androidx.compose.material3.ColorScheme,
): PageColors {
    // Number of different color schemes to rotate through
    val colorRotationCount = 4

    return when (pageIndex % colorRotationCount) {
        0 -> {
            PageColors(
                backgroundColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
            )
        }

        1 -> {
            PageColors(
                backgroundColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer,
            )
        }

        2 -> {
            PageColors(
                backgroundColor = colorScheme.tertiaryContainer,
                contentColor = colorScheme.onTertiaryContainer,
            )
        }

        else -> {
            PageColors(
                backgroundColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingContentPreview() {
    KidsMathTutorAppTheme {
        OnboardingContent(
            state =
                OnboardingScreen.State(
                    currentPage = 0,
                    totalPages = 4,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingContentDarkPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        OnboardingContent(
            state =
                OnboardingScreen.State(
                    currentPage = 2,
                    totalPages = 4,
                    eventSink = {},
                ),
        )
    }
}
