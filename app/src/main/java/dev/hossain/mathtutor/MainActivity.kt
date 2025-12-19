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
        @OptIn(ExperimentalSharedTransitionApi::class)
        override fun onCreate(savedInstanceState: Bundle?) {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)

            // Start background music when app launches
            audioService.startBackgroundMusic()
            Timber.d("Started background music on app launch")

            // Register lifecycle observer for music management
            lifecycle.addObserver(
                object : DefaultLifecycleObserver {
                    override fun onPause(owner: LifecycleOwner) {
                        audioService.pauseBackgroundMusic()
                        Timber.d("Paused background music (app backgrounded)")
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        audioService.resumeBackgroundMusic()
                        Timber.d("Resumed background music (app foregrounded)")
                    }

                    override fun onDestroy(owner: LifecycleOwner) {
                        audioService.stopBackgroundMusic()
                        audioService.release()
                        Timber.d("Stopped background music and released audio resources (app closed)")
                    }
                },
            )

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

                    // See https://slackhq.github.io/circuit/circuit-content/
                    CircuitCompositionLocals(circuit) {
                        // See https://slackhq.github.io/circuit/shared-elements/
                        SharedElementTransitionLayout {
                            // See https://slackhq.github.io/circuit/overlays/
                            ContentWithOverlays {
                                NavigableCircuitContent(
                                    navigator = navigator,
                                    backStack = backStack,
                                    decoratorFactory =
                                        remember(navigator) {
                                            GestureNavigationDecorationFactory(onBackInvoked = navigator::pop)
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
