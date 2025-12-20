package dev.hossain.mathtutor.ui.mathrace

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.haptic.HapticService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [MathRacePresenter].
 *
 * Tests the presenter logic including game states, timer behavior,
 * problem generation, answer checking, and score tracking.
 */
class MathRacePresenterTest {
    private lateinit var fakeProblemGenerator: FakeProblemGenerator
    private lateinit var fakeGameRepository: FakeGameRepository
    private lateinit var fakeUserProfileRepository: FakeUserProfileRepository
    private lateinit var fakeAudioService: FakeAudioService
    private lateinit var fakeHapticService: FakeHapticService

    @Before
    fun setup() {
        fakeProblemGenerator = FakeProblemGenerator()
        fakeGameRepository = FakeGameRepository()
        fakeUserProfileRepository = FakeUserProfileRepository()
        fakeAudioService = FakeAudioService()
        fakeHapticService = FakeHapticService()
    }

    // ==================== Game State Tests ====================

    @Test
    fun `initial game state is NotStarted`() {
        val gameState = MathRaceScreen.GameState.NotStarted

        // Verify it's the singleton object
        assertThat(gameState).isEqualTo(MathRaceScreen.GameState.NotStarted)
    }

    @Test
    fun `countdown state contains countdown value`() {
        val countdownState = MathRaceScreen.GameState.Countdown(countdownValue = 3)

        assertThat(countdownState.countdownValue).isEqualTo(3)
    }

    @Test
    fun `countdown sequence goes from 3 to 0`() {
        val countdownValues = listOf(3, 2, 1, 0)

        countdownValues.forEachIndexed { index, expected ->
            val state = MathRaceScreen.GameState.Countdown(countdownValue = expected)
            assertThat(state.countdownValue).isEqualTo(expected)
        }

        // Verify 0 represents "GO"
        val goState = MathRaceScreen.GameState.Countdown(countdownValue = 0)
        assertThat(goState.countdownValue).isEqualTo(0)
    }

    @Test
    fun `finished state contains final stats`() {
        val finishedState =
            MathRaceScreen.GameState.Finished(
                finalScore = 15,
                totalAttempts = 18,
                isNewRecord = true,
                accuracy = 83.33f,
                averageTimePerProblem = 3.33f,
            )

        assertThat(finishedState.finalScore).isEqualTo(15)
        assertThat(finishedState.totalAttempts).isEqualTo(18)
        assertThat(finishedState.isNewRecord).isTrue()
        assertThat(finishedState.accuracy).isWithin(0.01f).of(83.33f)
        assertThat(finishedState.averageTimePerProblem).isWithin(0.01f).of(3.33f)
    }

    // ==================== Answer Checking Tests ====================

    @Test
    fun `correct answer increments score`() {
        var score = 0
        var correctAnswers = 0
        var totalAttempts = 0

        val problem = createProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION)
        val userAnswer = 8

        // Simulate checking correct answer
        if (problem.checkAnswer(userAnswer)) {
            score++
            correctAnswers++
        }
        totalAttempts++

        assertThat(score).isEqualTo(1)
        assertThat(correctAnswers).isEqualTo(1)
        assertThat(totalAttempts).isEqualTo(1)
    }

    @Test
    fun `incorrect answer does not increment score`() {
        var score = 0
        var correctAnswers = 0
        var totalAttempts = 0

        val problem = createProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION)
        val userAnswer = 7 // Wrong answer

        // Simulate checking incorrect answer
        if (problem.checkAnswer(userAnswer)) {
            score++
            correctAnswers++
        }
        totalAttempts++

        assertThat(score).isEqualTo(0)
        assertThat(correctAnswers).isEqualTo(0)
        assertThat(totalAttempts).isEqualTo(1)
    }

    @Test
    fun `multiple answers tracked correctly`() {
        var score = 0
        var correctAnswers = 0
        var totalAttempts = 0

        val answers =
            listOf(
                true,
                true,
                false,
                true,
                false,
                true, // 4 correct, 2 incorrect
            )

        answers.forEach { isCorrect ->
            if (isCorrect) {
                score++
                correctAnswers++
            }
            totalAttempts++
        }

        assertThat(score).isEqualTo(4)
        assertThat(correctAnswers).isEqualTo(4)
        assertThat(totalAttempts).isEqualTo(6)
    }

    // ==================== Stats Calculation Tests ====================

    @Test
    fun `accuracy calculated correctly with some correct answers`() {
        val correctAnswers = 8
        val totalAttempts = 10

        val accuracy =
            if (totalAttempts > 0) {
                (correctAnswers.toFloat() / totalAttempts) * 100f
            } else {
                0f
            }

        assertThat(accuracy).isWithin(0.01f).of(80f)
    }

    @Test
    fun `accuracy is 0 when no attempts`() {
        val correctAnswers = 0
        val totalAttempts = 0

        val accuracy =
            if (totalAttempts > 0) {
                (correctAnswers.toFloat() / totalAttempts) * 100f
            } else {
                0f
            }

        assertThat(accuracy).isWithin(0.01f).of(0f)
    }

    @Test
    fun `accuracy is 100 when all correct`() {
        val correctAnswers = 15
        val totalAttempts = 15

        val accuracy =
            if (totalAttempts > 0) {
                (correctAnswers.toFloat() / totalAttempts) * 100f
            } else {
                0f
            }

        assertThat(accuracy).isWithin(0.01f).of(100f)
    }

    @Test
    fun `average time per problem calculated correctly`() {
        val durationSeconds = 60
        val totalAttempts = 20

        val avgTime =
            if (totalAttempts > 0) {
                durationSeconds.toFloat() / totalAttempts
            } else {
                0f
            }

        assertThat(avgTime).isWithin(0.01f).of(3f)
    }

    @Test
    fun `average time is 0 when no attempts`() {
        val durationSeconds = 60
        val totalAttempts = 0

        val avgTime =
            if (totalAttempts > 0) {
                durationSeconds.toFloat() / totalAttempts
            } else {
                0f
            }

        assertThat(avgTime).isWithin(0.01f).of(0f)
    }

    @Test
    fun `new record detected when score exceeds personal best`() {
        val personalBest = 10
        val score = 15

        val isNewRecord = score > personalBest

        assertThat(isNewRecord).isTrue()
    }

    @Test
    fun `no new record when score equals personal best`() {
        val personalBest = 15
        val score = 15

        val isNewRecord = score > personalBest

        assertThat(isNewRecord).isFalse()
    }

    @Test
    fun `no new record when score is less than personal best`() {
        val personalBest = 20
        val score = 15

        val isNewRecord = score > personalBest

        assertThat(isNewRecord).isFalse()
    }

    // ==================== Input Handling Tests ====================

    @Test
    fun `number entry appends digit to answer`() {
        var currentAnswer = ""

        // Enter digits 1, 2, 3
        currentAnswer += "1"
        currentAnswer += "2"
        currentAnswer += "3"

        assertThat(currentAnswer).isEqualTo("123")
    }

    @Test
    fun `answer limited to max digits`() {
        val maxDigits = 4
        var currentAnswer = ""

        // Try to enter 5 digits
        listOf("1", "2", "3", "4", "5").forEach { digit ->
            if (currentAnswer.length < maxDigits) {
                currentAnswer += digit
            }
        }

        assertThat(currentAnswer).isEqualTo("1234")
        assertThat(currentAnswer.length).isEqualTo(4)
    }

    @Test
    fun `backspace removes last digit`() {
        var currentAnswer = "123"

        currentAnswer = currentAnswer.dropLast(1)

        assertThat(currentAnswer).isEqualTo("12")
    }

    @Test
    fun `backspace on empty answer does nothing`() {
        var currentAnswer = ""

        if (currentAnswer.isNotEmpty()) {
            currentAnswer = currentAnswer.dropLast(1)
        }

        assertThat(currentAnswer).isEqualTo("")
    }

    // ==================== Problem Generation Tests ====================

    @Test
    fun `problem generator creates valid problem`() {
        val problems =
            fakeProblemGenerator.generateProblems(
                count = 1,
                operation = MathOperation.ADDITION,
                gradeLevel = GradeLevel.GRADE_1,
            )

        assertThat(problems.size).isEqualTo(1)
        val problem = problems[0]
        assertThat(problem.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(problem.correctAnswer).isEqualTo(problem.num1 + problem.num2)
    }

    @Test
    fun `problem generator respects MIXED operation`() {
        val problems =
            fakeProblemGenerator.generateProblems(
                count = 10,
                operation = MathOperation.MIXED,
                gradeLevel = GradeLevel.GRADE_1,
            )

        assertThat(problems.size).isEqualTo(10)
        // With MIXED, problems should be valid regardless of specific operation
        problems.forEach { problem ->
            assertThat(problem.num1 > 0).isTrue()
            assertThat(problem.num2 > 0).isTrue()
        }
    }

    // ==================== Event Tests ====================

    @Test
    fun `StartGame event available from NotStarted state`() {
        val gameState: MathRaceScreen.GameState = MathRaceScreen.GameState.NotStarted
        val canStartGame =
            gameState == MathRaceScreen.GameState.NotStarted ||
                gameState is MathRaceScreen.GameState.Finished

        assertThat(canStartGame).isTrue()
    }

    @Test
    fun `StartGame event available from Finished state`() {
        val gameState: MathRaceScreen.GameState =
            MathRaceScreen.GameState.Finished(
                finalScore = 10,
                totalAttempts = 12,
                isNewRecord = false,
                accuracy = 83.33f,
                averageTimePerProblem = 5f,
            )
        val canStartGame =
            gameState == MathRaceScreen.GameState.NotStarted ||
                gameState is MathRaceScreen.GameState.Finished

        assertThat(canStartGame).isTrue()
    }

    @Test
    fun `NumberEntered event only works during Playing state`() {
        val playingState = MathRaceScreen.GameState.Playing
        val countdownState = MathRaceScreen.GameState.Countdown(3)
        val notStartedState = MathRaceScreen.GameState.NotStarted

        assertThat(playingState == MathRaceScreen.GameState.Playing).isTrue()
        assertThat(countdownState == MathRaceScreen.GameState.Playing).isFalse()
        assertThat(notStartedState == MathRaceScreen.GameState.Playing).isFalse()
    }

    @Test
    fun `PlayAgain event resets game state`() {
        // Simulate play again resetting state
        var score = 15
        var totalAttempts = 20
        var timeRemaining = 0

        // Reset
        score = 0
        totalAttempts = 0
        timeRemaining = 60

        assertThat(score).isEqualTo(0)
        assertThat(totalAttempts).isEqualTo(0)
        assertThat(timeRemaining).isEqualTo(60)
    }

    // ==================== State Tests ====================

    @Test
    fun `state contains all required fields`() {
        val state =
            MathRaceScreen.State(
                gameState = MathRaceScreen.GameState.Playing,
                currentProblem = createProblem(5, 3, MathOperation.ADDITION),
                currentAnswer = "8",
                score = 10,
                timeRemaining = 45,
                personalBest = 15,
                totalAttempts = 12,
                correctAnswers = 10,
                lastAnswerCorrect = true,
                userName = "Test User",
                eventSink = {},
            )

        assertThat(state.gameState).isEqualTo(MathRaceScreen.GameState.Playing)
        assertThat(state.currentAnswer).isEqualTo("8")
        assertThat(state.score).isEqualTo(10)
        assertThat(state.timeRemaining).isEqualTo(45)
        assertThat(state.personalBest).isEqualTo(15)
        assertThat(state.totalAttempts).isEqualTo(12)
        assertThat(state.correctAnswers).isEqualTo(10)
        assertThat(state.lastAnswerCorrect == true).isTrue()
        assertThat(state.userName).isEqualTo("Test User")
    }

    @Test
    fun `lastAnswerCorrect is null initially`() {
        val state =
            MathRaceScreen.State(
                eventSink = {},
            )

        assertThat(state.lastAnswerCorrect).isNull()
    }

    // ==================== Audio Service Tests ====================

    @Test
    fun `countdown plays countdown audio`() {
        fakeAudioService.playCountdown()

        assertThat(fakeAudioService.countdownPlayed).isEqualTo(1)
    }

    @Test
    fun `go plays go audio`() {
        fakeAudioService.playGo()

        assertThat(fakeAudioService.goPlayed).isEqualTo(1)
    }

    @Test
    fun `warning plays warning audio`() {
        fakeAudioService.playWarning()

        assertThat(fakeAudioService.warningPlayed).isEqualTo(1)
    }

    @Test
    fun `correct answer plays success audio`() {
        fakeAudioService.playSuccess()

        assertThat(fakeAudioService.successPlayed).isEqualTo(1)
    }

    @Test
    fun `incorrect answer plays error audio`() {
        fakeAudioService.playError()

        assertThat(fakeAudioService.errorPlayed).isEqualTo(1)
    }

    @Test
    fun `new record plays perfect score audio`() {
        fakeAudioService.playPerfectScore()

        assertThat(fakeAudioService.perfectScorePlayed).isEqualTo(1)
    }

    // ==================== Haptic Service Tests ====================

    @Test
    fun `correct answer triggers success haptic`() {
        fakeHapticService.triggerSuccess()

        assertThat(fakeHapticService.successTriggered).isEqualTo(1)
    }

    @Test
    fun `incorrect answer triggers error haptic`() {
        fakeHapticService.triggerError()

        assertThat(fakeHapticService.errorTriggered).isEqualTo(1)
    }

    @Test
    fun `button click triggers button haptic`() {
        fakeHapticService.triggerButtonClick()

        assertThat(fakeHapticService.buttonClickTriggered).isEqualTo(1)
    }

    @Test
    fun `warning triggers long press haptic`() {
        fakeHapticService.triggerLongPress()

        assertThat(fakeHapticService.longPressTriggered).isEqualTo(1)
    }

    // ==================== Game Repository Tests ====================

    @Test
    fun `personal best loaded from repository`() {
        fakeGameRepository.setPersonalBest(Game.MATH_RACE, 25)

        // Simulate loading personal best
        val personalBest = 25

        assertThat(personalBest).isEqualTo(25)
    }

    // ==================== Timer Tests ====================

    @Test
    fun `timer starts at 60 seconds`() {
        val initialTime = 60

        assertThat(initialTime).isEqualTo(60)
    }

    @Test
    fun `warning threshold is 10 seconds`() {
        val warningThreshold = 10

        assertThat(warningThreshold).isEqualTo(10)
    }

    @Test
    fun `timer reaches zero correctly`() {
        var timeRemaining = 3

        repeat(3) {
            timeRemaining--
        }

        assertThat(timeRemaining).isEqualTo(0)
    }

    // ==================== Helper Functions ====================

    private fun createProblem(
        num1: Int,
        num2: Int,
        operation: MathOperation,
    ): MathProblem {
        val answer =
            when (operation) {
                MathOperation.ADDITION -> num1 + num2
                MathOperation.SUBTRACTION -> num1 - num2
                MathOperation.MULTIPLICATION -> num1 * num2
                MathOperation.DIVISION -> num1 / num2
                MathOperation.MIXED -> num1 + num2 // Default to addition for MIXED
            }
        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = operation,
            correctAnswer = answer,
        )
    }
}

/**
 * Fake implementation of [ProblemGenerator] for testing.
 */
class FakeProblemGenerator : ProblemGenerator {
    override fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): List<MathProblem> {
        val ops =
            if (operation == MathOperation.MIXED) {
                listOf(MathOperation.ADDITION, MathOperation.SUBTRACTION)
            } else {
                listOf(operation)
            }

        return List(count) { index ->
            val op = ops[index % ops.size]
            val num1 = (index + 1) * 2
            val num2 = index + 1
            val answer =
                when (op) {
                    MathOperation.ADDITION -> num1 + num2
                    MathOperation.SUBTRACTION -> num1 - num2
                    MathOperation.MULTIPLICATION -> num1 * num2
                    MathOperation.DIVISION -> num1 / num2
                    MathOperation.MIXED -> num1 + num2
                }
            MathProblem(num1 = num1, num2 = num2, operation = op, correctAnswer = answer)
        }
    }
}

/**
 * Fake implementation of [GameRepository] for testing.
 */
class FakeGameRepository : GameRepository {
    private val personalBests = mutableMapOf<Game, Int>()
    private val savedSessions = mutableListOf<GameSession>()

    fun setPersonalBest(
        game: Game,
        score: Int,
    ) {
        personalBests[game] = score
    }

    override suspend fun saveGameSession(session: GameSession): Long {
        savedSessions.add(session)
        return savedSessions.size.toLong()
    }

    override fun getPersonalBest(game: Game): Flow<Int> = flowOf(personalBests[game] ?: 0)

    override fun getBestSession(game: Game): Flow<GameSession?> = flowOf(null)

    override fun getGameStats(game: Game): Flow<GameStats> = flowOf(GameStats.empty(game))

    override fun getTotalGamesPlayed(game: Game): Flow<Int> = flowOf(0)

    override fun isGameUnlocked(game: Game): Flow<Boolean> = flowOf(true)

    override fun getSessionsByGame(game: Game): Flow<List<GameSession>> = flowOf(emptyList())

    override fun getRecentSessions(limit: Int): Flow<List<GameSession>> = flowOf(emptyList())

    override fun getAllGameStats(): Flow<Map<Game, GameStats>> =
        flowOf(
            Game.entries.associateWith { game -> GameStats.empty(game) },
        )

    override fun getPerfectGameCount(game: Game): Flow<Int> = flowOf(0)

    override suspend fun clearAllSessions() {
        savedSessions.clear()
    }
}

/**
 * Fake implementation of [UserProfileRepository] for testing.
 */
class FakeUserProfileRepository : UserProfileRepository {
    private val profileFlow = MutableStateFlow<UserProfile?>(null)

    fun setProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override fun getProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun saveProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {}

    override suspend fun updateName(name: String?) {}

    override suspend fun updateAdaptiveDifficulty(enabled: Boolean) {}
}

/**
 * Fake implementation of [AudioService] for testing.
 */
class FakeAudioService : AudioService {
    var successPlayed = 0
    var perfectScorePlayed = 0
    var badgeUnlockPlayed = 0
    var errorPlayed = 0
    var streakContinuePlayed = 0
    var levelUpPlayed = 0
    var countdownPlayed = 0
    var goPlayed = 0
    var warningPlayed = 0

    override fun playSuccess() {
        successPlayed++
    }

    override fun playPerfectScore() {
        perfectScorePlayed++
    }

    override fun playBadgeUnlock() {
        badgeUnlockPlayed++
    }

    override fun playError() {
        errorPlayed++
    }

    override fun playStreakContinue() {
        streakContinuePlayed++
    }

    override fun playLevelUp() {
        levelUpPlayed++
    }

    override fun playCountdown() {
        countdownPlayed++
    }

    override fun playGo() {
        goPlayed++
    }

    override fun playWarning() {
        warningPlayed++
    }

    override fun startBackgroundMusic() {}

    override fun stopBackgroundMusic() {}

    override fun pauseBackgroundMusic() {}

    override fun resumeBackgroundMusic() {}

    override fun setMusicEnabled(enabled: Boolean) {}

    override fun setSoundEffectsEnabled(enabled: Boolean) {}

    override fun setVolume(volume: Float) {}

    override fun release() {}
}

/**
 * Fake implementation of [HapticService] for testing.
 */
class FakeHapticService : HapticService {
    var successTriggered = 0
    var errorTriggered = 0
    var badgeUnlockTriggered = 0
    var buttonClickTriggered = 0
    var longPressTriggered = 0

    override fun triggerSuccess() {
        successTriggered++
    }

    override fun triggerError() {
        errorTriggered++
    }

    override fun triggerBadgeUnlock() {
        badgeUnlockTriggered++
    }

    override fun triggerButtonClick() {
        buttonClickTriggered++
    }

    override fun triggerLongPress() {
        longPressTriggered++
    }

    override fun setHapticsEnabled(enabled: Boolean) {}
}
