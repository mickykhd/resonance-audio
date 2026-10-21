package com.resonance.audio.playback

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Hosts a MediaSession around the shared ExoPlayer so lock-screen, headset and
 * notification transport controls keep working while the UI is in the background.
 */
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player: ExoPlayer? = PlaybackRegistry.engine?.playerForSession
        if (player != null) {
            session = MediaSession.Builder(this, player).build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = PlaybackRegistry.player
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }
}

/** Holds the single engine instance so the service can bind to the same player. */
object PlaybackRegistry {
    @Volatile
    var engine: ExoPlayerEngine? = null

    val player: ExoPlayer? get() = engine?.playerForSession

    fun isPlaying(player: Player?): Boolean = player?.isPlaying == true
}
