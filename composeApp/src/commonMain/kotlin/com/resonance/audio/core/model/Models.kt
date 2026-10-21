package com.resonance.audio.core.model

enum class Route { LIBRARY, FOLDERS, STREAM, PLAYLISTS, SETTINGS }

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String = "",
    val durationMs: Long,
    val path: String,
    val formatLabel: String,
    val folderPath: String = "",
    val sizeBytes: Long = 0,
    val isRemote: Boolean = false,
) {
    val seed: Int get() = stableHash("$artist|$album|$title")
}

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String>,
) {
    val seed: Int get() = stableHash(id)
}

data class FolderEntry(
    val path: String,
    val displayName: String,
    val trackCount: Int,
    val enabled: Boolean,
    val labelBytes: Long = 0,
) {
    val badge: Badge get() = if (enabled) Badge.WHITELIST else Badge.BLACKLISTED
}

enum class Badge { WHITELIST, AUTO_INDEX, BLACKLISTED }

data class StorageInfo(
    val usedBytes: Long,
    val totalBytes: Long,
    val musicBytes: Long,
    val podcastsBytes: Long,
    val trackCount: Int,
    val scanState: ScanState,
    val lastScanAtMs: Long,
)

enum class ScanState { IDLE, INDEXED, SCANNING }

data class Station(
    val id: String,
    val name: String,
    val provider: String,
    val subtitle: String,
    val streamUrl: String,
    val bitrateLabel: String,
    val listeners: String,
    val formatLabel: String,
) {
    val seed: Int get() = stableHash(id)
}

data class StreamTrack(
    val id: String,
    val title: String,
    val author: String,
    val source: String,
    val url: String,
    val formatLine: String,
    val sizeLabel: String,
    val license: String,
) {
    val seed: Int get() = stableHash(id)
}

data class PodcastEpisode(
    val id: String,
    val show: String,
    val title: String,
    val episodeLine: String,
    val endLine: String,
    val tag: String,
    val url: String,
) {
    val seed: Int get() = stableHash(id)
}

data class EqBand(
    val freqLabel: String,
    val levelDb: Int,
    val minDb: Int = -15,
    val maxDb: Int = 15,
)

data class EqUiState(
    val available: Boolean,
    val bands: List<EqBand>,
    val availablePresets: List<String>,
    val currentPreset: String,
    val bassBoost: Float,
    val spatial: Float,
    val outputDevice: String,
    val bitrateLabel: String,
) {
    companion object {
        val Empty = EqUiState(
            available = false,
            bands = emptyList(),
            availablePresets = emptyList(),
            currentPreset = "Flat",
            bassBoost = 0f,
            spatial = 0f,
            outputDevice = "",
            bitrateLabel = "",
        )
    }
}

enum class RepeatMode { OFF, ONE, ALL }

data class PlaybackUiState(
    val hasCurrent: Boolean,
    val currentId: String,
    val title: String,
    val artist: String,
    val formatLabel: String,
    val sourceLabel: String,
    val seed: Int,
    val durationMs: Long,
    val positionMs: Long,
    val isPlaying: Boolean,
    val isLive: Boolean,
    val shuffle: Boolean,
    val repeatMode: RepeatMode,
) {
    companion object {
        val Empty = PlaybackUiState(
            hasCurrent = false,
            currentId = "",
            title = "",
            artist = "",
            formatLabel = "",
            sourceLabel = "",
            seed = 0,
            durationMs = 0,
            positionMs = 0,
            isPlaying = false,
            isLive = false,
            shuffle = false,
            repeatMode = RepeatMode.OFF,
        )
    }
}

data class ScanRules(
    val ignoreShortAudio: Boolean,
    val autoScanNewMedia: Boolean,
)

fun stableHash(s: String): Int {
    var h = 1125899906842597L
    for (c in s) {
        h = 31 * h + c.code
    }
    return (h xor (h shr 24)).toInt()
}
