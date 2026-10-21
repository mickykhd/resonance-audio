package com.resonance.audio

import com.resonance.audio.core.formatClock
import com.resonance.audio.core.formatCount
import com.resonance.audio.core.formatDuration
import com.resonance.audio.core.formatGb
import com.resonance.audio.core.formatMbOrGb
import com.resonance.audio.core.losslessBitDepth
import com.resonance.audio.core.qualityLabelFor
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun clockFormatsMinutesAndSeconds() {
        assertEquals("02:18", formatClock(138_000))
        assertEquals("00:00", formatClock(0))
        assertEquals("61:05", formatClock(3_665_000))
    }

    @Test
    fun durationFormatsCasually() {
        assertEquals("3:58", formatDuration(238_000))
        assertEquals("0:42", formatDuration(42_000))
        assertEquals("12:00", formatDuration(720_000))
    }

    @Test
    fun countAddsThousandsSeparators() {
        assertEquals("0", formatCount(0))
        assertEquals("999", formatCount(999))
        assertEquals("1,024", formatCount(1024))
        assertEquals("12,345,678", formatCount(12_345_678))
    }

    @Test
    fun gbFormatsDecimalPlaces() {
        assertEquals("1.4", formatGb(1_500_000_000))
        assertEquals("100", formatGb(107_400_000_000))
    }

    @Test
    fun mbOrGbSwitchesUnitBySize() {
        assertEquals("0 MB", formatMbOrGb(0))
        assertEquals("33 MB", formatMbOrGb(35_000_000))
        assertEquals("1.4 GB", formatMbOrGb(1_500_000_000))
    }

    @Test
    fun qualityLabelsMapByExtension() {
        assertEquals("320kbps MP3", qualityLabelFor("mp3", 320))
        assertEquals("MP3 128k", qualityLabelFor("mp3", 128))
        assertEquals("FLAC 24-bit", qualityLabelFor("flac", 0))
        assertEquals("AAC 256k", qualityLabelFor("aac", 256))
        assertEquals("OGG 192k", qualityLabelFor("OGG", 192))
    }

    @Test
    fun losslessDepthClamps() {
        assertEquals(16, losslessBitDepth(0, 1_000))
        // ~40kbps effective stereo 44.1k clamps to 16
        assertEquals(16, losslessBitDepth(3_830_000, 180_000))
        // True 24-bit FLAC-size file: 44100*2ch*3B*240s
        assertEquals(24, losslessBitDepth(63_504_000, 240_000))
        // absurd large file saturates at 32
        assertEquals(32, losslessBitDepth(50_000_000, 60_000))
    }
}
