package com.resonance.audio.core

import com.resonance.audio.core.model.Track
import com.resonance.audio.core.model.stableHash

/** Browses the local library by one of its metadata dimensions. */
enum class GroupKind(val label: String) {
    ARTIST("Artists"),
    ALBUM("Albums"),
    GENRE("Genres"),
    FOLDER("Folders"),
}

data class TrackGroup(
    val key: String,
    val title: String,
    val subtitle: String,
    val kind: GroupKind,
    val tracks: List<Track>,
) {
    val seed: Int get() = stableHash("${kind.name}|$key")
    val count: Int get() = tracks.size
    val durationMs: Long get() = tracks.sumOf { it.durationMs }
}

/** Groups [tracks] by [kind], sorting groups alphabetically and tracks by title. */
fun groupTracks(tracks: List<Track>, kind: GroupKind): List<TrackGroup> {
    if (tracks.isEmpty()) return emptyList()
    val grouped = when (kind) {
        GroupKind.ARTIST -> tracks.groupBy { it.artist.trim().ifBlank { "Unknown Artist" } }
        GroupKind.ALBUM -> tracks.groupBy { it.album.trim().ifBlank { "Unknown Album" } }
        GroupKind.GENRE -> tracks.groupBy { it.genre.trim().ifBlank { "Unknown Genre" } }
        GroupKind.FOLDER -> tracks.groupBy { it.folderPath.trim().trimEnd('/').ifBlank { "Internal Storage" } }
    }
    return grouped.map { (key, items) ->
        val ordered = items.sortedBy { it.title.lowercase() }
        TrackGroup(
            key = key,
            title = if (kind == GroupKind.FOLDER) key.substringAfterLast('/').ifBlank { key } else key,
            subtitle = subtitleFor(kind, ordered),
            kind = kind,
            tracks = ordered,
        )
    }.sortedBy { it.title.lowercase() }
}

private fun subtitleFor(kind: GroupKind, tracks: List<Track>): String {
    val count = "${tracks.size} track${if (tracks.size == 1) "" else "s"}"
    val length = lengthLabel(tracks.sumOf { it.durationMs })
    return when (kind) {
        GroupKind.ARTIST -> {
            val albums = tracks.map { it.album.trim() }.filter { it.isNotBlank() }.distinct().size
            val albumPart = if (albums > 0) "$albums album${if (albums == 1) "" else "s"}" else "Singles"
            "$albumPart • $count • $length"
        }
        GroupKind.ALBUM -> {
            val artist = tracks.first().artist.trim().ifBlank { "Unknown Artist" }
            "$artist • $count • $length"
        }
        GroupKind.GENRE, GroupKind.FOLDER -> "$count • $length"
    }
}

private fun lengthLabel(ms: Long): String {
    val minutes = (ms / 60_000).toInt()
    return if (minutes >= 60) "${minutes / 60} hr ${minutes % 60} min" else "$minutes min"
}
