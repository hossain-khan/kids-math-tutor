package dev.hossain.mathtutor.ui.importchallenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.PreviewData
import dev.hossain.mathtutor.domain.parser.ChallengeJsonParser
import dev.hossain.mathtutor.domain.parser.ValidationException
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import dev.hossain.mathtutor.domain.service.CustomChallengeService
import dev.hossain.mathtutor.ui.parentchallenges.ParentChallengesScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [ImportChallengeScreen].
 *
 * Manages the state and business logic for importing custom challenges,
 * including JSON validation, preview generation, and challenge creation.
 *
 * Parents can create challenge JSON using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 */
@AssistedInject
class ImportChallengePresenter
    constructor(
        @Assisted private val screen: ImportChallengeScreen,
        @Assisted private val navigator: Navigator,
        private val jsonParser: ChallengeJsonParser,
        private val challengeService: CustomChallengeService,
        private val challengeRepository: CustomChallengeRepository,
        private val userPreferencesRepository: dev.hossain.mathtutor.data.UserPreferencesRepository,
    ) : Presenter<ImportChallengeScreen.State> {
        @CircuitInject(ImportChallengeScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: ImportChallengeScreen,
                navigator: Navigator,
            ): ImportChallengePresenter
        }

        @Composable
        override fun present(): ImportChallengeScreen.State {
            val coroutineScope = rememberCoroutineScope()

            var jsonInput by rememberSaveable { mutableStateOf("") }
            var validationState by remember { mutableStateOf<ValidationState>(ValidationState.Idle) }
            var previewData by remember { mutableStateOf<PreviewData?>(null) }
            var isLoading by remember { mutableStateOf(false) }
            var detectedJsonFromShare by remember { mutableStateOf(false) }
            var isGuideExpanded by remember { mutableStateOf(true) }

            // Load guide expansion state from preferences
            LaunchedEffect(Unit) {
                userPreferencesRepository.isImportGuideExpanded.collect { expanded ->
                    isGuideExpanded = expanded
                }
            }

            // Initialize with shared text if provided
            LaunchedEffect(screen.prefilledJson) {
                screen.prefilledJson?.let { sharedText ->
                    val detectedJson = jsonParser.findJsonInText(sharedText)
                    if (detectedJson != null) {
                        jsonInput = detectedJson
                        detectedJsonFromShare = true
                        Timber.d("JSON detected from shared content")
                    } else {
                        // No JSON detected, show full shared text for manual extraction
                        jsonInput = sharedText
                        detectedJsonFromShare = false
                        Timber.d("No JSON detected in shared content, showing raw text")
                    }
                }
            }

            return ImportChallengeScreen.State(
                jsonInput = jsonInput,
                validationState = validationState,
                previewData = previewData,
                isLoading = isLoading,
                detectedJsonFromShare = detectedJsonFromShare,
                isGuideExpanded = isGuideExpanded,
            ) { event ->
                when (event) {
                    is ImportChallengeScreen.Event.JsonInputChanged -> {
                        jsonInput = event.input
                        // Reset validation when input changes
                        if (validationState !is ValidationState.Idle) {
                            validationState = ValidationState.Idle
                            previewData = null
                        }
                        // Clear the share detection flag when user manually edits
                        if (detectedJsonFromShare) {
                            detectedJsonFromShare = false
                        }
                    }

                    ImportChallengeScreen.Event.ValidateAndPreview -> {
                        coroutineScope.launch {
                            try {
                                // Parse the JSON
                                val parseResult = jsonParser.parseFromText(jsonInput)

                                parseResult
                                    .onSuccess { spec ->
                                        // Check for duplicate challenge
                                        val duplicateTitle = challengeService.findDuplicateChallenge(spec)
                                        if (duplicateTitle != null) {
                                            validationState =
                                                ValidationState.Invalid(
                                                    mapOf(
                                                        "duplicate" to
                                                            "This challenge already exists in your library: \"$duplicateTitle\"",
                                                    ),
                                                )
                                            previewData = null
                                            Timber.d("Duplicate challenge detected: $duplicateTitle")
                                            return@launch
                                        }

                                        // Generate preview
                                        try {
                                            val preview = challengeService.generatePreview(spec)
                                            validationState = ValidationState.Valid
                                            previewData = preview
                                            Timber.d("Preview generated successfully: ${preview.title}")
                                        } catch (e: Exception) {
                                            Timber.e(e, "Failed to generate preview")
                                            validationState =
                                                ValidationState.Invalid(
                                                    mapOf("general" to "Failed to generate preview: ${e.message}"),
                                                )
                                            previewData = null
                                        }
                                    }.onFailure { error ->
                                        Timber.e(error, "Failed to parse JSON")
                                        val errorMap =
                                            when (error) {
                                                is ValidationException -> {
                                                    error.fieldErrors
                                                }

                                                else -> {
                                                    mapOf(
                                                        "general" to
                                                            (
                                                                error.message
                                                                    ?: "Invalid JSON format"
                                                            ),
                                                    )
                                                }
                                            }
                                        validationState = ValidationState.Invalid(errorMap)
                                        previewData = null
                                    }
                            } catch (e: Exception) {
                                Timber.e(e, "Unexpected error during validation")
                                validationState =
                                    ValidationState.Invalid(
                                        mapOf("general" to "Unexpected error: ${e.message}"),
                                    )
                                previewData = null
                            }
                        }
                    }

                    ImportChallengeScreen.Event.SaveChallenge -> {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                // Parse the JSON again to get the spec
                                val parseResult = jsonParser.parseFromText(jsonInput)

                                parseResult
                                    .onSuccess { spec ->
                                        // Create the challenge
                                        val result = challengeService.createChallengeFromSpec(spec)

                                        result
                                            .onSuccess { challenge ->
                                                // Persist to repository
                                                challengeRepository.saveChallenge(challenge)
                                                Timber.d("ImportChallenge: Challenge saved: ${challenge.title}")

                                                // If we came from a deeplink (no back stack), navigate to ParentChallengesScreen
                                                // Otherwise, pop back to the previous screen
                                                if (screen.prefilledJson != null) {
                                                    // Deeplink scenario - go to challenges list
                                                    Timber.d("ImportChallenge: Deeplink import - navigating to ParentChallengesScreen")
                                                    navigator.resetRoot(ParentChallengesScreen)
                                                } else {
                                                    // Normal flow - pop back with result for parent to show success message
                                                    navigator.pop(
                                                        result =
                                                            ImportChallengeScreen.ImportResult(
                                                                challengeTitle = challenge.title,
                                                            ),
                                                    )
                                                }
                                            }.onFailure { error ->
                                                Timber.e(error, "Failed to create challenge")
                                                validationState =
                                                    ValidationState.Invalid(
                                                        mapOf(
                                                            "general" to
                                                                "Failed to save challenge: ${error.message}",
                                                        ),
                                                    )
                                            }
                                    }.onFailure { error ->
                                        Timber.e(error, "Failed to parse JSON for save")
                                        validationState =
                                            ValidationState.Invalid(
                                                mapOf(
                                                    "general" to
                                                        "Failed to parse JSON: ${error.message}",
                                                ),
                                            )
                                    }
                            } catch (e: Exception) {
                                Timber.e(e, "Unexpected error during save")
                                validationState =
                                    ValidationState.Invalid(
                                        mapOf("general" to "Failed to save: ${e.message}"),
                                    )
                            } finally {
                                isLoading = false
                            }
                        }
                    }

                    ImportChallengeScreen.Event.ClearInput -> {
                        jsonInput = ""
                        validationState = ValidationState.Idle
                        previewData = null
                        detectedJsonFromShare = false
                    }

                    ImportChallengeScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }

                    ImportChallengeScreen.Event.ToggleGuideExpanded -> {
                        isGuideExpanded = !isGuideExpanded
                        coroutineScope.launch {
                            userPreferencesRepository.setImportGuideExpanded(isGuideExpanded)
                            Timber.d("Import guide expanded state toggled: $isGuideExpanded")
                        }
                    }
                }
            }
        }
    }
