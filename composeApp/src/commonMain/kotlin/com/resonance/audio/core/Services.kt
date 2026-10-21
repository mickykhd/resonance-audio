package com.resonance.audio.core

import kotlinx.coroutines.flow.StateFlow
import com.resonance.audio.core.model.EqUiState
import com.resonance.audio.core.model.PlaybackUiState
import com.resonance.audio.core.model.RepeatMode
import com.resonance.audio.core.model.Track

/** Reads the on-device music library (MediaStore on Android). */
interface LibrarySource {
    suspend fun hasAudioPermission(): Boolean
    suspend fun requestPermission(): Boolean

    suspend fun scanTracks(): List<Track>
    suspend fun scanFolders(): List<com.resonance.audio.core.model.FolderEntry>
    suspend fun storageInfo(trackCount: Int): com.resonance.audio.core.model.StorageInfo

    /** Downloads a CC/URL track into the device Music directory; returns when done. */
    suspend fun downloadToLibrary(title: String, artist: String, url: String)

    suspend fun isOnMeteredNetwork(): Boolean
}

/** App-scoped switches persisted on platform storage. */
interface SettingsStore {
    suspend fun wifiOnly(): Boolean
    suspend fun setWifiOnly(on: Boolean)

    suspend fun streamQuality(): Int // 0 auto, 1 = 128k, 2 = 320k
    suspend fun setStreamQuality(q: Int)

    suspend fun ignoreShortAudio(): Boolean
    suspend fun setIgnoreShortAudio(on: Boolean)

    suspend fun autoScanNewMedia(): Boolean
    suspend fun setAutoScanNewMedia(on: Boolean)

    suspend fun folderEnabled(path: String): Boolean
    suspend fun setFolderEnabled(path: String, on: Boolean)
    suspend fun customFolders(): List<String>
    suspend fun addCustomFolder(path: String)

    suspend fun favorites(): Set<String>
    suspend fun setFavorite(id: String, on: Boolean)

    suspend fun recentTrackIds(): List<String>
    suspend fun pushRecentTrackId(id: String)

    suspend fun playlists(): List<com.resonance.audio.core.model.Playlist>
    suspend fun savePlaylist(playlist: com.resonance.audio.core.model.Playlist)
    suspend fun deletePlaylist(id: String)

    suspend fun lastScanMs(): Long
    suspend fun setLastScanMs(ms: Long)
}

/** Live audio playback engine. */
interface PlayerController {
    val state: StateFlow<PlaybackUiState>
    val eq: StateFlow<EqUiState>

    fun playQueue(tracks: List<Track>, startIndex: Int, sourceLabel: String)
    fun playLive(tracks: List<Track>, index: Int, sourceLabel: String)
    fun playPause()
    fun next()
    fun previous()
    fun seekTo(ms: Long)
    fun setShuffle(on: Boolean)
    fun cycleRepeatMode(): RepeatMode
    suspend fun setEqBandLevel(index: Int, levelDb: Int)
    suspend fun applyEqPreset(name: String)
    suspend fun setBassBoost(fraction: Float)
    suspend fun setSpatial(fraction: Float)
    fun release()
}
