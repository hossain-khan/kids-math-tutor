# Phase 5: Audio Feedback & Accessibility

**Duration**: 2 weeks  
**Goal**: Multi-sensory learning experience with comprehensive accessibility  
**Status**: 🔴 Not Started

---

## Overview

This phase transforms the app into a rich, multi-sensory learning experience while ensuring it's accessible to all children. By the end:
1. Audio feedback enhances the learning experience (success sounds, error sounds, optional music)
2. Haptic feedback provides tactile confirmation for interactions
3. App is fully accessible with TalkBack support, high contrast mode, and dynamic text sizing
4. Parents can customize audio and haptic settings

**Key Principle**: Every child learns differently. Engage multiple senses while ensuring the app works for everyone, regardless of ability.

---

## Features Breakdown

### 1. Audio System

#### Sound Design Philosophy
- **Success Sounds**: Positive, encouraging, never jarring
- **Error Sounds**: Gentle, supportive, not punitive
- **Background Music**: Optional, calming, low-volume
- **Voice Feedback**: Read problems aloud (future enhancement)

#### Audio Assets Required

```
app/src/main/res/raw/
├── success_01.mp3          # Soft chime (correct answer)
├── success_02.mp3          # Gentle bell (perfect session)
├── success_03.mp3          # Celebratory (badge unlock)
├── error_gentle.mp3        # Soft "try again" tone
├── badge_unlock.mp3        # Special achievement sound
├── streak_continue.mp3     # Daily streak maintained
├── level_up.mp3            # Grade level increased
└── background_music.mp3    # Optional ambient music
```

#### AudioService Interface

```kotlin
interface AudioService {
    fun playSuccess()
    fun playPerfectScore()
    fun playBadgeUnlock()
    fun playError()
    fun playStreakContinue()
    fun playLevelUp()
    
    fun startBackgroundMusic()
    fun stopBackgroundMusic()
    fun pauseBackgroundMusic()
    fun resumeBackgroundMusic()
    
    fun setMusicEnabled(enabled: Boolean)
    fun setSoundEffectsEnabled(enabled: Boolean)
    fun setVolume(volume: Float) // 0.0 to 1.0
    
    fun release()
}
```

#### AudioService Implementation with Media3 ExoPlayer

```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AudioServiceImpl constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : AudioService {
    
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    private val soundEffects = mutableMapOf<SoundType, Int>()
    
    private var musicPlayer: ExoPlayer? = null
    private var isMusicEnabled = true
    private var areSoundEffectsEnabled = true
    private var currentVolume = 1.0f
    
    enum class SoundType {
        SUCCESS, PERFECT_SCORE, BADGE_UNLOCK, ERROR, 
        STREAK_CONTINUE, LEVEL_UP
    }
    
    init {
        loadSoundEffects()
        observePreferences()
    }
    
    private fun loadSoundEffects() {
        soundEffects[SoundType.SUCCESS] = soundPool.load(context, R.raw.success_01, 1)
        soundEffects[SoundType.PERFECT_SCORE] = soundPool.load(context, R.raw.success_02, 1)
        soundEffects[SoundType.BADGE_UNLOCK] = soundPool.load(context, R.raw.success_03, 1)
        soundEffects[SoundType.ERROR] = soundPool.load(context, R.raw.error_gentle, 1)
        soundEffects[SoundType.STREAK_CONTINUE] = soundPool.load(context, R.raw.streak_continue, 1)
        soundEffects[SoundType.LEVEL_UP] = soundPool.load(context, R.raw.level_up, 1)
    }
    
    private fun observePreferences() {
        // Observe user preferences for audio settings
        // Update isMusicEnabled, areSoundEffectsEnabled, currentVolume
    }
    
    override fun playSuccess() {
        playSound(SoundType.SUCCESS)
    }
    
    override fun playPerfectScore() {
        playSound(SoundType.PERFECT_SCORE)
    }
    
    override fun playBadgeUnlock() {
        playSound(SoundType.BADGE_UNLOCK)
    }
    
    override fun playError() {
        playSound(SoundType.ERROR)
    }
    
    override fun playStreakContinue() {
        playSound(SoundType.STREAK_CONTINUE)
    }
    
    override fun playLevelUp() {
        playSound(SoundType.LEVEL_UP)
    }
    
    private fun playSound(type: SoundType) {
        if (!areSoundEffectsEnabled) return
        
        soundEffects[type]?.let { soundId ->
            soundPool.play(
                soundId,
                currentVolume,
                currentVolume,
                1,
                0,
                1.0f
            )
        }
    }
    
    override fun startBackgroundMusic() {
        if (!isMusicEnabled) return
        
        if (musicPlayer == null) {
            musicPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(
                    RawResourceDataSource.buildRawResourceUri(R.raw.background_music)
                )
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                volume = currentVolume * 0.3f // Background music at 30% of volume
                prepare()
            }
        }
        
        musicPlayer?.play()
    }
    
    override fun stopBackgroundMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }
    
    override fun pauseBackgroundMusic() {
        musicPlayer?.pause()
    }
    
    override fun resumeBackgroundMusic() {
        if (isMusicEnabled) {
            musicPlayer?.play()
        }
    }
    
    override fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            stopBackgroundMusic()
        }
    }
    
    override fun setSoundEffectsEnabled(enabled: Boolean) {
        areSoundEffectsEnabled = enabled
    }
    
    override fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        musicPlayer?.volume = currentVolume * 0.3f
    }
    
    override fun release() {
        soundPool.release()
        stopBackgroundMusic()
    }
}
```

#### Integration with Math Practice

```kotlin
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@Composable
fun MathPracticePresenter(
    @Assisted screen: MathPracticeScreen,
    @Assisted navigator: Navigator,
    problemGenerator: ProblemGenerator,
    audioService: AudioService, // Injected
    hapticService: HapticService // Injected
): MathPracticeScreen.State {
    
    // ... existing code ...
    
    fun checkAnswer() {
        val isCorrect = currentAnswer == currentProblem?.correctAnswer?.toString()
        
        if (isCorrect) {
            audioService.playSuccess()
            hapticService.triggerSuccess()
            // ... rest of success logic
        } else {
            audioService.playError()
            hapticService.triggerError()
            // ... rest of error logic
        }
    }
    
    // ... rest of presenter
}
```

---

### 2. Haptic Feedback System

#### HapticService Interface

```kotlin
interface HapticService {
    fun triggerSuccess()
    fun triggerError()
    fun triggerBadgeUnlock()
    fun triggerButtonClick()
    fun triggerLongPress()
    
    fun setHapticsEnabled(enabled: Boolean)
}
```

#### HapticService Implementation

```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class HapticServiceImpl constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : HapticService {
    
    private var isHapticsEnabled = true
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    init {
        observePreferences()
    }
    
    private fun observePreferences() {
        // Observe haptics preference from repository
    }
    
    override fun triggerSuccess() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50) // 50ms
        }
    }
    
    override fun triggerError() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 100, 50), -1)
        }
    }
    
    override fun triggerBadgeUnlock() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 100, 50, 150)
            val amplitudes = intArrayOf(0, 128, 0, 192, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 100, 50, 150), -1)
        }
    }
    
    override fun triggerButtonClick() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10) // Very short
        }
    }
    
    override fun triggerLongPress() {
        if (!isHapticsEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
    
    override fun setHapticsEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
    }
}
```

#### Haptic Feedback in UI Components

```kotlin
@Composable
fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticService: HapticService
) {
    Button(
        onClick = {
            hapticService.triggerButtonClick()
            onClick()
        },
        modifier = modifier
            .size(80.dp)
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
```

---

### 3. Accessibility Improvements

#### 3.1 Dynamic Text Sizing

```kotlin
@Composable
fun AccessibleText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val textSize = when {
        // Read from accessibility settings
        LocalConfiguration.current.fontScale > 1.3f -> style.fontSize * 1.2f
        else -> style.fontSize
    }
    
    Text(
        text = text,
        style = style.copy(fontSize = textSize),
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        }
    )
}
```

#### 3.2 High Contrast Mode

```kotlin
data class AccessibilitySettings(
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false
)

@Composable
fun HighContrastTheme(
    isHighContrast: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isHighContrast) {
        darkColorScheme(
            primary = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFFFFFFFF),
            onPrimaryContainer = Color(0xFF000000),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            error = Color(0xFFFF0000),
            onError = Color(0xFFFFFFFF)
        )
    } else {
        // Use default Material 3 dynamic colors
        dynamicDarkColorScheme(LocalContext.current)
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

#### 3.3 TalkBack Support with Content Descriptions

```kotlin
@Composable
fun MathProblemDisplay(
    problem: MathProblem,
    modifier: Modifier = Modifier
) {
    val operationSymbol = when (problem.operation) {
        MathOperation.ADDITION -> "+"
        MathOperation.SUBTRACTION -> "-"
        MathOperation.MULTIPLICATION -> "×"
        MathOperation.DIVISION -> "÷"
        MathOperation.MIXED -> "?"
    }
    
    val operationName = when (problem.operation) {
        MathOperation.ADDITION -> "plus"
        MathOperation.SUBTRACTION -> "minus"
        MathOperation.MULTIPLICATION -> "times"
        MathOperation.DIVISION -> "divided by"
        MathOperation.MIXED -> ""
    }
    
    // TalkBack will read: "3 plus 5 equals"
    val contentDesc = "${problem.num1} $operationName ${problem.num2} equals"
    
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = contentDesc
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${problem.num1}",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = operationSymbol,
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "${problem.num2}",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "=",
            style = MaterialTheme.typography.displayMedium
        )
    }
}
```

#### 3.4 Accessible Buttons with Clear Actions

```kotlin
@Composable
fun CheckAnswerButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .semantics {
                contentDescription = if (enabled) {
                    "Check your answer"
                } else {
                    "Enter an answer first"
                }
                // Mark as important for accessibility
                role = Role.Button
            }
    ) {
        Text("Check Answer")
    }
}
```

#### 3.5 Focus Management for Keyboard Navigation

```kotlin
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        // Auto-focus first button for keyboard navigation
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = modifier
            .semantics {
                contentDescription = "Number pad for entering answers"
            }
    ) {
        // Number buttons 1-9
        for (row in 0..2) {
            Row {
                for (col in 1..3) {
                    val number = row * 3 + col
                    NumberButton(
                        number = number,
                        onClick = { onNumberClick(number) },
                        modifier = if (number == 1) {
                            Modifier.focusRequester(focusRequester)
                        } else {
                            Modifier
                        }
                    )
                }
            }
        }
        
        // Bottom row: backspace, 0, (empty)
        Row {
            IconButton(
                onClick = onBackspace,
                modifier = Modifier.semantics {
                    contentDescription = "Backspace, delete last digit"
                }
            ) {
                Icon(Icons.Default.Backspace, contentDescription = null)
            }
            
            NumberButton(
                number = 0,
                onClick = { onNumberClick(0) }
            )
        }
    }
}
```

---

### 4. Settings for Audio & Haptics

#### Audio & Haptic Settings Screen

```
┌─────────────────────────────────────┐
│  [←]    Audio & Haptics      [?]    │
├─────────────────────────────────────┤
│                                     │
│   Sound Effects                     │
│                                     │
│   [✓] Enable sound effects          │
│                                     │
│   Volume: ▮▮▮▮▮▮▮▯▯▯                │
│                                     │
│   ────────────────────────────      │
│                                     │
│   Background Music                  │
│                                     │
│   [✓] Play background music         │
│                                     │
│   ────────────────────────────      │
│                                     │
│   Haptic Feedback                   │
│                                     │
│   [✓] Vibrate on interactions       │
│                                     │
│   ────────────────────────────      │
│                                     │
│   Accessibility                     │
│                                     │
│   [✓] High contrast mode            │
│   [✓] Large text                    │
│                                     │
└─────────────────────────────────────┘
```

#### AudioHapticSettingsScreen (Circuit)

```kotlin
@Parcelize
data class AudioHapticSettingsScreen() : Screen {
    data class State(
        val soundEffectsEnabled: Boolean,
        val backgroundMusicEnabled: Boolean,
        val hapticsEnabled: Boolean,
        val volume: Float,
        val highContrastEnabled: Boolean,
        val largeTextEnabled: Boolean,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    sealed interface Event : CircuitUiEvent {
        data class ToggleSoundEffects(val enabled: Boolean) : Event
        data class ToggleBackgroundMusic(val enabled: Boolean) : Event
        data class ToggleHaptics(val enabled: Boolean) : Event
        data class SetVolume(val volume: Float) : Event
        data class ToggleHighContrast(val enabled: Boolean) : Event
        data class ToggleLargeText(val enabled: Boolean) : Event
    }
}

@CircuitInject(AudioHapticSettingsScreen::class, AppScope::class)
@Composable
fun AudioHapticSettingsPresenter(
    @Assisted screen: AudioHapticSettingsScreen,
    @Assisted navigator: Navigator,
    userPreferencesRepository: UserPreferencesRepository,
    audioService: AudioService,
    hapticService: HapticService
): AudioHapticSettingsScreen.State {
    
    val preferences by userPreferencesRepository.getPreferences().collectAsState(
        initial = UserPreferences()
    )
    
    return AudioHapticSettingsScreen.State(
        soundEffectsEnabled = preferences.soundEffectsEnabled,
        backgroundMusicEnabled = preferences.backgroundMusicEnabled,
        hapticsEnabled = preferences.hapticsEnabled,
        volume = preferences.volume,
        highContrastEnabled = preferences.highContrastEnabled,
        largeTextEnabled = preferences.largeTextEnabled
    ) { event ->
        when (event) {
            is AudioHapticSettingsScreen.Event.ToggleSoundEffects -> {
                audioService.setSoundEffectsEnabled(event.enabled)
                // Save to repository
            }
            is AudioHapticSettingsScreen.Event.ToggleBackgroundMusic -> {
                audioService.setMusicEnabled(event.enabled)
                if (event.enabled) {
                    audioService.startBackgroundMusic()
                } else {
                    audioService.stopBackgroundMusic()
                }
                // Save to repository
            }
            is AudioHapticSettingsScreen.Event.ToggleHaptics -> {
                hapticService.setHapticsEnabled(event.enabled)
                // Save to repository
            }
            is AudioHapticSettingsScreen.Event.SetVolume -> {
                audioService.setVolume(event.volume)
                // Save to repository
            }
            is AudioHapticSettingsScreen.Event.ToggleHighContrast -> {
                // Save to repository, trigger theme change
            }
            is AudioHapticSettingsScreen.Event.ToggleLargeText -> {
                // Save to repository, trigger text size change
            }
        }
    }
}

@CircuitInject(AudioHapticSettingsScreen::class, AppScope::class)
@Composable
fun AudioHapticSettingsUi(
    state: AudioHapticSettingsScreen.State,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sound Effects Section
        Text(
            text = "Sound Effects",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable sound effects")
            Switch(
                checked = state.soundEffectsEnabled,
                onCheckedChange = { 
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleSoundEffects(it))
                }
            )
        }
        
        Text(
            text = "Volume",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        Slider(
            value = state.volume,
            onValueChange = { 
                state.eventSink(AudioHapticSettingsScreen.Event.SetVolume(it))
            },
            enabled = state.soundEffectsEnabled || state.backgroundMusicEnabled,
            modifier = Modifier.semantics {
                contentDescription = "Volume slider, current volume ${(state.volume * 100).toInt()} percent"
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Background Music Section
        Text(
            text = "Background Music",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Play background music")
            Switch(
                checked = state.backgroundMusicEnabled,
                onCheckedChange = { 
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleBackgroundMusic(it))
                }
            )
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Haptics Section
        Text(
            text = "Haptic Feedback",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vibrate on interactions")
            Switch(
                checked = state.hapticsEnabled,
                onCheckedChange = { 
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleHaptics(it))
                }
            )
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Accessibility Section
        Text(
            text = "Accessibility",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("High contrast mode")
            Switch(
                checked = state.highContrastEnabled,
                onCheckedChange = { 
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleHighContrast(it))
                }
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Large text")
            Switch(
                checked = state.largeTextEnabled,
                onCheckedChange = { 
                    state.eventSink(AudioHapticSettingsScreen.Event.ToggleLargeText(it))
                }
            )
        }
    }
}
```

---

### 5. UserPreferences Update

#### Updated UserPreferences Data Model

```kotlin
data class UserPreferences(
    val soundEffectsEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val volume: Float = 0.7f,
    val highContrastEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false
)
```

#### UserPreferencesRepository Updates

```kotlin
interface UserPreferencesRepository {
    fun getPreferences(): Flow<UserPreferences>
    suspend fun setSoundEffectsEnabled(enabled: Boolean)
    suspend fun setBackgroundMusicEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setVolume(volume: Float)
    suspend fun setHighContrastEnabled(enabled: Boolean)
    suspend fun setLargeTextEnabled(enabled: Boolean)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserPreferencesRepositoryImpl constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    
    companion object {
        private val SOUND_EFFECTS_KEY = booleanPreferencesKey("sound_effects_enabled")
        private val BACKGROUND_MUSIC_KEY = booleanPreferencesKey("background_music_enabled")
        private val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
        private val VOLUME_KEY = floatPreferencesKey("volume")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast_enabled")
        private val LARGE_TEXT_KEY = booleanPreferencesKey("large_text_enabled")
    }
    
    override fun getPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { prefs ->
            UserPreferences(
                soundEffectsEnabled = prefs[SOUND_EFFECTS_KEY] ?: true,
                backgroundMusicEnabled = prefs[BACKGROUND_MUSIC_KEY] ?: false,
                hapticsEnabled = prefs[HAPTICS_KEY] ?: true,
                volume = prefs[VOLUME_KEY] ?: 0.7f,
                highContrastEnabled = prefs[HIGH_CONTRAST_KEY] ?: false,
                largeTextEnabled = prefs[LARGE_TEXT_KEY] ?: false
            )
        }
    }
    
    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SOUND_EFFECTS_KEY] = enabled
        }
    }
    
    override suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BACKGROUND_MUSIC_KEY] = enabled
        }
    }
    
    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[HAPTICS_KEY] = enabled
        }
    }
    
    override suspend fun setVolume(volume: Float) {
        dataStore.edit { prefs ->
            prefs[VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }
    
    override suspend fun setHighContrastEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[HIGH_CONTRAST_KEY] = enabled
        }
    }
    
    override suspend fun setLargeTextEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[LARGE_TEXT_KEY] = enabled
        }
    }
}
```

---

## Technical Implementation

### Dependencies

#### Update gradle/libs.versions.toml

```toml
[versions]
media3 = "1.4.1"

[libraries]
androidx-media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
androidx-media3-common = { module = "androidx.media3:media3-common", version.ref = "media3" }
```

#### Update app/build.gradle.kts

```kotlin
dependencies {
    // Media3 for audio playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
}
```

### Permissions

#### Update AndroidManifest.xml

```xml
<!-- For haptic feedback -->
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## Testing Strategy

### Unit Tests

#### AudioService Tests

```kotlin
@Test
fun `audio service plays success sound when enabled`() {
    val audioService = AudioServiceImpl(context, mockPreferencesRepo)
    audioService.setSoundEffectsEnabled(true)
    
    audioService.playSuccess()
    
    // Verify sound played (using spy or mock)
}

@Test
fun `audio service does not play sound when disabled`() {
    val audioService = AudioServiceImpl(context, mockPreferencesRepo)
    audioService.setSoundEffectsEnabled(false)
    
    audioService.playSuccess()
    
    // Verify sound not played
}

@Test
fun `background music starts when enabled`() {
    val audioService = AudioServiceImpl(context, mockPreferencesRepo)
    audioService.setMusicEnabled(true)
    
    audioService.startBackgroundMusic()
    
    // Verify music player is playing
}
```

#### HapticService Tests

```kotlin
@Test
fun `haptic service triggers vibration when enabled`() {
    val hapticService = HapticServiceImpl(context, mockPreferencesRepo)
    hapticService.setHapticsEnabled(true)
    
    hapticService.triggerSuccess()
    
    // Verify vibrator called (using spy)
}

@Test
fun `haptic service does not vibrate when disabled`() {
    val hapticService = HapticServiceImpl(context, mockPreferencesRepo)
    hapticService.setHapticsEnabled(false)
    
    hapticService.triggerSuccess()
    
    // Verify vibrator not called
}
```

### Accessibility Tests

#### TalkBack Content Description Tests

```kotlin
@Test
fun `math problem has correct content description for TalkBack`() {
    composeTestRule.setContent {
        MathProblemDisplay(
            problem = MathProblem(3, 5, MathOperation.ADDITION, 8)
        )
    }
    
    composeTestRule
        .onNode(hasContentDescription("3 plus 5 equals"))
        .assertExists()
}

@Test
fun `check answer button has descriptive content for TalkBack`() {
    composeTestRule.setContent {
        CheckAnswerButton(onClick = {}, enabled = true)
    }
    
    composeTestRule
        .onNode(hasContentDescription("Check your answer"))
        .assertExists()
}
```

#### High Contrast Tests

```kotlin
@Test
fun `high contrast theme uses black and white colors`() {
    composeTestRule.setContent {
        HighContrastTheme(isHighContrast = true) {
            Text("Test", color = MaterialTheme.colorScheme.onSurface)
        }
    }
    
    // Verify colors are high contrast (black/white)
}
```

### Manual Testing Checklist

**Audio**
- [ ] Success sound plays on correct answer
- [ ] Error sound plays on incorrect answer
- [ ] Badge unlock sound plays when badge earned
- [ ] Background music plays when enabled
- [ ] Background music loops continuously
- [ ] Volume slider adjusts all sounds
- [ ] Audio settings persist across app restarts

**Haptics**
- [ ] Correct answer triggers pleasant vibration
- [ ] Incorrect answer triggers distinct vibration
- [ ] Badge unlock triggers celebratory pattern
- [ ] Button clicks trigger subtle feedback
- [ ] Haptics can be disabled in settings
- [ ] Haptics settings persist

**Accessibility**
- [ ] TalkBack reads math problems correctly
- [ ] TalkBack reads button actions clearly
- [ ] High contrast mode increases readability
- [ ] Large text mode increases font sizes
- [ ] All interactive elements have content descriptions
- [ ] Keyboard navigation works (Tab key)
- [ ] Focus indicators visible
- [ ] Color is not the only indicator (use icons/shapes)

**Real Device Testing**
- [ ] Test on device with TalkBack enabled
- [ ] Test on device with font size increased
- [ ] Test on device with high contrast enabled
- [ ] Test with headphones
- [ ] Test with device on silent mode
- [ ] Test with Do Not Disturb enabled

---

## Migration Plan from Phase 4

### Step 1: Audio System Foundation (Days 1-3)
1. Add Media3 dependencies
2. Create audio assets (success, error, badge sounds)
3. Implement AudioService interface and implementation
4. Create SoundPool for sound effects
5. Create ExoPlayer for background music
6. Unit tests for AudioService
7. Test audio playback

### Step 2: Haptic Feedback System (Days 4-5)
1. Add VIBRATE permission
2. Implement HapticService interface and implementation
3. Create different vibration patterns
4. Handle different Android versions (pre-Q, Q+)
5. Unit tests for HapticService
6. Test haptic patterns on devices

### Step 3: Integrate Audio & Haptics (Days 6-7)
1. Inject AudioService and HapticService into presenters
2. Add audio feedback to MathPracticePresenter
3. Add audio feedback to BadgeUnlock
4. Add audio feedback to StreakContinue
5. Add haptic feedback to all buttons
6. Add haptic feedback to number pad
7. Test integration in all screens

### Step 4: Accessibility Improvements (Days 8-10)
1. Add content descriptions to all interactive elements
2. Create AccessibleText component
3. Implement high contrast theme
4. Add semantic properties to composables
5. Test with TalkBack enabled
6. Fix any accessibility issues found

### Step 5: Settings Screen (Days 11-12)
1. Update UserPreferences data model
2. Update UserPreferencesRepository
3. Create AudioHapticSettingsScreen Circuit
4. Add volume slider
5. Add toggle switches for all settings
6. Wire up navigation from main settings
7. Test settings persistence

### Step 6: Polish & Testing (Days 13-14)
1. End-to-end testing with audio and haptics
2. Accessibility testing with TalkBack
3. Test on multiple devices
4. Performance testing (audio latency)
5. Real child testing (with and without disabilities)
6. Bug fixes
7. CHANGELOG.md update
8. Documentation

---

## Success Metrics

- ✅ Audio feedback works reliably on all devices
- ✅ Haptic feedback appropriate and not annoying
- ✅ TalkBack reads all content correctly
- ✅ High contrast mode improves readability
- ✅ No audio lag or performance issues
- ✅ Settings persist correctly
- ✅ Passes Android Accessibility Scanner

---

## Definition of Done

- ✅ Audio system implemented with Media3
- ✅ Sound effects for success, error, badge unlock
- ✅ Optional background music
- ✅ Haptic feedback for all interactions
- ✅ TalkBack support with proper content descriptions
- ✅ High contrast mode implemented
- ✅ Large text support
- ✅ Settings screen for audio & haptics
- ✅ All settings persist
- ✅ All tests passing
- ✅ TalkBack tested on real device
- ✅ CHANGELOG.md updated
- ✅ Accessibility guidelines followed
- ✅ Real child testing (including children with disabilities)

---

## Accessibility Guidelines Reference

### WCAG 2.1 Level AA Compliance
1. **Perceivable**: Content must be perceivable
   - Text alternatives for non-text content
   - Time-based media alternatives
   - Adaptable content structure
   - Distinguishable colors (contrast ratio ≥4.5:1)

2. **Operable**: UI must be operable
   - Keyboard accessible
   - Enough time to read/use content
   - No seizure-inducing content
   - Navigable and findable

3. **Understandable**: Content must be understandable
   - Readable text
   - Predictable functionality
   - Input assistance

4. **Robust**: Content must be robust
   - Compatible with assistive technologies
   - Valid markup

### Android Accessibility Best Practices
- Use `contentDescription` for all ImageViews/Icons
- Use `semantics` for all interactive Composables
- Support TalkBack gestures
- Minimum touch target: 48dp × 48dp
- Clear focus indicators
- Logical reading order
- Avoid time-based interactions

---

*Document created: December 16, 2025*  
*Phase status: 🔴 Not Started*  
*Target completion: Week 11 (after Phase 4)*
