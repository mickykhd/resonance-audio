package com.resonance.audio.core

import kotlin.math.sin

/**
 * Deterministic pseudo-waveform used by the Now Playing scrubber.
 * Same seed always yields the same silhouette per track.
 */
fun waveformBars(seed: Int, bars: Int = 56, minFraction: Float = 0.18f, maxFraction: Float = 1f): List<Float> {
    require(bars > 0)
    val list = ArrayList<Float>(bars)
    val s = seed % 997
    var accum = 0.37f
    for (i in 0 until bars) {
        val t = i.toFloat() / bars
        val a = sin(i * 0.53 + s * 0.013)
        val b = sin(i * 1.71 + s * 0.031 + 1.3)
        val c = sin(i * 0.23 + s * 0.007 + 2.1)
        val v = (a * 0.5 + b * 0.3 + c * 0.2).toFloat()
        accum = 0.82f * accum + 0.18f * v
        val normalized = ((accum / 1.1f) + 0.55f)
        val clamped01 = normalized.coerceIn(0f, 1f)
        list.add(minFraction + clamped01 * (maxFraction - minFraction))
    }
    // Normalize peaks to maxFraction for punchy look
    val peak = list.max()
    return list.map { (it / peak) * (maxFraction - minFraction) + minFraction }
}
