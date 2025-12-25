package dev.hossain.mathtutor.ui.devportal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.NumberRange
import dev.hossain.mathtutor.domain.model.ProblemSpec
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.domain.service.CustomChallengeService
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.haptic.HapticService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant

/**
 * Basic scaffold presenter for `DeveloperPortalScreen`.
 * Implements placeholder actions used by dev tools. Concrete implementations of actions
 * will be added later as separate tasks.
 */
@AssistedInject
class DeveloperPortalPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val userProfileRepository: UserProfileRepository,
        private val sessionRepository: SessionRepository,
        private val gameRepository: GameRepository,
        private val badgeRepository: BadgeRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
        private val sessionSeeder: dev.hossain.mathtutor.devtools.SessionSeeder,
        private val customChallengeService: CustomChallengeService,
    ) : Presenter<DeveloperPortalScreen.State> {
        @CircuitInject(DeveloperPortalScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): DeveloperPortalPresenter
        }

        @Composable
        override fun present(): DeveloperPortalScreen.State {
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Developer Portal",
                    screenClass = DeveloperPortalScreen::class.java.name,
                )
            }

            val scope = rememberCoroutineScope()
            var showSeedSection by remember { mutableStateOf(true) }
            var showDataOpsSection by remember { mutableStateOf(true) }
            var showDiagnosticsSection by remember { mutableStateOf(true) }
            var showClearConfirm by remember { mutableStateOf(false) }
            var clearInProgress by remember { mutableStateOf(false) }
            var clearResultMessage by remember { mutableStateOf<String?>(null) }
            var showResetOnboardingConfirm by remember { mutableStateOf(false) }
            var resetOnboardingInProgress by remember { mutableStateOf(false) }
            var resetOnboardingResultMessage by remember { mutableStateOf<String?>(null) }
            var seedInProgress by remember { mutableStateOf(false) }
            var seedResultMessage by remember { mutableStateOf<String?>(null) }
            var importChallengesInProgress by remember { mutableStateOf(false) }
            var importChallengesResultMessage by remember { mutableStateOf<String?>(null) }

            var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }
            var isAnalyticsEnabled by remember { mutableStateOf(true) }
            var isBackgroundMusicPlaying by remember { mutableStateOf(false) }
            var soundHapticFeedback by remember { mutableStateOf<String?>(null) }
            var soundsLoadedState by remember { mutableStateOf(false) }
            var sampleIdMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
            var currentProfileName by remember { mutableStateOf<String?>(null) }
            var currentGradeLevel by remember { mutableStateOf<GradeLevel?>(null) }
            var currentAdaptiveDifficulty by remember { mutableStateOf(true) }
            var profileUpdateResultMessage by remember { mutableStateOf<String?>(null) }
            var totalSessionCount by remember { mutableStateOf(0) }
            // Remembered listener to receive sound load events
            val soundListener =
                remember<(Boolean, Map<String, Int>) -> Unit> {
                    { loaded, sampleIds ->
                        soundsLoadedState = loaded
                        sampleIdMap = sampleIds
                        Timber.d("[DevPortal] Sound load listener: loaded=$loaded, sampleIds=$sampleIds")
                    }
                }

            LaunchedEffect(Unit) {
                // Collect badges from repository to display in UI
                launch {
                    badgeRepository.getAllBadges().collect { list ->
                        badges = list
                    }
                }
                // Load current analytics state
                launch {
                    userPreferencesRepository.isAnalyticsEnabled.collect { enabled ->
                        isAnalyticsEnabled = enabled
                    }
                }
                // Load current user profile
                launch {
                    userProfileRepository.getProfile().collect { profile ->
                        currentProfileName = profile?.name
                        currentGradeLevel = profile?.gradeLevel
                        currentAdaptiveDifficulty = profile?.adaptiveDifficultyEnabled ?: true
                    }
                }

                // Load session count
                launch {
                    sessionRepository.getAllSessions().collect { sessions ->
                        totalSessionCount = sessions.size
                    }
                }

                // Register the listener (it will be invoked immediately with current state)
                audioService.registerSoundLoadListener(soundListener)
            }

            // Ensure we unregister the listener when this composable leaves
            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    try {
                        audioService.unregisterSoundLoadListener(soundListener)
                    } catch (e: Exception) {
                        Timber.e(e, "[DevPortal] Failed to unregister sound listener")
                    }
                }
            }

            var forceUnlockInProgress by remember { mutableStateOf(false) }
            var forceUnlockResultMessage by remember { mutableStateOf<String?>(null) }

            return DeveloperPortalScreen.State(
                showSeedSection = showSeedSection,
                showDataOpsSection = showDataOpsSection,
                showDiagnosticsSection = showDiagnosticsSection,
                showClearConfirm = showClearConfirm,
                clearInProgress = clearInProgress,
                clearResultMessage = clearResultMessage,
                showResetOnboardingConfirm = showResetOnboardingConfirm,
                resetOnboardingInProgress = resetOnboardingInProgress,
                resetOnboardingResultMessage = resetOnboardingResultMessage,
                seedInProgress = seedInProgress,
                seedResultMessage = seedResultMessage,
                importChallengesInProgress = importChallengesInProgress,
                importChallengesResultMessage = importChallengesResultMessage,
                badges = badges,
                forceUnlockInProgress = forceUnlockInProgress,
                forceUnlockResultMessage = forceUnlockResultMessage,
                isAnalyticsEnabled = isAnalyticsEnabled,
                isBackgroundMusicPlaying = isBackgroundMusicPlaying,
                soundHapticFeedback = soundHapticFeedback,
                currentProfileName = currentProfileName,
                currentGradeLevel = currentGradeLevel,
                currentAdaptiveDifficulty = currentAdaptiveDifficulty,
                profileUpdateResultMessage = profileUpdateResultMessage,
                soundsLoaded = soundsLoadedState,
                soundSampleIds = sampleIdMap,
                totalSessionCount = totalSessionCount,
            ) { event ->
                when (event) {
                    is DeveloperPortalScreen.Event.ForceUnlockBadge -> {
                        forceUnlockInProgress = true
                        forceUnlockResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                badgeRepository.unlockBadge(event.badgeId)
                                withContext(Dispatchers.Main) {
                                    forceUnlockResultMessage = "Badge unlocked"
                                    forceUnlockInProgress = false
                                }
                                Timber.d("[DevPortal] Force unlocked badge: ${event.badgeId}")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to force unlock badge: ${event.badgeId}")
                                withContext(Dispatchers.Main) {
                                    forceUnlockResultMessage = "Unlock failed: ${e.message}"
                                    forceUnlockInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.UnlockAllBadges -> {
                        forceUnlockInProgress = true
                        forceUnlockResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                var unlockedCount = 0
                                badges.forEach { badge ->
                                    if (!badge.isUnlocked()) {
                                        badgeRepository.unlockBadge(badge.id)
                                        unlockedCount++
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    forceUnlockResultMessage = "Unlocked $unlockedCount badges"
                                    forceUnlockInProgress = false
                                }
                                Timber.d("[DevPortal] Unlocked all badges: $unlockedCount")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to unlock all badges")
                                withContext(Dispatchers.Main) {
                                    forceUnlockResultMessage = "Unlock all failed: ${e.message}"
                                    forceUnlockInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.ToggleAnalyticsOverride -> {
                        // Toggle analytics immediately (debug-only)
                        scope.launch(Dispatchers.IO) {
                            val current = userPreferencesRepository.isAnalyticsEnabled.firstOrNull() ?: true
                            userPreferencesRepository.setAnalyticsEnabled(!current)
                            analyticsService.setAnalyticsEnabled(!current)
                            Timber.d("[DevPortal] Toggled analytics to ${!current}")
                        }
                    }

                    is DeveloperPortalScreen.Event.ClearAppDataClicked -> {
                        // Show confirmation dialog instead of clearing immediately
                        showClearConfirm = true
                        clearResultMessage = null
                    }

                    is DeveloperPortalScreen.Event.ConfirmClear -> {
                        // Expect user to type "DELETE" (case-sensitive) to confirm
                        if (event.confirmationText != "DELETE") {
                            clearResultMessage = "Confirmation text does not match 'DELETE'"
                            Timber.d("[DevPortal] Clear confirmation failed - wrong text")
                        } else {
                            // Perform clear
                            clearInProgress = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    Timber.d("[DevPortal] Clearing app data (DB, prefs, cache) - confirmed")
                                    sessionRepository.clearAllSessions()
                                    gameRepository.clearAllSessions()

                                    // Reset preferences to defaults
                                    userPreferencesRepository.setOnboardingCompleted(false)
                                    userPreferencesRepository.setHapticsEnabled(true)
                                    userPreferencesRepository.setSoundEffectsEnabled(true)
                                    userPreferencesRepository.setBackgroundMusicEnabled(false)
                                    userPreferencesRepository.setVolume(0.7f)
                                    userPreferencesRepository.setHighContrastEnabled(false)
                                    userPreferencesRepository.setLargeTextEnabled(false)
                                    userPreferencesRepository.setAnalyticsEnabled(true)

                                    withContext(Dispatchers.Main) {
                                        clearResultMessage = "Clear complete"
                                        showClearConfirm = false
                                        clearInProgress = false
                                    }
                                    Timber.d("[DevPortal] Clear complete")
                                } catch (e: Exception) {
                                    Timber.e(e, "[DevPortal] Failed to clear data")
                                    withContext(Dispatchers.Main) {
                                        clearResultMessage = "Clear failed: ${e.message}"
                                        clearInProgress = false
                                    }
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.CancelClear -> {
                        showClearConfirm = false
                        clearResultMessage = "Clear cancelled"
                    }

                    is DeveloperPortalScreen.Event.ResetOnboardingClicked -> {
                        // Show confirmation dialog instead of resetting immediately
                        showResetOnboardingConfirm = true
                        resetOnboardingResultMessage = null
                    }

                    is DeveloperPortalScreen.Event.ConfirmResetOnboarding -> {
                        // Perform reset onboarding
                        resetOnboardingInProgress = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                Timber.d("[DevPortal] Resetting onboarding state")
                                // Reset onboarding completed flag to trigger onboarding on next launch
                                userPreferencesRepository.setOnboardingCompleted(false)

                                withContext(Dispatchers.Main) {
                                    resetOnboardingResultMessage =
                                        "Onboarding reset. App will show onboarding on next launch."
                                    showResetOnboardingConfirm = false
                                    resetOnboardingInProgress = false
                                }
                                Timber.d("[DevPortal] Onboarding reset complete")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to reset onboarding")
                                withContext(Dispatchers.Main) {
                                    resetOnboardingResultMessage = "Reset failed: ${e.message}"
                                    resetOnboardingInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.CancelResetOnboarding -> {
                        showResetOnboardingConfirm = false
                        resetOnboardingResultMessage = "Reset cancelled"
                    }

                    is DeveloperPortalScreen.Event.SeedSessionsRequested -> {
                        // Trigger seeding with provided parameters
                        seedInProgress = true
                        seedResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                val seeded =
                                    sessionSeeder.seedSampleSessions(
                                        count = event.count,
                                        operation = event.operation,
                                        grade = event.grade,
                                        avgAccuracy = 0.8f,
                                    )
                                withContext(Dispatchers.Main) {
                                    seedResultMessage = "Seeded $seeded sessions"
                                    seedInProgress = false
                                }
                                Timber.d("[DevPortal] Seeded $seeded sessions")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to seed sessions")
                                withContext(Dispatchers.Main) {
                                    seedResultMessage = "Seed failed: ${e.message}"
                                    seedInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.SeedSessionsClicked -> {
                        // Backwards-compat: run default seeding (10 mixed grade1)
                        seedInProgress = true
                        seedResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                val seeded =
                                    sessionSeeder.seedSampleSessions(
                                        count = 10,
                                        operation = MathOperation.MIXED,
                                        grade = dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_1,
                                    )
                                withContext(Dispatchers.Main) {
                                    seedResultMessage = "Seeded $seeded sessions"
                                    seedInProgress = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    seedResultMessage = "Seed failed: ${e.message}"
                                    seedInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.ImportSampleChallengesClicked -> {
                        // Import 6 sample challenges with various problem types
                        importChallengesInProgress = true
                        importChallengesResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                val imported = importSampleChallenges()
                                withContext(Dispatchers.Main) {
                                    importChallengesResultMessage = "Imported $imported challenges"
                                    importChallengesInProgress = false
                                }
                                Timber.d("[DevPortal] Imported $imported sample challenges")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to import sample challenges")
                                withContext(Dispatchers.Main) {
                                    importChallengesResultMessage = "Import failed: ${e.message}"
                                    importChallengesInProgress = false
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.ForceBadgeCheckClicked -> {
                        scope.launch(Dispatchers.IO) {
                            val unlocked = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
                            Timber.d("[DevPortal] Force badge check unlocked ${unlocked.size} badges")
                        }
                    }

                    is DeveloperPortalScreen.Event.PlaySuccessSound -> {
                        audioService.playSuccess()
                        hapticService.triggerSuccess()
                        soundHapticFeedback = "Success sound & haptic played"
                        Timber.d("[DevPortal] Played success sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayErrorSound -> {
                        audioService.playError()
                        hapticService.triggerError()
                        soundHapticFeedback = "Error sound & haptic played"
                        Timber.d("[DevPortal] Played error sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayLevelUpSound -> {
                        audioService.playLevelUp()
                        hapticService.triggerSuccess()
                        soundHapticFeedback = "Level-up sound & haptic played"
                        Timber.d("[DevPortal] Played level-up sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayBadgeUnlockSound -> {
                        audioService.playBadgeUnlock()
                        hapticService.triggerBadgeUnlock()
                        soundHapticFeedback = "Badge unlock sound & haptic played"
                        Timber.d("[DevPortal] Played badge unlock sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayCountdownSound -> {
                        audioService.playCountdown()
                        soundHapticFeedback = "Countdown sound played"
                        Timber.d("[DevPortal] Played countdown sound")
                    }

                    is DeveloperPortalScreen.Event.PlayGoSound -> {
                        audioService.playGo()
                        hapticService.triggerSuccess()
                        soundHapticFeedback = "GO! sound & haptic played"
                        Timber.d("[DevPortal] Played GO! sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayPerfectScore -> {
                        audioService.playPerfectScore()
                        hapticService.triggerSuccess()
                        soundHapticFeedback = "Perfect score sound played"
                        Timber.d("[DevPortal] Played perfect score sound & haptic")
                    }

                    is DeveloperPortalScreen.Event.PlayStreakContinue -> {
                        audioService.playStreakContinue()
                        soundHapticFeedback = "Streak continue sound played"
                        Timber.d("[DevPortal] Played streak continue sound")
                    }

                    is DeveloperPortalScreen.Event.PlayWarning -> {
                        audioService.playWarning()
                        soundHapticFeedback = "Warning sound played"
                        Timber.d("[DevPortal] Played warning sound")
                    }

                    is DeveloperPortalScreen.Event.ToggleBackgroundMusic -> {
                        if (isBackgroundMusicPlaying) {
                            audioService.stopBackgroundMusic()
                            audioService.setMusicEnabled(false)
                            isBackgroundMusicPlaying = false
                            soundHapticFeedback = "Background music stopped"
                            Timber.d("[DevPortal] Stopped background music")
                        } else {
                            audioService.setMusicEnabled(true)
                            audioService.startBackgroundMusic()
                            isBackgroundMusicPlaying = true
                            soundHapticFeedback = "Background music started"
                            Timber.d("[DevPortal] Started background music")
                        }
                    }

                    is DeveloperPortalScreen.Event.UpdateGradeLevel -> {
                        profileUpdateResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                Timber.d("[DevPortal] Updating grade level to ${event.gradeLevel}")
                                userProfileRepository.updateGradeLevel(event.gradeLevel)
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Grade level updated to ${event.gradeLevel.displayName}"
                                }
                                Timber.d("[DevPortal] Grade level updated successfully")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to update grade level")
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Update failed: ${e.message}"
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.UpdateAdaptiveDifficulty -> {
                        profileUpdateResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                Timber.d("[DevPortal] Updating adaptive difficulty to ${event.enabled}")
                                userProfileRepository.updateAdaptiveDifficulty(event.enabled)
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Adaptive difficulty ${if (event.enabled) "enabled" else "disabled"}"
                                }
                                Timber.d("[DevPortal] Adaptive difficulty updated successfully")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to update adaptive difficulty")
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Update failed: ${e.message}"
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.UpdateProfileName -> {
                        profileUpdateResultMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                Timber.d("[DevPortal] Updating profile name to '${event.name}'")
                                userProfileRepository.updateName(event.name)
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Name updated"
                                }
                                Timber.d("[DevPortal] Profile name updated successfully")
                            } catch (e: Exception) {
                                Timber.e(e, "[DevPortal] Failed to update profile name")
                                withContext(Dispatchers.Main) {
                                    profileUpdateResultMessage = "Update failed: ${e.message}"
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }

                    is DeveloperPortalScreen.Event.NavigateToMathRace -> {
                        Timber.d("[DevPortal] Navigating to Math Race")
                        navigator.goTo(
                            dev.hossain.mathtutor.ui.mathrace
                                .MathRaceScreen(),
                        )
                    }

                    is DeveloperPortalScreen.Event.NavigateToMemoryMatch -> {
                        Timber.d("[DevPortal] Navigating to Memory Match")
                        navigator.goTo(
                            dev.hossain.mathtutor.ui.memorymatch
                                .MemoryMatchScreen(),
                        )
                    }

                    is DeveloperPortalScreen.Event.NavigateToNumberSequence -> {
                        Timber.d("[DevPortal] Navigating to Number Sequence")
                        navigator.goTo(
                            dev.hossain.mathtutor.ui.numbersequence
                                .NumberSequenceScreen(),
                        )
                    }

                    is DeveloperPortalScreen.Event.ViewColorPalette -> {
                        Timber.d("[DevPortal] Navigating to Color Palette Viewer")
                        navigator.goTo(ColorPaletteViewerScreen)
                    }
                }
            }
        }

        /**
         * Imports 6 sample custom challenges with various problem types.
         * Returns the count of successfully imported challenges.
         */
        private suspend fun importSampleChallenges(): Int {
            val challenges =
                listOf(
                    // 1. Addition Practice (Addition with Grade 1 range)
                    ChallengeImportSpec.Generated(
                        title = "Quick Addition",
                        subtitle = "Practice adding numbers 0-10",
                        operation = MathOperation.ADDITION,
                        problemCount = 10,
                        numberRange = NumberRange(0, 10),
                    ),
                    // 2. Subtraction Practice (Subtraction with Grade 1 range)
                    ChallengeImportSpec.Generated(
                        title = "Quick Subtraction",
                        subtitle = "Practice subtracting numbers 0-10",
                        operation = MathOperation.SUBTRACTION,
                        problemCount = 10,
                        numberRange = NumberRange(0, 10),
                    ),
                    // 3. Multiplication Basics (Multiplication with Grade 2 range)
                    ChallengeImportSpec.Generated(
                        title = "Multiply by 5",
                        subtitle = "Practice multiplying numbers by 5",
                        operation = MathOperation.MULTIPLICATION,
                        problemCount = 10,
                        numberRange = NumberRange(1, 10),
                    ),
                    // 4. Division Basics
                    ChallengeImportSpec.Generated(
                        title = "Divide by 2",
                        subtitle = "Practice dividing even numbers by 2",
                        operation = MathOperation.DIVISION,
                        problemCount = 10,
                        numberRange = NumberRange(2, 20),
                    ),
                    // 5. Mixed Operations (Custom problems)
                    ChallengeImportSpec.Explicit(
                        title = "Number Bonds to 10",
                        subtitle = "Find pairs that make 10",
                        problems =
                            listOf(
                                ProblemSpec(1, 9, MathOperation.ADDITION),
                                ProblemSpec(2, 8, MathOperation.ADDITION),
                                ProblemSpec(3, 7, MathOperation.ADDITION),
                                ProblemSpec(4, 6, MathOperation.ADDITION),
                                ProblemSpec(5, 5, MathOperation.ADDITION),
                                ProblemSpec(6, 4, MathOperation.ADDITION),
                                ProblemSpec(7, 3, MathOperation.ADDITION),
                                ProblemSpec(8, 2, MathOperation.ADDITION),
                                ProblemSpec(9, 1, MathOperation.ADDITION),
                                ProblemSpec(10, 0, MathOperation.ADDITION),
                            ),
                    ),
                    // 6. Mixed Operations (Addition & Subtraction)
                    ChallengeImportSpec.Explicit(
                        title = "Mixed Operations Review",
                        subtitle = "Practice addition and subtraction together",
                        problems =
                            listOf(
                                ProblemSpec(3, 2, MathOperation.ADDITION),
                                ProblemSpec(8, 2, MathOperation.SUBTRACTION),
                                ProblemSpec(4, 3, MathOperation.ADDITION),
                                ProblemSpec(7, 1, MathOperation.SUBTRACTION),
                                ProblemSpec(6, 2, MathOperation.ADDITION),
                                ProblemSpec(9, 3, MathOperation.SUBTRACTION),
                                ProblemSpec(5, 4, MathOperation.ADDITION),
                                ProblemSpec(10, 5, MathOperation.SUBTRACTION),
                                ProblemSpec(7, 3, MathOperation.ADDITION),
                                ProblemSpec(6, 1, MathOperation.SUBTRACTION),
                            ),
                    ),
                )

            var count = 0
            val createdChallengeIds = mutableListOf<Pair<String, String>>() // (title, id)
            for (spec in challenges) {
                val result = customChallengeService.createChallengeFromSpec(spec)
                if (result.isSuccess) {
                    count++
                    val challenge = result.getOrNull()
                    if (challenge != null) {
                        createdChallengeIds.add(challenge.title to challenge.id)
                        Timber.d("[DevPortal] Successfully imported challenge: ${spec.title}")
                    }
                } else {
                    Timber.e(
                        result.exceptionOrNull(),
                        "[DevPortal] Failed to import challenge: ${spec.title}",
                    )
                }
            }

            // Add practice sessions for 2 of the challenges
            // Quick Addition: 3 sessions at 90% accuracy
            val quickAdditionChallenge = createdChallengeIds.find { it.first == "Quick Addition" }
            if (quickAdditionChallenge != null) {
                repeat(3) { sessionIndex ->
                    val now = Instant.now()
                    val session =
                        ChallengePracticeSession(
                            startTime = now.minusSeconds((3 - sessionIndex).toLong() * 300), // Stagger sessions
                            endTime = now.minusSeconds((3 - sessionIndex).toLong() * 300).plusSeconds(120),
                            problemsAttempted = 10,
                            correctAnswers = 9, // 90% accuracy
                            totalTimeMs = 120000,
                        )
                    try {
                        customChallengeService.recordPracticeSession(quickAdditionChallenge.second, session)
                        Timber.d("[DevPortal] Recorded session $sessionIndex for Quick Addition (90% accuracy)")
                    } catch (e: Exception) {
                        Timber.e(e, "[DevPortal] Failed to record session for Quick Addition")
                    }
                }
            }

            // Quick Subtraction: 1 session at 45% accuracy
            val quickSubtractionChallenge = createdChallengeIds.find { it.first == "Quick Subtraction" }
            if (quickSubtractionChallenge != null) {
                val now = Instant.now()
                val session =
                    ChallengePracticeSession(
                        startTime = now.minusSeconds(600),
                        endTime = now.minusSeconds(600).plusSeconds(90),
                        problemsAttempted = 10,
                        correctAnswers = 5, // 50% accuracy (close to 45%)
                        totalTimeMs = 90000,
                    )
                try {
                    customChallengeService.recordPracticeSession(quickSubtractionChallenge.second, session)
                    Timber.d("[DevPortal] Recorded session for Quick Subtraction (50% accuracy)")
                } catch (e: Exception) {
                    Timber.e(e, "[DevPortal] Failed to record session for Quick Subtraction")
                }
            }

            return count
        }
    }
