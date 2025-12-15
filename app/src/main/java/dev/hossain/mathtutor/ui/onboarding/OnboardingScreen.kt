package dev.hossain.mathtutor.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

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
    ) : Presenter<OnboardingScreen.State> {
        @Composable
        override fun present(): OnboardingScreen.State {
            val coroutineScope = rememberCoroutineScope()
            var currentPage = 0

            return OnboardingScreen.State(
                currentPage = currentPage,
                totalPages = onboardingPages.size,
            ) { event ->
                when (event) {
                    is OnboardingScreen.Event.PageChanged -> {
                        currentPage = event.page
                    }

                    OnboardingScreen.Event.NextClicked -> {
                        // Handled in UI layer with pager
                    }

                    OnboardingScreen.Event.SkipClicked,
                    OnboardingScreen.Event.GetStartedClicked,
                    -> {
                        coroutineScope.launch {
                            userPreferencesRepository.setOnboardingCompleted(true)
                            navigator.pop()
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

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    TextButton(onClick = { state.eventSink(OnboardingScreen.Event.SkipClicked) }) {
                        Text("Skip")
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Card(
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                            ),
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pagerState.currentPage < onboardingPages.size - 1) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next")
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else {
                Button(
                    onClick = { state.eventSink(OnboardingScreen.Event.GetStartedClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Get Started")
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}
