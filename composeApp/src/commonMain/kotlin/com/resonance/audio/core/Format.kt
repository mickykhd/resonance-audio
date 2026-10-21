package com.resonance.audio.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Formats ms as zero-padded mono clock, e.g. 218000 -> "02:18". */
fun formatClock(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}

/** Formats ms as casual duration, e.g. 238000 -> "3:58". */
fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    return "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
}

/** 68.2 GB style formatting. */
fun formatGb(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 100) {
        "${gb.roundToInt()}"
    } else {
        String.format("%.1f", gb)
    }
}

/** MB under 512 MB, GB above. */
fun formatMbOrGb(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    return if (bytes < 512L * 1024 * 1024) {
        "${bytes / 1_048_576} MB"
    } else {
        "${formatGb(bytes)} GB"
    }
}

fun formatCount(n: Long): String {
    return formatCountImpl(n)
}

private fun formatCountImpl(n: Long): String {
    val s = n.toString()
    val out = StringBuilder()
    for ((i, c) in s.withIndex()) {
        if (i != 0 && (s.length - i) % 3 == 0) out.append(',')
        out.append(c)
    }
    return out.toString()
}

/** Derives the display quality badge from mime/extension and bitrate. */
fun qualityLabelFor(extensionLower: String, bitrateKbps: Int): String {
    val base = extensionLower.uppercase()
    val k = "${bitrateKbps}k"
    return when (base) {
        "FLAC" -> "FLAC 24-bit"
        "ALAC" -> "ALAC 24-bit"
        "WAV" -> "WAV"
        "MP3" -> if (bitrateKbps >= 290) "320kbps MP3" else "MP3 $k"
        "AAC" -> "AAC $k"
        "M4A" -> "M4A $k"
        "OGG" -> "OGG $k"
        "OPUS" -> "OPUS $k"
        else -> if (bitrateKbps > 0) "$base $k" else base
    }
}

/** Determines a bit-depth style badge for lossless files by bandwidth usage. */
fun losslessBitDepth(sizeBytes: Long, durationMs: Long, sampleRateHz: Int = 44100): Int {
    if (durationMs <= 0 || sizeBytes <= 0) return 16
    val bitPerSampleApprox =
        (sizeBytes * 8.0) / (durationMs / 1000.0) / sampleRateHz / 2.0 // stereo
    return max(16, min(32, bitPerSampleApprox.roundToInt()))
}
