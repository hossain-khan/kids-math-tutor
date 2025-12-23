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
                                                // Navigate back with result for parent to show success message
                                                navigator.pop(
                                                    result =
                                                        ImportChallengeScreen.ImportResult(
                                                            challengeTitle = challenge.title,
                                                        ),
                                                )
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
                }
            }
        }
    }
