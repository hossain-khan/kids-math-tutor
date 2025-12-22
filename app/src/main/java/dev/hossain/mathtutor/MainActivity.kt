package dev.hossain.mathtutor

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.sharedelements.SharedElementTransitionLayout
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.di.ActivityKey
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.hossain.mathtutor.ui.navigation.AdaptiveNavigationWrapper
import dev.hossain.mathtutor.ui.navigation.isTopLevelDestination
import dev.hossain.mathtutor.ui.onboarding.OnboardingScreen
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import timber.log.Timber

/**
 * Main activity for the application, demonstrating Metro constructor injection for Activities.
 *
 * This Activity is injected via constructor using Metro DI, enabled by [ComposeAppComponentFactory].
 *
 * Key Metro features demonstrated:
 * - [ActivityKey]: Map key annotation for multibinding
 * - [ContributesIntoMap]: Contributes this Activity to the multibinding map
 * - [Inject]: Marks this class for constructor injection
 * - [binding]: Type-safe binding helper for specifying the bound type
 *
 * The Activity receives its dependencies ([Circuit]) through constructor injection,
 * which is more testable and type-safe than field injection.
 *
 * See https://zacsweers.github.io/metro/latest/injection-types/#constructor-injection for constructor injection.
 * See https://zacsweers.github.io/metro/latest/bindings/#multibindings for multibindings.
 * See https://zacsweers.github.io/metro/latest/aggregation/ for contribution.
 */
@ActivityKey(MainActivity::class)
@ContributesIntoMap(AppScope::class, binding = binding<Activity>())
@Inject
class MainActivity
    constructor(
        private val circuit: Circuit,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val audioService: AudioService,
    ) : ComponentActivity() {
        // Lifecycle observer for music management - stored as property to allow proper cleanup
        private val musicLifecycleObserver =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    // Start/resume music when app becomes visible (better than onResume for lifecycle)
                    audioService.resumeBackgroundMusic()
                    Timber.d("[MainActivity] Resumed background music (app visible)")
                }

                override fun onStop(owner: LifecycleOwner) {
                    // Pause music when app is no longer visible (better than onPause for lifecycle)
                    audioService.pauseBackgroundMusic()
                    Timber.d("[MainActivity] Paused background music (app not visible)")
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    // Clean up audio resources and remove observer to prevent memory leaks
                    audioService.stopBackgroundMusic()
                    audioService.release()
                    lifecycle.removeObserver(this)
                    Timber.d("[MainActivity] Stopped background music, released resources, and removed observer")
                }
            }

        @OptIn(ExperimentalSharedTransitionApi::class)
        override fun onCreate(savedInstanceState: Bundle?) {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)

            // Start background music when app launches
            audioService.startBackgroundMusic()
            Timber.d("[MainActivity] Started background music on app launch")

            // Register lifecycle observer for music management
            lifecycle.addObserver(musicLifecycleObserver)

            setContent {
                KidsMathTutorAppTheme {
                    val isOnboardingCompleted by userPreferencesRepository.isOnboardingCompleted.collectAsState(
                        initial = false,
                    )

                    val initialScreen =
                        if (isOnboardingCompleted) {
                            HomeScreen
                        } else {
                            OnboardingScreen
                        }

                    // See https://slackhq.github.io/circuit/navigation/
                    val backStack = rememberSaveableBackStack(root = initialScreen)
                    val navigator = rememberCircuitNavigator(backStack)

                    // Get the current screen for adaptive navigation highlighting
                    val currentScreen = backStack.topRecord?.screen

                    // See https://slackhq.github.io/circuit/circuit-content/
                    CircuitCompositionLocals(circuit) {
                        // See https://slackhq.github.io/circuit/shared-elements/
                        SharedElementTransitionLayout {
                            // See https://slackhq.github.io/circuit/overlays/
                            ContentWithOverlays {
                                // Only show adaptive navigation for top-level destinations
                                // Hide it during onboarding and for non-top-level screens
                                val showAdaptiveNav =
                                    isOnboardingCompleted &&
                                        currentScreen?.isTopLevelDestination() == true

                                if (showAdaptiveNav) {
                                    AdaptiveNavigationWrapper(
                                        currentScreen = currentScreen,
                                        onDestinationSelected = { destination ->
                                            // Navigate to the selected destination
                                            // Use resetRoot to avoid stacking top-level destinations
                                            navigator.resetRoot(destination.screen)
                                        },
                                    ) {
                                        NavigableCircuitContent(
                                            navigator = navigator,
                                            backStack = backStack,
                                            decoratorFactory =
                                                remember(navigator) {
                                                    GestureNavigationDecorationFactory(
                                                        onBackInvoked = navigator::pop,
                                                    )
                                                },
                                        )
                                    }
                                } else {
                                    // Show content without adaptive navigation
                                    // (during onboarding or non-top-level screens)
                                    NavigableCircuitContent(
                                        navigator = navigator,
                                        backStack = backStack,
                                        decoratorFactory =
                                            remember(navigator) {
                                                GestureNavigationDecorationFactory(
                                                    onBackInvoked = navigator::pop,
                                                )
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
