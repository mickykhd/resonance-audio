package com.resonance.audio.core

import com.resonance.audio.core.model.RepeatMode

/**
 * Pure queue arithmetic shared between the platform players and unit tests.
 */
object QueueLogic {

    fun clampIndex(size: Int, index: Int): Int =
        if (size == 0) 0 else ((index % size) + size) % size

    fun nextIndex(size: Int, currentIndex: Int, shuffle: Boolean, repeat: RepeatMode): Int? {
        if (size <= 0) return null
        if (size == 1) {
            return when (repeat) {
                RepeatMode.OFF -> null
                RepeatMode.ONE, RepeatMode.ALL -> 0
            }
        }
        if (shuffle) {
            return when (repeat) {
                RepeatMode.OFF -> if (currentIndex >= size - 1) null else (currentIndex + 1) % size
                else -> if (size - 1 == currentIndex) 0 else (currentIndex + 1) % size
            }
        }
        val n = currentIndex + 1
        return when (repeat) {
            RepeatMode.OFF -> if (n < size) n else null
            RepeatMode.ALL -> n % size
            RepeatMode.ONE -> n % size
        }
    }

    fun previousIndex(size: Int, currentIndex: Int, shuffle: Boolean, repeat: RepeatMode): Int? {
        if (size <= 0) return null
        if (size == 1) {
            return when (repeat) {
                RepeatMode.OFF -> null
                else -> 0
            }
        }
        if (shuffle) {
            // In shuffle we simply step back one slot; keep behavior simple.
            return if (currentIndex <= 0) null else currentIndex - 1
        }
        val p = currentIndex - 1
        return when (repeat) {
            RepeatMode.OFF -> if (p >= 0) p else null
            RepeatMode.ONE, RepeatMode.ALL -> if (p < 0) size - 1 else p
        }
    }
}
