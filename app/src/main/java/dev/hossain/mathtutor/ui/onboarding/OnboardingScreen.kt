package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.unit.Dp
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
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_LARGE
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
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
        private val analyticsService: AnalyticsService,
    ) : Presenter<OnboardingScreen.State> {
        @Composable
        override fun present(): OnboardingScreen.State {
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
                        // Navigate to grade selection - onboarding is only marked complete after name entry
                        navigator.goTo(GradeSelectionScreen())
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
    val isDarkMode = isSystemInDarkTheme()

    // Use configured colors based on page index and theme mode
    val pageColors = getPageColors(pagerState.currentPage, isDarkMode)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(pageColors.backgroundColor),
    ) {
        // Center content on wider screens
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH_LARGE)
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

                // Adaptive page indicator spacing
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                ) {
                    val indicatorSpacing =
                        when {
                            maxWidth >= EXPANDED_WIDTH_BREAKPOINT -> 10.dp
                            maxWidth >= MEDIUM_WIDTH_BREAKPOINT -> 8.dp
                            else -> 6.dp
                        }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(onboardingPages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier =
                                    Modifier
                                        .padding(horizontal = indicatorSpacing)
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
                                containerColor = pageColors.buttonColor,
                                contentColor = pageColors.contentColor,
                            ),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp),
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
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
                                containerColor = pageColors.buttonColor,
                                contentColor = pageColors.contentColor,
                            ),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text(
                            text = "Get Started! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
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
    BoxWithConstraints {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        // Check the smaller dimension to determine device size (works in both orientations)
        val smallerDimension = if (screenWidth < screenHeight) screenWidth else screenHeight
        val isPhone = smallerDimension < MEDIUM_WIDTH_BREAKPOINT

        // Debug logging
        Timber.d(
            "OnboardingPageContent: screenWidth=$screenWidth, screenHeight=$screenHeight, isLandscape=$isLandscape, smallerDimension=$smallerDimension, isPhone=$isPhone, MEDIUM_WIDTH_BREAKPOINT=$MEDIUM_WIDTH_BREAKPOINT",
        )

        // Adaptive image scaling based on screen width
        val imageScale =
            when {
                screenWidth >= EXPANDED_WIDTH_BREAKPOINT -> 1.5f

                // Large tablets: 1.5x scale
                screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> 1.3f

                // Tablets: 1.3x scale
                else -> 1.0f // Phones: 1x scale
            }

        // Adaptive spacing based on screen width
        val verticalSpacing =
            when {
                screenWidth >= EXPANDED_WIDTH_BREAKPOINT -> 48.dp
                screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> 40.dp
                else -> 32.dp
            }

        // Use side-by-side layout for phone landscape mode
        if (isPhone && isLandscape) {
            Timber.d("OnboardingPageContent: Using side-by-side layout (phone landscape)")
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    modifier =
                        Modifier
                            .weight(0.45f)
                            .wrapContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = page.title,
                        modifier = Modifier.wrapContentSize(),
                        contentScale = ContentScale.Fit,
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .weight(0.55f)
                            .wrapContentSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = contentColor,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else {
            // Default vertical layout for portrait and tablets
            Timber.d("OnboardingPageContent: Using vertical layout (portrait or tablet)")
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
                            .wrapContentSize()
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
                                .wrapContentSize()
                                .widthIn(max = 600.dp * imageScale),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                Text(
                    text = page.title,
                    style =
                        when {
                            screenWidth >= EXPANDED_WIDTH_BREAKPOINT -> MaterialTheme.typography.displaySmall
                            screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> MaterialTheme.typography.headlineLarge
                            else -> MaterialTheme.typography.headlineLarge
                        },
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style =
                        when {
                            screenWidth >= MEDIUM_WIDTH_BREAKPOINT -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.bodyLarge
                        },
                    color = contentColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/**
 * Data class to hold background, content, and button color for onboarding pages.
 */
private data class PageColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val buttonColor: Color,
)

/**
 * Returns configured colors for onboarding pages based on page index and theme mode.
 * Uses hardcoded colors from OnboardingColorConfig based on image themes.
 */
private fun getPageColors(
    pageIndex: Int,
    isDarkMode: Boolean,
): PageColors {
    val config =
        onboardingPageColorsConfig.getOrNull(pageIndex % onboardingPageColorsConfig.size)
            ?: onboardingPageColorsConfig[0]

    return if (isDarkMode) {
        PageColors(
            backgroundColor = config.darkBackgroundColor,
            contentColor = config.darkTextColor,
            buttonColor = config.darkButtonColor,
        )
    } else {
        PageColors(
            backgroundColor = config.lightBackgroundColor,
            contentColor = config.lightTextColor,
            buttonColor = config.lightButtonColor,
        )
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

@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape",
)
@Composable
private fun OnboardingContentPhoneLandscapePreview() {
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

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape",
)
@Composable
private fun OnboardingContentTabletLandscapePreview() {
    KidsMathTutorAppTheme {
        OnboardingContent(
            state =
                OnboardingScreen.State(
                    currentPage = 1,
                    totalPages = 4,
                    eventSink = {},
                ),
        )
    }
}
