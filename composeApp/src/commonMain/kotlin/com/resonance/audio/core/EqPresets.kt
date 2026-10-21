package com.resonance.audio.core

/**
 * Studio parametric presets expressed on the 32..16k (10 band) axis in dB.
 */
object EqPresets {

    val BAND_LABELS = listOf("32", "64", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    data class Preset(val name: String, val levels: List<Int>)

    val ALL = listOf(
        Preset("Flat", List(10) { 0 }),
        Preset("Electronic / Bass Boost", listOf(9, 11, 8, 3, 2, 1, 5, 8, 10, 11)),
        Preset("Vocal Warm", listOf(-2, 0, 3, 6, 7, 6, 3, 1, -1, -2)),
        Preset("Neutral Reference", listOf(1, 2, 2, 1, 0, 0, 1, 2, 2, 3)),
        Preset("Late Night", listOf(4, 3, 1, -1, -2, -2, -1, 1, 3, 5)),
    )

    /** Map fractional f (0=low, 1=treble) onto this preset signature so it
     *  also works on devices that expose fewer EQ bands. */
    fun sampleFor(f: Float, preset: Preset): Int {
        val t = (f * (preset.levels.size - 1))
        val i = (t).toInt().coerceIn(0, preset.levels.lastIndex)
        val j = (i + 1).coerceAtMost(preset.levels.lastIndex)
        val frac = t - i
        val a = preset.levels[i]
        val b = preset.levels[j]
        return (a + (b - a) * frac).toInt()
    }

    fun byName(name: String): Preset = ALL.firstOrNull { it.name == name } ?: ALL.first()
}
