package dev.hossain.mathtutor.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import timber.log.Timber

/**
 * Implementation of [AudioService] using SoundPool for sound effects
 * and ExoPlayer for background music.
 *
 * SoundPool is used for short sound effects because it provides:
 * - Low latency playback
 * - Efficient memory usage for short clips
 * - Support for concurrent sounds
 *
 * ExoPlayer is used for background music because it provides:
 * - Efficient looping
 * - Better memory management for longer audio
 * - Volume control without re-loading
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AudioServiceImpl
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : AudioService {
        // Sound effect IDs loaded into SoundPool
        private var successSoundId: Int = 0
        private var perfectScoreSoundId: Int = 0
        private var badgeUnlockSoundId: Int = 0
        private var errorSoundId: Int = 0
        private var streakContinueSoundId: Int = 0
        private var levelUpSoundId: Int = 0

        // Game-specific sound effect IDs
        private var countdownSoundId: Int = 0
        private var goSoundId: Int = 0
        private var warningSoundId: Int = 0

        // Sound loading state
        // Number of sounds we expect to load into SoundPool; set when loadSounds() runs
        private var soundsLoadExpected: Int = 0
        private var soundsLoadCompleted: Int = 0
        private var soundsLoaded: Boolean = false
        private val soundLoadQueue: MutableList<Int> = mutableListOf() // queue plays until loaded

        // Settings
        private var soundEffectsEnabled: Boolean = true
        private var musicEnabled: Boolean = false // Default OFF - user must enable in settings
        private var volume: Float = AudioConstants.DEFAULT_SOUND_EFFECTS_VOLUME

        // Sound load listeners (dev UI can register to receive updates)
        private val soundLoadListeners: MutableList<(loaded: Boolean, sampleIds: Map<String, Int>) -> Unit> = mutableListOf()

        // Background music volume is 60% of main volume
        private val musicVolumeMultiplier: Float = AudioConstants.BACKGROUND_MUSIC_VOLUME_MULTIPLIER

        // Track whether SoundPool has been initialized to avoid unnecessary initialization in release()
        // Note: This flag is set within the lazy initializer. In a single-threaded context (main thread),
        // this is safe. For multi-threaded access, additional synchronization would be needed.
        private var soundPoolInitialized = false

        // SoundPool for short sound effects (max 3 concurrent streams)
        private val soundPool: SoundPool by lazy {
            soundPoolInitialized = true
            val audioAttributes =
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            SoundPool
                .Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()
                .also { loadSounds(it) }
        }

        // ExoPlayer for background music
        private var exoPlayer: ExoPlayer? = null

        /**
         * Load all sound effects into SoundPool.
         */
        private fun loadSounds(pool: SoundPool) {
            Timber.d("[AudioService] Loading sound effects...")

            // Load sounds and remember how many we requested
            val ids =
                listOf(
                    pool.load(context, R.raw.success_01_alt2, 1),
                    pool.load(context, R.raw.success_02, 1),
                    pool.load(context, R.raw.success_03, 1),
                    pool.load(context, R.raw.error_gentle, 1),
                    pool.load(context, R.raw.streak_continue, 1),
                    pool.load(context, R.raw.level_up, 1),
                    pool.load(context, R.raw.countdown_tick, 1),
                    pool.load(context, R.raw.countdown_go, 1),
                    pool.load(context, R.raw.time_warning, 1),
                )

            // Assign explicit ids to fields (order matches above)
            successSoundId = ids[0]
            perfectScoreSoundId = ids[1]
            badgeUnlockSoundId = ids[2]
            errorSoundId = ids[3]
            streakContinueSoundId = ids[4]
            levelUpSoundId = ids[5]
            countdownSoundId = ids[6]
            goSoundId = ids[7]
            warningSoundId = ids[8]

            soundsLoadExpected = ids.size
            soundsLoadCompleted = 0
            soundsLoaded = false

            // Listen for load completion and flush any queued plays once ready
            pool.setOnLoadCompleteListener { sp, sampleId, status ->
                if (status == 0) {
                    soundsLoadCompleted += 1
                    Timber.d("[AudioService] Sound loaded: sampleId=$sampleId (completed=$soundsLoadCompleted/$soundsLoadExpected)")
                    if (soundsLoadCompleted >= soundsLoadExpected) {
                        soundsLoaded = true
                        Timber.d("[AudioService] All sound effects loaded")

                        // Notify listeners about loaded state and sample IDs
                        val sampleMap =
                            mapOf(
                                "success" to successSoundId,
                                "perfect" to perfectScoreSoundId,
                                "badge" to badgeUnlockSoundId,
                                "error" to errorSoundId,
                                "streak" to streakContinueSoundId,
                                "levelUp" to levelUpSoundId,
                                "countdown" to countdownSoundId,
                                "go" to goSoundId,
                                "warning" to warningSoundId,
                            )
                        soundLoadListeners.forEach { listener ->
                            try {
                                listener(true, sampleMap)
                            } catch (e: Exception) {
                                Timber.e(e, "[AudioService] Error notifying soundLoadListener")
                            }
                        }

                        // Play any queued sound requests now that loading is complete
                        if (soundLoadQueue.isNotEmpty()) {
                            Timber.d("[AudioService] Flushing ${soundLoadQueue.size} queued sound(s)")
                            soundLoadQueue.forEach { id ->
                                try {
                                    sp.play(id, volume, volume, 1, 0, 1.0f)
                                } catch (e: Exception) {
                                    Timber.e(e, "[AudioService] Failed to play queued sound $id")
                                }
                            }
                            soundLoadQueue.clear()
                        }
                    }
                } else {
                    Timber.w("[AudioService] Sound load failed for sampleId=$sampleId status=$status")
                }
            }

            // If someone registers a listener later, we want to be able to push current state; keep a local list
            // (listeners are called when loading completes)
        }

        /**
         * Play a sound effect if sound effects are enabled.
         */
        private fun playSound(soundId: Int) {
            if (!soundEffectsEnabled) {
                Timber.d("[AudioService] Sound skipped (enabled=$soundEffectsEnabled)")
                return
            }

            // Check if device is in silent or vibrate mode
            if (isDeviceAudioSuppressed()) {
                Timber.d("[AudioService] Sound skipped (device in silent/vibrate mode)")
                return
            }

            // Ensure SoundPool is initialized (this will trigger loadSounds())
            val sp = soundPool

            if (!soundsLoaded) {
                // SoundPool may not have finished loading samples yet — queue this play
                Timber.d("[AudioService] Sound not yet loaded, queueing soundId=$soundId")
                soundLoadQueue.add(soundId)
                return
            }

            if (soundId == 0) {
                Timber.w("[AudioService] Invalid soundId=0 after loading - skipping")
                return
            }

            try {
                sp.play(soundId, volume, volume, 1, 0, 1.0f)
                Timber.d("[AudioService] Playing sound $soundId at volume $volume")
            } catch (e: Exception) {
                Timber.e(e, "[AudioService] Failed to play sound $soundId")
            }
        }

        // ==================== Sound Effects ====================

        override fun playSuccess() {
            // Ensure SoundPool initialized so IDs are assigned, then play
            val sp = soundPool
            playSound(successSoundId)
        }

        override fun playPerfectScore() {
            val sp = soundPool
            playSound(perfectScoreSoundId)
        }

        override fun playBadgeUnlock() {
            val sp = soundPool
            playSound(badgeUnlockSoundId)
        }

        override fun playError() {
            val sp = soundPool
            playSound(errorSoundId)
        }

        override fun playStreakContinue() {
            val sp = soundPool
            playSound(streakContinueSoundId)
        }

        override fun playLevelUp() {
            val sp = soundPool
            playSound(levelUpSoundId)
        }

        // ==================== Game Sound Effects ====================

        override fun playCountdown() {
            val sp = soundPool
            playSound(countdownSoundId)
        }

        override fun playGo() {
            val sp = soundPool
            playSound(goSoundId)
        }

        override fun playWarning() {
            val sp = soundPool
            playSound(warningSoundId)
        }

        // ==================== Background Music ====================

        @OptIn(UnstableApi::class)
        private fun initializeExoPlayer(): ExoPlayer {
            Timber.d("[AudioService] Initializing ExoPlayer for background music")
            return ExoPlayer
                .Builder(context)
                .build()
                .apply {
                    // Build the raw resource URI
                    val musicUri = "android.resource://${context.packageName}/${R.raw.swan_lake_music_box}"
                    val mediaItem = MediaItem.fromUri(musicUri)
                    setMediaItem(mediaItem)
                    repeatMode = Player.REPEAT_MODE_ALL
                    setVolume(volume * musicVolumeMultiplier)
                    prepare()
                }
        }

        override fun startBackgroundMusic() {
            if (!musicEnabled) {
                Timber.d("[AudioService] Music disabled, not starting")
                return
            }

            if (exoPlayer == null) {
                exoPlayer = initializeExoPlayer()
            }

            exoPlayer?.let { player ->
                player.setVolume(volume * musicVolumeMultiplier)
                player.play()
                Timber.d("[AudioService] Background music started")
            }
        }

        override fun stopBackgroundMusic() {
            exoPlayer?.let { player ->
                player.stop()
                player.release()
                Timber.d("[AudioService] Background music stopped and player released")
            }
            exoPlayer = null
        }

        override fun pauseBackgroundMusic() {
            exoPlayer?.pause()
            Timber.d("[AudioService] Background music paused")
        }

        override fun resumeBackgroundMusic() {
            if (!musicEnabled) {
                Timber.d("[AudioService] Music disabled, not resuming")
                return
            }
            exoPlayer?.play()
            Timber.d("[AudioService] Background music resumed")
        }

        // ==================== Settings ====================

        override fun setMusicEnabled(enabled: Boolean) {
            musicEnabled = enabled
            Timber.d("[AudioService] Music enabled: $enabled")

            if (!enabled) {
                pauseBackgroundMusic()
            }
        }

        override fun registerSoundLoadListener(listener: (loaded: Boolean, sampleIds: Map<String, Int>) -> Unit) {
            // Add listener and call immediately with current state
            soundLoadListeners.add(listener)
            try {
                val sampleMap =
                    mapOf(
                        "success" to successSoundId,
                        "perfect" to perfectScoreSoundId,
                        "badge" to badgeUnlockSoundId,
                        "error" to errorSoundId,
                        "streak" to streakContinueSoundId,
                        "levelUp" to levelUpSoundId,
                        "countdown" to countdownSoundId,
                        "go" to goSoundId,
                        "warning" to warningSoundId,
                    )
                listener(soundsLoaded, sampleMap)
            } catch (e: Exception) {
                Timber.e(e, "[AudioService] Error calling soundLoadListener upon registration")
            }
        }

        override fun unregisterSoundLoadListener(listener: (loaded: Boolean, sampleIds: Map<String, Int>) -> Unit) {
            soundLoadListeners.remove(listener)
        }

        override fun setSoundEffectsEnabled(enabled: Boolean) {
            soundEffectsEnabled = enabled
            Timber.d("[AudioService] Sound effects enabled: $enabled")
        }

        override fun setVolume(volume: Float) {
            this.volume = volume.coerceIn(0f, 1f)
            Timber.d("[AudioService] Volume set to ${this.volume}")

            // Update ExoPlayer volume if playing
            exoPlayer?.setVolume(this.volume * musicVolumeMultiplier)
        }

        override fun isDeviceAudioSuppressed(): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            return audioManager?.ringerMode?.let { mode ->
                mode == AudioManager.RINGER_MODE_SILENT || mode == AudioManager.RINGER_MODE_VIBRATE
            } ?: false
        }

        // ==================== Lifecycle ====================

        override fun release() {
            Timber.d("[AudioService] Releasing audio resources")
            // Only release SoundPool if it was actually initialized
            if (soundPoolInitialized) {
                soundPool.release()
            }
            exoPlayer?.release()
            exoPlayer = null
        }
    }
