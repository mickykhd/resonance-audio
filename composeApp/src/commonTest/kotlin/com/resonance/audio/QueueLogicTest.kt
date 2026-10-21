package com.resonance.audio

import com.resonance.audio.core.QueueLogic
import com.resonance.audio.core.model.RepeatMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QueueLogicTest {

    @Test
    fun clampIndexBouncesAndWraps() {
        assertEquals(1, QueueLogic.clampIndex(3, -5))
        assertEquals(2, QueueLogic.clampIndex(3, 5))
        assertEquals(0, QueueLogic.clampIndex(0, 7))
        assertEquals(2, QueueLogic.clampIndex(3, -1))
    }

    @Test
    fun emptyQueueYieldsNulls() {
        assertNull(QueueLogic.nextIndex(0, 0, false, RepeatMode.ALL))
        assertNull(QueueLogic.previousIndex(0, 0, false, RepeatMode.OFF))
    }

    @Test
    fun singleTrackRespectsRepeat() {
        assertNull(QueueLogic.nextIndex(1, 0, false, RepeatMode.OFF))
        assertEquals(0, QueueLogic.nextIndex(1, 0, false, RepeatMode.ONE))
        assertEquals(0, QueueLogic.previousIndex(1, 0, false, RepeatMode.ALL))
    }

    @Test
    fun nextWithoutRepeatStopsAtEnd() {
        assertNull(QueueLogic.nextIndex(3, 2, false, RepeatMode.OFF))
        assertEquals(1, QueueLogic.nextIndex(3, 0, false, RepeatMode.OFF))
    }

    @Test
    fun nextWrapsOnAll() {
        assertEquals(0, QueueLogic.nextIndex(3, 2, false, RepeatMode.ALL))
    }

    @Test
    fun previousFromStart() {
        assertEquals(2, QueueLogic.previousIndex(3, 0, false, RepeatMode.ALL))
        assertNull(QueueLogic.previousIndex(3, 0, false, RepeatMode.OFF))
        assertEquals(2, QueueLogic.previousIndex(3, 0, false, RepeatMode.ONE))
    }
}
