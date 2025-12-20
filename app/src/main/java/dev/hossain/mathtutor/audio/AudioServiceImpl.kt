package dev.hossain.mathtutor.audio

import android.content.Context
import android.media.AudioAttributes
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

        // Settings
        private var soundEffectsEnabled: Boolean = true
        private var musicEnabled: Boolean = false // Default OFF - user must enable in settings
        private var volume: Float = 1.0f

        // Background music volume is 30% of main volume
        private val musicVolumeMultiplier: Float = 0.3f

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
            successSoundId = pool.load(context, R.raw.success_01, 1)
            perfectScoreSoundId = pool.load(context, R.raw.success_02, 1)
            badgeUnlockSoundId = pool.load(context, R.raw.success_03, 1)
            errorSoundId = pool.load(context, R.raw.error_gentle, 1)
            streakContinueSoundId = pool.load(context, R.raw.streak_continue, 1)
            levelUpSoundId = pool.load(context, R.raw.level_up, 1)
            // Game sounds - reuse existing sounds with appropriate variations
            countdownSoundId = pool.load(context, R.raw.countdown_tick, 1)
            goSoundId = pool.load(context, R.raw.countdown_go, 1)
            warningSoundId = pool.load(context, R.raw.time_warning, 1)
            Timber.d("[AudioService] Sound effects loaded")
        }

        /**
         * Play a sound effect if sound effects are enabled.
         */
        private fun playSound(soundId: Int) {
            if (!soundEffectsEnabled || soundId == 0) {
                Timber.d("[AudioService] Sound skipped (enabled=$soundEffectsEnabled, soundId=$soundId)")
                return
            }
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
            Timber.d("[AudioService] Playing sound $soundId at volume $volume")
        }

        // ==================== Sound Effects ====================

        override fun playSuccess() {
            playSound(successSoundId)
        }

        override fun playPerfectScore() {
            playSound(perfectScoreSoundId)
        }

        override fun playBadgeUnlock() {
            playSound(badgeUnlockSoundId)
        }

        override fun playError() {
            playSound(errorSoundId)
        }

        override fun playStreakContinue() {
            playSound(streakContinueSoundId)
        }

        override fun playLevelUp() {
            playSound(levelUpSoundId)
        }

        // ==================== Game Sound Effects ====================

        override fun playCountdown() {
            playSound(countdownSoundId)
        }

        override fun playGo() {
            playSound(goSoundId)
        }

        override fun playWarning() {
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
