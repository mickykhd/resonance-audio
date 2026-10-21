package com.resonance.audio.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.resonance.audio.core.PlayerController
import com.resonance.audio.core.EqPresets
import com.resonance.audio.core.model.RepeatMode
import com.resonance.audio.core.model.EqBand
import com.resonance.audio.core.model.EqUiState
import com.resonance.audio.core.model.PlaybackUiState
import com.resonance.audio.core.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/** ExoPlayer + android.media.audiofx implementation of PlayerController. */
class ExoPlayerEngine(context: Context) : PlayerController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ctx: Context = context.applicationContext

    private val player: ExoPlayer = ExoPlayer.Builder(ctx)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        .setHandleAudioBecomingNoisy(true)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .build()

    /** Exposed so the media session service can wrap the very same player. */
    val playerForSession: ExoPlayer get() = player

    private val _state = MutableStateFlow(PlaybackUiState.Empty)
    override val state: StateFlow<PlaybackUiState> = _state

    private val _eq = MutableStateFlow(EqUiState.Empty)
    override val eq: StateFlow<EqUiState> = _eq

    private var queue: List<Track> = emptyList()
    private var sourceLabel: String = "Playing from Library"

    // ---- audio effects ----
    private var equalizer: Equalizer? = null
    private var bass: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var lastSessionId: Int = 0
    private var bassFrac: Float = 0f
    private var spatialFrac: Float = 0f
    private var currentPresetName: String = "Flat"

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = player.currentMediaItemIndex
                val track = queue.getOrNull(idx) ?: return
                currentPresetTick(track)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                tick()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                tick()
            }
        })
        scope.launch {
            while (isActive) {
                if (_state.value.hasCurrent && player.isPlaying) {
                    tick()
                }
                delay(250)
            }
        }
        scope.launch { initializeEqDefaults() }
    }

    private fun tick() {
        val idx = player.currentMediaItemIndex
        val track = queue.getOrNull(idx)
        val duration = if (player.duration > 0) player.duration else track?.durationMs ?: 0L
        val live = track?.isRemote == true || player.duration <= 0
        val pos = if (live) 0L else player.currentPosition.coerceAtLeast(0L)
        _state.value = _state.value.copy(
            hasCurrent = track != null,
            currentId = track?.id ?: "",
            title = track?.title ?: "",
            artist = track?.artist ?: "",
            formatLabel = track?.formatLabel ?: "",
            sourceLabel = sourceLabel,
            seed = track?.seed ?: 0,
            durationMs = if (live) 0 else duration,
            positionMs = pos,
            isPlaying = player.isPlaying,
            isLive = live,
            shuffle = player.shuffleModeEnabled,
            repeatMode = mapRepeat(player.repeatMode),
        )
    }

    private fun currentPresetTick(track: Track) {
        tick()
        maybeAttachEqualizer()
        _eq.value = _eq.value.copy(bitrateLabel = track.formatLabel)
    }

    // ---- PlayerController ----

    override fun playQueue(tracks: List<Track>, startIndex: Int, label: String) {
        queue = tracks
        sourceLabel = label
        val items = tracks.map { MediaItem.fromUri(parseAndroidUri(it.path)) }
        val index = startIndex.coerceIn(0, tracks.lastIndex)
        player.setMediaItems(items, index, 0L)
        player.repeatMode = Player.REPEAT_MODE_OFF
        player.shuffleModeEnabled = _state.value.shuffle
        player.prepare()
        player.play()
        startMediaSessionService()
        currentPresetTick(tracks[index])
        ensureOutputLabels()
    }

    private fun startMediaSessionService() {
        runCatching {
            ctx.startService(Intent(ctx, PlaybackService::class.java))
        }
    }

    override fun playLive(tracks: List<Track>, index: Int, label: String) {
        playQueue(tracks, index, label)
    }

    override fun playPause() {
        if (!player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            return
        }
        if (player.isPlaying) player.pause() else player.play()
        tick()
    }

    override fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        } else if (player.repeatMode == Player.REPEAT_MODE_ONE) {
            player.seekTo(0)
            player.play()
        }
        tick()
    }

    override fun previous() {
        if (player.currentPosition > 3_000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
        } else {
            player.seekTo(0)
        }
        tick()
    }

    override fun seekTo(ms: Long) {
        player.seekTo(ms.coerceAtLeast(0L))
        tick()
    }

    override fun setShuffle(on: Boolean) {
        player.shuffleModeEnabled = on
        tick()
    }

    override fun cycleRepeatMode(): RepeatMode {
        val next = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        tick()
        return mapRepeat(next)
    }

    // ---- EQ ----

    override suspend fun setEqBandLevel(index: Int, levelDb: Int) {
        val eq = equalizer ?: return
        val band = index.toShort()
        if (band >= eq.numberOfBands) return
        val range = eq.bandLevelRange
        val millibels = (levelDb * 100).coerceIn(range[0].toInt(), range[1].toInt()).toShort()
        eq.setBandLevel(band, millibels)
        currentPresetName = "Custom"
        publishBands()
    }

    override suspend fun applyEqPreset(name: String) {
        val preset = EqPresets.byName(name)
        currentPresetName = preset.name
        val eq = equalizer
        if (eq == null) {
            publishBands()
            return
        }
        val bands = eq.numberOfBands.toInt()
        val range = eq.bandLevelRange
        for (b in 0 until bands) {
            val frac = if (bands > 1) b.toFloat() / (bands - 1) else 0.5f
            val level = EqPresets.sampleFor(frac, preset)
            val mb = (level * 100).coerceIn(range[0].toInt(), range[1].toInt()).toShort()
            try {
                eq.setBandLevel(b.toShort(), mb)
            } catch (t: Throwable) {
                break
            }
        }
        publishBands()
    }

    override suspend fun setBassBoost(fraction: Float) {
        bassFrac = fraction
        val sessionId = player.audioSessionId
        val side = bass ?: attachBass(sessionId)
        side?.setStrength((fraction * 1000).toInt().coerceIn(0, 1000).toShort())
        _eq.value = _eq.value.copy(bassBoost = fraction)
    }

    override suspend fun setSpatial(fraction: Float) {
        spatialFrac = fraction
        val sessionId = player.audioSessionId
        val side = virtualizer ?: attachVirtualizer(sessionId)
        side?.setStrength((fraction * 1000).toInt().coerceIn(0, 1000).toShort())
        _eq.value = _eq.value.copy(spatial = fraction)
    }

    override fun release() {
        runCatching { equalizer?.enabled = false }
        runCatching { bass?.release() }
        runCatching { virtualizer?.release() }
        runCatching { equalizer?.release() }
        player.release()
    }

    // ---- helpers ----

    private fun ensureOutputLabels() {
        if (_eq.value.outputDevice.isEmpty()) {
            _eq.value = _eq.value.copy(outputDevice = outputDeviceLabel())
        }
        val current = queue.getOrNull(player.currentMediaItemIndex)
        if (current != null && _eq.value.bitrateLabel.isEmpty()) {
            _eq.value = _eq.value.copy(bitrateLabel = current.formatLabel)
        }
    }

    private fun outputDeviceLabel(): String {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            ?: return "Phone Speaker"
        return when {
            am.isBluetoothA2dpOn -> "Bluetooth A2DP"
            am.isWiredHeadsetOn -> "Wired Headset"
            else -> "Phone Speaker"
        }
    }

    private fun initializeEqDefaults() {
        val eq = equalizer
        if (eq == null) {
            _eq.value = EqUiState(
                available = true,
                bands = EqPresets.BAND_LABELS.mapIndexed { i, label ->
                    EqBand(freqLabel = label, levelDb = 0)
                },
                availablePresets = EqPresets.ALL.map { it.name },
                currentPreset = currentPresetName,
                bassBoost = bassFrac,
                spatial = spatialFrac,
                outputDevice = outputDeviceLabel(),
                bitrateLabel = "",
            )
            return
        }
        publishBands()
        _eq.value = _eq.value.copy(
            availablePresets = EqPresets.ALL.map { it.name },
            outputDevice = outputDeviceLabel(),
        )
    }

    private fun publishBands() {
        val eq = equalizer
        var bands = if (eq == null) {
            EqPresets.BAND_LABELS.map { EqBand(freqLabel = it, levelDb = 0) }
        } else {
            val range = eq.bandLevelRange
            val minDb = (range[0].toInt() / 100).coerceIn(-24, 0)
            val maxDb = (range[1].toInt() / 100).coerceIn(0, 24)
            (0 until eq.numberOfBands.toInt()).map { b ->
                val freqHz = eq.getCenterFreq(b.toShort()) / 1000
                EqBand(
                    freqLabel = bandLabel(freqHz),
                    levelDb = (eq.getBandLevel(b.toShort()).toInt() / 100),
                    minDb = minDb,
                    maxDb = maxDb,
                )
            }
        }
        if (bands.isEmpty()) {
            bands = EqPresets.BAND_LABELS.map { EqBand(freqLabel = it, levelDb = 0) }
        }
        _eq.value = _eq.value.copy(
            available = equalizer != null,
            bands = bands,
            currentPreset = currentPresetName,
        )
    }

    private fun bandLabel(hz: Int): String = if (hz >= 1000) {
        val khz = hz / 1000.0
        if (khz >= 10) "${khz.toInt()}k" else String.format(Locale.US, "%.0fk", khz)
    } else if (hz >= 100) {
        hz.toString()
    } else {
        hz.toString()
    }

    private fun maybeAttachEqualizer() {
        val sessionId = player.audioSessionId
        if (sessionId == 0 || sessionId == lastSessionId) return
        lastSessionId = sessionId
        try {
            val eq = Equalizer(1, sessionId)
            eq.enabled = true
            equalizer = eq
        } catch (t: Throwable) {
            equalizer = null
        }
        attachBass(sessionId)
        attachVirtualizer(sessionId)
        scope.launch {
            publishBands()
            _eq.value = _eq.value.copy(
                availablePresets = EqPresets.ALL.map { it.name },
                outputDevice = outputDeviceLabel(),
            )
            applyEqPreset(currentPresetName)
        }
    }

    private fun attachBass(sessionId: Int): BassBoost? {
        if (bass != null) return bass
        if (sessionId == 0) return null
        bass = try {
            val b = BassBoost(1, sessionId)
            b.enabled = true
            b
        } catch (t: Throwable) {
            null
        }
        return bass
    }

    private fun attachVirtualizer(sessionId: Int): Virtualizer? {
        if (virtualizer != null) return virtualizer
        if (sessionId == 0) return null
        virtualizer = try {
            val v = Virtualizer(1, sessionId)
            v.enabled = true
            v
        } catch (t: Throwable) {
            null
        }
        return virtualizer
    }

    private fun parseAndroidUri(path: String): android.net.Uri = when {
        path.startsWith("content://") -> android.net.Uri.parse(path)
        path.startsWith("http://") || path.startsWith("https://") -> android.net.Uri.parse(path)
        else -> android.net.Uri.parse("file://$path")
    }

    private fun mapRepeat(playerMode: Int): RepeatMode = when (playerMode) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.OFF
    }
}
