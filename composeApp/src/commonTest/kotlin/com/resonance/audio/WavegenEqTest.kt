package com.resonance.audio

import com.resonance.audio.core.EqPresets
import com.resonance.audio.core.model.stableHash
import com.resonance.audio.core.waveformBars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WavegenTest {

    @Test
    fun barsAreDeterministicPerSeed() {
        assertEquals(waveformBars(42, 20), waveformBars(42, 20))
        assertTrue(waveformBars(42, 20) != waveformBars(43, 20))
    }

    @Test
    fun barCountAndRangeHonorContract() {
        val bars = waveformBars(7, 56)
        assertEquals(56, bars.size)
        assertTrue(bars.min() >= 0.18f)
        assertTrue(bars.max() <= 1.0f)
        assertTrue(bars.max() > bars.min())
    }

    @Test
    fun coldStartsDifferAcrossSeedsButStayBounded() {
        for (seed in listOf(1, 2, 3, 10_000)) {
            val bars = waveformBars(seed, 42)
            assertTrue(bars.min() >= 0.0f)
            assertTrue(bars.max() <= 1.0f)
        }
    }
}

class EqPresetTest {

    @Test
    fun flatPresetIsAllZero() {
        for (f in floatArrayOf(0f, 0.25f, 0.5f, 1f)) {
            assertEquals(0, EqPresets.sampleFor(f, EqPresets.byName("Flat")))
        }
    }

    @Test
    fun interpolationStaysWithinBoundsAndInterpolates() {
        val electronic = EqPresets.byName("Electronic / Bass Boost")
        val low = EqPresets.sampleFor(0.0f, electronic)
        val mid = EqPresets.sampleFor(0.7f, electronic)
        assertEquals(9, low)
        assertTrue(mid in 2..8)
    }

    @Test
    fun byNameFallsBackToFlat() {
        assertEquals(EqPresets.ALL.first().name, EqPresets.byName("Does Not Exist").name)
    }
}

class StableHashTest {

    @Test
    fun stableAcrossCalls() {
        assertEquals(stableHash("a|b|c"), stableHash("a|b|c"))
        assertTrue(stableHash("a|b|c") != stableHash("a|b|d"))
    }
}
